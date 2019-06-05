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
Documentation     HP Bare metal HW test cases to verify configuration
...               from given blueprint
Library           OperatingSystem
Library           BuiltIn
Library           Process

*** Variables ***
${LOGHWHP}        ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}.log

*** Test Cases ***
Verify cluster connectivity
    [Documentation]    Wait a few seconds to prove connectivity
    @{nodes}  Create List  ${HOST_MR}  ${HOST_WR1}  ${HOST_WR2}  ${HOST_WR3}  ${HOST_WR4}
    FOR  ${node}  IN  @{nodes}
         ${output}=        Run    ping ${node} -c 3
         Append To File    ${LOGHWHP}  ${output}${\n}
         Should Contain    ${output}    3 packets transmitted, 3 received
    END

Verify hw health status
    [Documentation]   HW health should be ok
    ${output}=        Run    curl ${BASE_URI} -k | python -m json.tool | grep -A4 '"System":'
    Append To File    ${LOGHWHP}  ${output}${\n}
    Should Contain    ${output}    OK

Verify chassis details
    [Documentation]   Data should match chassis input
    ${output}=        Run
    ...   curl --user ${IPMIUSER}:${IPMIPWRD} ${BASE_URI}Chassis/1/ -k | python -m json.tool | grep "SerialNumber"
    Append To File    ${LOGHWHP}  ${output}${\n}
    Should Contain    ${output}    ${CHASSIS}

Verify iDRAC settings
    [Documentation]   Data should match idrac input
    ${output}=        Run
    ...   curl --user ${IPMIUSER}:${IPMIPWRD} ${BASE_URI}Managers/1/EthernetInterfaces/1/ -k | python -m json.tool | grep -A2 IPv4Addresses
    Append To File    ${LOGHWHP}  ${output}${\n}
    Should Contain    ${output}    ${IDRACIP}

Verify boot registry
    [Documentation]   Data should match boot input
    ${output}=        Run
    ...   curl --user ${IPMIUSER}:${IPMIPWRD} ${BASE_URI}Systems/1/ -k | python -m json.tool | grep BootSourceOverrideMode
    Append To File    ${LOGHWHP}  ${output}${\n}
    Should Contain    ${output}    ${BOOTSEQ}

Verify bios version
    [Documentation]   Data should match bios input
    ${output}=        Run
    ...   curl --user ${IPMIUSER}:${IPMIPWRD} ${BASE_URI}Systems/1/ -k | python -m json.tool | grep BiosVersion
    Append To File    ${LOGHWHP}  ${output}${\n}
    Should Contain    ${output}    ${BIOSVER}

Verify firmware version
    [Documentation]   Data should match firmware input
    ${output}=        Run
    ...   curl --user ${IPMIUSER}:${IPMIPWRD} ${BASE_URI}Managers/1/ -k | python -m json.tool | grep "FirmwareVersion"
    Append To File    ${LOGHWHP}  ${output}${\n}
    Should Contain    ${output}   ${FIRMWARE}
