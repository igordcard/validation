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
Test Setup        Check that k8s cluster is reachable
Test Teardown     Cleanup Sonobuoy

*** Variables ***
${RESULT_PATH}    /opt/akraino/validation/results/conformance/
${REPO_PATH}      /opt/akraino/validation/repo/conformance/
${LOG}            ${RESULT_PATH}${/}conformance.log

*** Test Cases ***
Run Sonobuoy Conformance Test
        # Start the test
        Run                     kubectl apply -f ${REPO_PATH}${/}sonobuoy.yaml
        Sleep                   5s
        ${rc}  ${output}=       Run And Return Rc And Output
                                ...  kubectl describe pod/sonobuoy -n heptio-sonobuoy
        Append To File          ${LOG}  ${output}${\n}

        # Wait until the test finishes execution
        Run                     until sonobuoy status | grep "Sonobuoy has completed"; do sleep 120; done
        Append To File          ${LOG}  "Sonobuoy has completed"${\n}

        # Get the result and store the sonobuoy logs
        ${rc}  ${output}=       Run And Return Rc And Output
                                ...  results=$(sonobuoy retrieve ${RESULT_PATH}) && sonobuoy e2e $results
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
                                ...  kubectl delete -f ${REPO_PATH}${/}sonobuoy.yaml
        Append To File          ${LOG}  ${output}${\n}
        Sleep                   3s
        Should Contain          ${output}      service "sonobuoy-master" deleted
