##############################################################################
# Copyright (c) 2019 AT&T Intellectual Property.                             #
# Copyright (c) 2019 Nokia.                                                  #
# Copyright (c) 2019 Enea AB
#                                                                            #
# Licensed under the Apache License, Version 2.0 (the "License");            #
# you maynot use this file except in compliance with the License.            #
#                                                                            #
# You may obtain a copy of the License at                                    #
#       http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                            #
# Unless required by applicable law or agreed to in writing, software        #
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  #
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           #
# See the License for the specific language governing permissions and        #
# limitations under the License.                                             #
##############################################################################

*** Settings ***
Documentation     Run k8s conformance test using sonobuoy
Library           OperatingSystem
Library           BuiltIn
Library           Collections
Library           String
Library           SSHLibrary
Library           Process
Test Setup        Run Keywords
...               Check that k8s cluster is reachable
...               Onboard Images
...               Create Manifest File
Test Teardown     Run Keywords
...               Cleanup Sonobuoy
...               Close All Connections

*** Variables ***
${LOG}            ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}.log

&{SONOBUOY}         path=gcr.io/heptio-images
...                 name=sonobuoy:v0.15.1
&{E2E}              path=akraino
...                 name=validation:kube-conformance-v1.15
&{SYSTEMD_LOGS}     path=akraino
...                 name=validation:sonobuoy-plugin-systemd-logs-latest
&{SONOBUOY_IMGS}    sonobuoy=&{SONOBUOY}
...                 e2e=&{E2E}
...                 systemd_logs=&{SYSTEMD_LOGS}

*** Test Cases ***
Run Sonobuoy Conformance Test
        # Start the test
        Run                     kubectl apply -f ${CURDIR}${/}sonobuoy.yaml
        Sleep                   10s
        ${rc}  ${output}=       Run And Return Rc And Output
                                ...  kubectl describe pod/sonobuoy -n heptio-sonobuoy
        Append To File          ${LOG}  ${output}${\n}

        # Wait until the test finishes execution
        Run                     while sonobuoy status | grep "Sonobuoy is still running"; do sleep 180; done
        Append To File          ${LOG}  "Sonobuoy has completed"${\n}

        # Get the result and store the sonobuoy logs
        ${rc}  ${output}=       Run And Return Rc And Output
                                ...  results=$(sonobuoy retrieve ${LOG_PATH}) && sonobuoy e2e $results
        Append To File          ${LOG}  ${output}${\n}
        Should Contain          ${output}       failed tests: 0

*** Keywords ***
Check that k8s cluster is reachable
        # Check that the config file is mounted in the container
        File Should Not Be Empty  /root/.kube/config

        # Make sure the pod is reachable with the local k8s client
        ${rc}  ${output}=       Run And Return Rc And Output
                                ...  kubectl get pods --all-namespaces
        Append To File          ${LOG}  ${output}${\n}
        Should Contain          ${output}      kube-system

Cleanup Sonobuoy
        ${rc}  ${output}=       Run And Return Rc And Output
                                ...  kubectl delete -f ${CURDIR}${/}sonobuoy.yaml
        Append To File          ${LOG}  ${output}${\n}
        Sleep                   3s
        Should Contain          ${output}      service "sonobuoy-master" deleted

Open Connection And Log In
        Open Connection         ${HOST}
        Login With Public Key   ${USERNAME}  ${SSH_KEYFILE}

Upload To Internal Registry
         [Arguments]            ${path}  ${name}
         ${rc}=  Execute Command
         ...     docker pull ${path}/${name}
         ...       return_stdout=False  return_rc=True
         Should Be Equal As Integers  ${rc}  0
         ${rc}=  Execute Command
         ...     docker tag ${path}/${name} ${INT_REG}/bluval/${name}
         ...       return_stdout=False  return_rc=True
         Should Be Equal As Integers  ${rc}  0
         ${rc}=  Execute Command
         ...     docker push ${INT_REG}/bluval/${name}
         ...       return_stdout=False  return_rc=True
         Should Be Equal As Integers  ${rc}  0

Onboard Sonobuoy Images
        FOR  ${img}  IN  @{SONOBUOY_IMGS}
            ${path}=            Get From Dictionary  ${SONOBUOY_IMGS['${img}']}  path
            ${name}=            Get From Dictionary  ${SONOBUOY_IMGS['${img}']}  name
            Upload To Internal Registry  ${path}  ${name}
            Set To Dictionary  ${SONOBUOY_IMGS['${img}']}  path=${INT_REG}/bluval
        END

Onboard Kubernetes e2e Test Images
        ${result}=              Run Process  sonobuoy  images
        Should Be Equal As Integers  ${result.rc}  0
        @{images}=              Split String  ${result.stdout}
        FOR  ${img}  IN  @{images}
            ${path}  ${name}  Split String From Right  ${img}  /  1
            Upload To Internal Registry  ${path}  ${name}
        END

Onboard Images
        ${INT_REG}=             Get Variable Value  ${INTERNAL_REGISTRY}  ${EMPTY}
        Set Test Variable       ${INT_REG}
        Return From Keyword If  $INT_REG == '${EMPTY}'
        Open Connection And Log In
        Onboard Sonobuoy Images
        Onboard Kubernetes e2e Test Images

Create Manifest File
        @{flags}=               Set Variable
        ...                         --e2e-focus  \\[Conformance\\\]
        ...                         --e2e-skip  Aggregator|Alpha|\\[(Disruptive|Feature:[^\\]]+|Flaky)\\]
        ...                         --kube-conformance-image  ${SONOBUOY_IMGS.e2e.path}/${SONOBUOY_IMGS.e2e.name}
        ...                         --sonobuoy-image  ${SONOBUOY_IMGS.sonobuoy.path}/${SONOBUOY_IMGS.sonobuoy.name}
        ...                         --image-pull-policy  Always
        ...                         --timeout  14400
        Run Keyword If          $INT_REG != '${EMPTY}'  Run Keywords
        ...                     Append To List  ${flags}
        ...                         --e2e-repo-config  ${CURDIR}${/}custom_repos.yaml
        ...                     AND
        ...                     Run Process  sed  -i  s|{{ registry }}|${INT_REG}/bluval|g
        ...                         ${CURDIR}${/}custom_repos.yaml
        ${result}=              Run Process  sonobuoy  gen  @{flags}
        Should Be Equal As Integers  ${result.rc}  0
        ${manifest}=            Replace String  ${result.stdout}
        ...                         image: gcr.io/heptio-images/sonobuoy-plugin-systemd-logs:latest
        ...                         image: ${SONOBUOY_IMGS.systemd_logs.path}/${SONOBUOY_IMGS.systemd_logs.name}
        Create File             ${CURDIR}${/}sonobuoy.yaml  ${manifest}
