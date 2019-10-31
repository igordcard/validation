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
Library           OperatingSystem
Library           BuiltIn
Library           SSHLibrary
Documentation     Cyclic latency test
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections

*** Variables ***
${LOG}             ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}.log
${SSH_KEYFILE}     /root/.ssh/id_rsa


*** Test Cases ***
Latency Test
    ${output}  ${rc}=      Execute Command    cyclictest --mlockall --smp --priority=80 --interval=200 --distance=0 --duration=2    return_rc=True    sudo=True
    Append To File         ${LOG}  ${output}${\n}
    Should Be Equal As Integers  ${rc}  0


*** Keywords ***
Open Connection And Log In
  Open Connection       ${HOST}
  Login With Public Key    ${USERNAME}   ${SSH_KEYFILE}

