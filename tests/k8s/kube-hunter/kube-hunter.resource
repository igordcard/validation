##############################################################################
# Copyright (c) 2020 AT&T Intellectual Property.                             #
# Copyright (c) 2020 Nokia.                                                  #
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
Library             Collections
Library             OperatingSystem
Library             Process
Library             SSHLibrary
Library             String


*** Variables ***
${REPORTDIR}        ${LOG_PATH}/${SUITE_NAME.replace(' ','_')}
&{KUBE_HUNTER}      path=akraino
...                 name=validation:kube-hunter-latest


*** Keywords ***
Open Connection And Log In
    Open Connection  ${HOST}
    Run Keyword IF  '${SSH_KEYFILE}' != 'None'  Login With Public Key  ${USERNAME}  ${SSH_KEYFILE}  ELSE IF  '${PASSWORD}' != 'None'  Login  ${USERNAME}  ${PASSWORD}  ELSE  FAIL

Get Cluster Address
    ${result}=        Run Process  kubectl  config  view  --minify
    ...                            -o  jsonpath\={.clusters[0].cluster.server}
    Should Be Equal As Integers  ${result.rc}  0
    ${addr}=          Fetch From Right  ${result.stdout}  ://
    ${addr}=          Fetch From Left  ${addr}  :
    Should Not Be Empty  ${addr}
    [Return]          ${addr}

Get Remote Addresses
    ${result}=        Run Process  kubectl  get  nodes
    ...                   -o  jsonpath\={.items[*].status.addresses[?(@.type\=\="ExternalIP")].address}
    Should Be Equal As Integers  ${result.rc}  0
    Pass Execution If  '${result.stdout}' == '${EMPTY}'  No external node IPs exposed
    @{addrs}=         Split String  ${result.stdout}
    [Return]          ${addrs}

Upload To Internal Registry
    [Arguments]       ${path}  ${name}
    ${rc}=            Execute Command
    ...               docker pull ${path}/${name}
    ...                 return_stdout=False  return_rc=True
    Should Be Equal As Integers  ${rc}  0
    ${rc}=            Execute Command
    ...               docker tag ${path}/${name} ${INT_REG}/bluval/${name}
    ...                 return_stdout=False  return_rc=True
    Should Be Equal As Integers  ${rc}  0
    ${rc}=            Execute Command
    ...               docker push ${INT_REG}/bluval/${name}
    ...                 return_stdout=False  return_rc=True
    Should Be Equal As Integers  ${rc}  0

Onboard Image
    ${INT_REG}=       Get Variable Value  ${INTERNAL_REGISTRY}  ${EMPTY}
    Set Test Variable  ${INT_REG}
    Return From Keyword If  $INT_REG == '${EMPTY}'
    Open Connection And Log In
    Upload To Internal Registry  ${KUBE_HUNTER['path']}  ${KUBE_HUNTER['name']}
    Set To Dictionary  ${KUBE_HUNTER}  path=${INT_REG}/bluval

Prepare Job Manifest
    Run Process       sed  -i  s|{{ image }}|${KUBE_HUNTER['path']}/${KUBE_HUNTER['name']}|g
    ...               ${CURDIR}/job.yaml

Set Scan Status
    [Arguments]       ${log}
    ${STATUS}=        Evaluate  "No vulnerabilities were found" in """${log}"""
    Set Test Variable  ${STATUS}

Delete Scan Job
    ${result}=        Run Process  kubectl  delete  job  kube-hunter
    Should Be Equal As Integers  ${result.rc}  0

Should Discover No Vulnerabilities
    Return From Keyword If  ${STATUS}
    Fail                    Vulnerabilities discovered
    ...                     non-critical

Run Scan Within Pod
    ${result}=        Run Process  kubectl  apply  -f  ${CURDIR}/job.yaml
    Should Be Equal As Integers  ${result.rc}  0
    ${result}=        Run Process  kubectl  wait  --for\=condition\=complete
    ...                            --timeout\=15m  job/kube-hunter
    Should Be Equal As Integers  ${result.rc}  0
    ${result}=        Run Process  kubectl  get  pods  --selector\=job-name\=kube-hunter
    ...                            -o  jsonpath\={.items[*].metadata.name}
    Should Be Equal As Integers  ${result.rc}  0
    ${result}=        Run Process  kubectl  logs  ${result.stdout}
    ...                 stdout=pod.log
    Copy File         pod.log  ${REPORTDIR}/
    Should Be Equal As Integers  ${result.rc}  0
    Set Scan Status  ${result.stdout}

Run Node Scan
    ${addrs}=         Get Remote Addresses
    ${result}=        Run Process  kube-hunter  --remote  @{addrs}
    ...                 stdout=node.log
    Copy File         node.log  ${REPORTDIR}/
    Should Be Equal As Integers  ${result.rc}  0
    Set Scan Status  ${result.stdout}

Run Cluster Scan
    ${addr}=          Get Cluster Address
    ${result}=        Run Process  kube-hunter  --remote  ${addr}
    ...                 stdout=cluster.log
    Copy File         cluster.log  ${REPORTDIR}/
    Should Be Equal As Integers  ${result.rc}  0
    Set Scan Status  ${result.stdout}
