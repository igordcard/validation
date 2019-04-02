##############################################################################
# Copyright (c) 2019 AT&T Intellectual Property.                             #
# Copyright (c) 2019 Nokia.                                                  #
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
Documentation     Run K8s Conformance Test
Library           SSHLibrary
Library           OperatingSystem
Library           BuiltIn
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections

*** Variables ***
${HOST}           localhost
${USERNAME}       localadmin
${LOG}            /opt/akraino/validation/conformance/print_conformance.txt

*** Test Cases ***
Get Robot Version
        [Documentation]         Verify Robot
        Start Command           robot --version
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}               Robot Framework

Verify Go Package
        [Documentation]         Verify Go Package
        Start Command           hello
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}               Hello, world!


Start Sonobuoy Conformance Test
        [Documentation]         Test will take about 1hr and 40 mins to complete
        ${output}=              Execute Command     cat /opt/akraino/validation/conformance/sonobuoy.yaml | kubectl apply -f -    sudo=True
        Append To File          ${LOG}  ${output}${\n}
        Sleep                   3s
        Should Contain          ${output}             pod/sonobuoy created


Description of Sonobuoy Pods
        [Documentation]         Description of Sonobuoy Pod
        ${output}  ${rc}=       Execute Command      kubectl describe pod/sonobuoy -n heptio-sonobuoy    return_rc=True    sudo=True
        Append To File          ${LOG}  ${output}${\n}
        Should Be Equal As Integers  ${rc}  0


Verify Conformance Test is Running
        [Documentation]         Conformance Test ongoing
        ${output}  ${rc}=       Execute Command       kubectl get pods --all-namespaces -o wide | grep heptio    return_rc=True    sudo=True
        Append To File          ${LOG}  ${output}${\n}
        Should Be Equal As Integers  ${rc}  0



*** Keywords ***
Open Connection And Log In
  Open Connection       ${HOST}
  Login With Public Key    ${USERNAME}  /root/.ssh/${USERNAME}_id_rsa

