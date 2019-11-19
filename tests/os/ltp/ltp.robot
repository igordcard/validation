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
Documentation     Validation, robustness and stability of Linux
Library           SSHLibrary
Library           OperatingSystem
Library           BuiltIn
Library           Process
Suite Setup       Run Keywords
...               Open Connection And Log In
...               Install LTP
Test Teardown     Download Logs
Suite Teardown    Run Keywords
...               Uninstall LTP
...               Close All Connections

*** Variables ***
${FULL_SUITE}            ${SUITE_NAME.replace(' ','_')}

*** Test Cases ***
# Plese maintain shortest job first order
RunLTP syscalls madvise only
    [Documentation]         Wait ~1m for madvise01-10 to complete
    ${log} =  Set Variable  ${OUTPUT DIR}${/}${FULL_SUITE}.${TEST NAME.replace(' ','_')}.log
    ${result}=              Execute Command  yes | sudo /opt/ltp/runltp -f syscalls -s madvise
    Append To File          ${log}  ${result}${\n}
    Should Contain          ${result}    INFO: ltp-pan reported all tests PASS

RunLTP syscalls only
    [Documentation]         Wait ~45m for syscalls to complete
    ${log} =  Set Variable  ${OUTPUT DIR}${/}${FULL_SUITE}.${TEST NAME.replace(' ','_')}.log
    ${result}=              Execute Command  yes | sudo /opt/ltp/runltp -f syscalls
    Append To File          ${log}  ${result}${\n}
    Should Contain          ${result}    INFO: ltp-pan reported all tests PASS

RunLTP all tests
    [Documentation]         Wait ~5hrs to complete 2536 tests
    ${log} =  Set Variable  ${OUTPUT DIR}${/}${FULL_SUITE}.${TEST NAME.replace(' ','_')}.log
    ${result}=              Execute Command  yes | sudo /opt/ltp/runltp
    Append To File          ${log}  ${result}${\n}
    Should Contain          ${result}    INFO: ltp-pan reported all tests PASS

*** Keywords ***
Open Connection And Log In
    Open Connection        ${HOST}
    Login With Public Key  ${USERNAME}  ${SSH_KEYFILE}

Install LTP
    Put File  /opt/akraino/ltp.tar.gz  /tmp/ltp.tar.gz
    Execute Command  tar -xf /tmp/ltp.tar.gz -C /  sudo=true

Uninstall LTP
    Execute Command  rm -rf /opt/ltp  sudo=True
    Execute Command  rm /tmp/ltp.tar.gz

Download Logs
    Execute Command  chmod -R a+r /opt/ltp/output  sudo=True
    SSHLibrary.Get File  /opt/ltp/output/*  ${OUTPUT DIR}/output/
    Execute Command  rm -rf /opt/ltp/output/*  sudo=True
    Execute Command  chmod -R a+r /opt/ltp/results  sudo=True
    SSHLibrary.Get File  /opt/ltp/results/*  ${OUTPUT DIR}/results/
    Execute Command  rm -rf /opt/ltp/results/*  sudo=True