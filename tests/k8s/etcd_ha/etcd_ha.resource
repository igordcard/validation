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
Library            BuiltIn
Library            Process
Library            String
Library            SSHLibrary
Library            OperatingSystem

*** Variables ***
${ETCD_VERSION}         3
${SSH_KEYFILE}          /root/.ssh/id_rsa

*** Keywords ***
Open Connection And Log In
    Open Connection        ${HOST}
    Login With Public Key  ${USERNAME}  ${SSH_KEYFILE}

Retrieve Etcd Config
     ${result}=     Execute Command    kubectl get nodes -l node-role.kubernetes.io/master -o jsonpath\='{.items[*].metadata.name}'
     @{nodes}=      Split String  ${result}
     Set Test Variable  ${pod}  etcd-@{nodes}[0]
     ${members}=    Get Etcd Members
     Set Test Variable  ${members}

Get Etcd Members
    ${result}=     Execute Command  kubectl exec -n kube-system -it ${pod} etcdctl${ETCD_VERSION} member list
    log        ${result}

Delete Etcd Node
    ${result}=     Execute Command  kubectl delete pod -n kube-system ${pod}
    Should Be Equal As Strings  ${result}  pod "${pod}" deleted  msg="pod delete failed"

Wait For Etcd Node To Recover
    Wait Until Keyword Succeeds  5m  10s  Etcd Node Should Be Running

Etcd Node Should Be Running
    ${result}=     Execute Command  kubectl get -n kube-system -o template pod/${pod} --template\='{{.status.phase}}'
    Should Contain  ${result}  Running

Etcd Endpoint Should Be Healthy
    ${result}=     Execute Command  kubectl exec -n kube-system -ti ${pod} etcdctl${ETCD_VERSION} endpoint health
    Should Contain  ${result}  healthy

Etcd Members Should Be Unchanged
    ${output}=     Get Etcd Members
    Should Be Equal As Strings  ${output}  ${members}

Etcd Cluster Should Be Healthy
    Etcd Node Should Be Running
    Etcd Endpoint Should Be Healthy
    Etcd Members Should Be Unchanged
