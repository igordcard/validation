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
Resource          variables.resource
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections

*** Variables ***
${LOG}            ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}.log


*** Test Cases ***
#Run whole ltp test suite
#    [Documentation]         Wait ~5hrs to complete 2536 tests
#    ${result}=              Run Process       ./runltp     shell=yes     cwd=/opt/ltp     stdout=${LOG}
#    Append To File          ${LOG}  ${result}${\n}
#    Sleep                   2s
#    Should Contain          ${result.stdout}   failed   0

#Run ltp syscalls test suite
#    [Documentation]         Wait ~45m for syscalls to complete
#    ${result}=              Run Process       ./runltp -f syscalls      shell=yes     cwd=/opt/ltp     stdout=${LOG}
#    Append To File          ${LOG}  ${result}${\n}
#    Sleep                   2s
#    Should Contain          ${result.stdout}   failed   0

Run ltp syscalls madvise
    [Documentation]         Wait ~1m for madvise01-10 to complete
    ${result}=              Run Process       ./runltp -f syscalls -s madvise     shell=yes     cwd=/opt/ltp     stdout=${LOG}
    Append To File          ${LOG}  ${result}${\n}
    Sleep                   2s
    Should Contain          ${result.stdout}   failed   0

*** Keywords ***
Open Connection And Log In
  Open Connection       ${HOST}
  Login                 ${ROOTUSER}     ${ROOTPSWD}

