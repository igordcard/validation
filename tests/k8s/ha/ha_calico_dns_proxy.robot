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
Documentation     HA test cases for calico, coredns and haproxy
Library           SSHLibrary
Library           OperatingSystem
Library           BuiltIn
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections

*** Variables ***
${HOST}           localhost
${USERNAME}       localadmin
${LOG}            ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}.log



*** Test Cases ***

## Calico

Verify calico status
        [Documentation]         Calico  nodes are active
        Start Command           kubectl get pod -n kube-system -o wide -l k8s-app=calico-node     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              Running

Failure of calico nodes
        [Documentation]         Calico pods deleted
        ${output}=              Execute Command         kubectl delete pod -n kube-system -l k8s-app=calico-node     sudo=True
        Append To File          ${LOG}  ${output}${\n}
        Sleep                   5s
        Should Contain          ${output}             deleted

Verify calico node re-establishment
        [Documentation]         Calico component is auto-created and running
        Sleep                   5s
        Start Command           kubectl get pod -n kube-system -o wide -l k8s-app=calico-node     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              Running

## Coredns

Verify coredns status
        [Documentation]         Coredns components active
        Start Command           kubectl get pod -n kube-system -o wide -l coredns=enabled     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              Running


Failure of coredns in control plane node
        [Documentation]         coredns pod is deleted
        ${output}=              Execute Command         kubectl delete pod -n kube-system -l coredns=enabled     sudo=True
        Append To File          ${LOG}  ${output}${\n}
        Sleep                   30s
        Should Contain          ${output}             deleted


Verify coredns re-establishment
        [Documentation]         Coredns component is auto-created and running
        Sleep                   5s
        Start Command           kubectl get pod -n kube-system -l coredns=enabled     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              Running

## Haproxy

Verify haproxy status
        [Documentation]         All haproxy pods are active
        Start Command           kubectl get pod -n kube-system -o wide -l application=haproxy    sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              Running


Failure of haproxy
        [Documentation]         Haproxy  components deleted
        ${output}=              Execute Command        kubectl delete pod -n kube-system -l application=haproxy     sudo=True
        Append To File          ${LOG}  ${output}${\n}
        Sleep                   5s
        Should Contain          ${output}             deleted


Verify haproxy re-establishment
        [Documentation]         Haproxy components auto-created and running
        Sleep                   20s
        Start Command           kubectl get pod -n kube-system -o wide -l application=haproxy     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              Running


*** Keywords ***
Open Connection And Log In
  Open Connection       ${HOST}
  Login With Public Key    ${USERNAME}   /root/.ssh/${USERNAME}_id_rsa


