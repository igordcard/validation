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
Library            OperatingSystem
Library            Process
Library            SSHLibrary
Library            String


*** Variables ***
${REPORTDIR}       ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}
${SRCDIR}          /opt/akraino/docker-bench-security
${DESTDIR}         /tmp/docker-bench-security
${NODEDIR}         /tmp/docker-bench-security-run
${SSH_OPTS}        -o StrictHostKeyChecking=no


*** Keywords ***
Open Connection And Log In
    Open Connection        ${HOST}
    Login With Public Key  ${USERNAME}  ${SSH_KEYFILE}

Upload Test Software To Nodes
    Put Directory          ${SRCDIR}  ${DESTDIR}  recursive=True
    Get Node Addresses
    Copy Test Software To All Nodes

Run Test Software On Nodes
    :FOR  ${node}  IN  @{nodes}
    \   Execute Command   ssh ${SSH_OPTS} ${node} "cd ${NODEDIR}; sudo ./docker-bench-security.sh -b -l bench.log"
    \   Execute Command   scp ${SSH_OPTS} ${node}:${NODEDIR}/bench.log ${DESTDIR}/docker-bench-${node}.log
    \   Execute Command   scp ${SSH_OPTS} ${node}:${NODEDIR}/bench.log.json ${DESTDIR}/docker-bench-${node}.json
    \   SSHLibrary.Get File  ${DESTDIR}/docker-bench-${node}.log  ${REPORTDIR}/
    \   SSHLibrary.Get File  ${DESTDIR}/docker-bench-${node}.json  ${REPORTDIR}/

Get Node Addresses
    ${stdout}=            Execute Command
    ...                   kubectl get nodes -o jsonpath='{.items[*].status.addresses[?(@.type=="InternalIP")].address'}
    @{nodes}=             Split String  ${stdout}
    Set Test Variable     @{nodes}

Copy Test Software To All Nodes
    :FOR  ${node}  IN  @{nodes}
    \   Execute Command   ssh ${SSH_OPTS} ${node} "mkdir -p ${NODEDIR}"
    \   Execute Command   scp ${SSH_OPTS} -rp ${DESTDIR}/. ${node}:${NODEDIR}

Remove Test Software From Nodes
    :FOR  ${node}  IN  @{nodes}
    \   Execute Command   ssh ${SSH_OPTS} ${node} "rm -rf ${NODEDIR}"
    Execute Command       rm -rf ${DESTDIR}
