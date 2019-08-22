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
Library            SSHLibrary
Library            String


*** Variables ***
${REPORTDIR}       ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}
${CHARTDIR}        /tmp/helm-chart
${SERVECMD}        helm serve --repo-path /home/${USERNAME}/.helm/repository/local


*** Keywords ***
Open Connection And Log In
    Open Connection        ${HOST}
    Login With Public Key  ${USERNAME}  ${SSH_KEYFILE}

Start Local Chart Repository Server
    Stop Local Chart Repository Server
    Start Command          ${SERVECMD}
    ${rc}=                 Execute Command  pgrep -xf "${SERVECMD}"
    ...                      return_stdout=False
    ...                      return_rc=True
    Should Be Equal As Integers  ${rc}  0

Stop Local Chart Repository Server
    Execute Command  pkill -xf "${SERVECMD}"

List Charts In Repositories
    ${stdout}=             Execute Command  helm search --regexp . | tail -n +2 | cut -f1
    @{CHARTS}=             Split String  ${stdout}
    Set Suite Variable     @{CHARTS}

Fetch Charts
    Execute Command        rm -r ${CHARTDIR}
    Execute Command        mkdir -p ${CHARTDIR}
    :FOR  ${chart}  IN  @{CHARTS}
    \   ${rc}=             Execute Command  helm fetch ${chart} -d ${CHARTDIR}
        ...                  return_stdout=False
        ...                  return_rc=True
    \   Should Be Equal As Integers  ${rc}  0

Lint Charts
    @{files}=              List Files In Directory  ${CHARTDIR}
    :FOR  ${file}  IN  @{files}
    \   ${stdout}=  Execute Command  helm lint ${CHARTDIR}/${file}
    \   Should Contain  ${stdout}  1 chart(s) linted, no failures

Simulate Install
    :FOR  ${chart}  IN  @{CHARTS}
    \   ${rc}=             Execute Command  helm install --dry-run ${chart}
        ...                  return_stdout=False
        ...                  return_rc=True
    \   Should Be Equal As Integers  ${rc}  0
