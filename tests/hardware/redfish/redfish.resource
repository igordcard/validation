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
Library             Collections
Library             JSONLibrary
Library             OperatingSystem
Library             Process


*** Variables ***
${REPORTDIR}        ${LOG_PATH}/${SUITE_NAME.replace(' ','_')}
${REDFISHDIR}       ${TEMPDIR}/Redfish


*** Keywords ***
Update Config File
    [Arguments]     ${config_file}
    ${conf}=        Load JSON From File  ${config_file}
    ${conf}=        Update Value To Json  ${conf}  $.password  ${BMC_PASSWORD}
    ${conf}=        Convert JSON To String  ${conf}
    Create File     ${config_file}  ${conf}

Run Suite Against Target Node
    [Arguments]     ${ip}
    Start Process   python  test_framework.py
    ...                 --directory  ${REDFISHDIR}/${ip}
    ...                 --rhost  ${ip}
    ...                 --user  ${BMC_USER}
    ...                 --interpreter  python
    ...                 --secure  Always
    ...               cwd=${REDFISHDIR}/${ip}
    ...               alias=${ip}
    Process Should Be Running

Install Usecase Checkers Test Suite
    @{BMC_IP}=      Remove Duplicates  ${BMC_IP}
    Set Test Variable  @{BMC_IP}
    FOR  ${ip}  IN  @{BMC_IP}
        Copy Directory  /opt/akraino/Redfish-Test-Framework
        ...             ${REDFISHDIR}/${ip}
        Copy Directory  /opt/akraino/Redfish-Usecase-Checkers
        ...             ${REDFISHDIR}/${ip}/Redfish-Usecase-Checkers
        Create Directory  ${REDFISHDIR}/${ip}/reports
        Update Config File  ${REDFISHDIR}/${ip}/framework_conf.json
    END

Uninstall Test Suite
    Remove Directory  ${REDFISHDIR}  recursive=True

Start Suite
    @{ips}=         Create List
    Set Test Variable  @{ips}
    FOR  ${ip}  IN  @{BMC_IP}
        Run Suite Against Target Node  ${ip}
        Append To List  ${ips}  ${ip}
    END

Suite Finished
    @{tmp}=         Copy List  ${ips}
    FOR  ${ip}  IN  @{tmp}
        ${result}=  Wait For Process  ${ip}  timeout=1ms
        Continue For Loop If  '${result}' == '${NONE}'
        Remove Values From List  ${ips}  ${ip}
        Copy Files  ${REDFISHDIR}/${ip}/reports/output-*/results*.json  ${REPORTDIR}/${ip}
        Copy Files  ${REDFISHDIR}/${ip}/output-*/*.html  ${REPORTDIR}/${ip}
    END
    Should Be Empty  ${ips}

Wait Until Suite Finishes
    Wait Until Keyword Succeeds  45m  15s  Suite Finished

Check Suite Results
    FOR  ${ip}  IN  @{BMC_IP}
        ${result}=  Get Process Result  ${ip}
        Should Be Equal As Integers  ${result.rc}  0
        Should Not Contain  ${result.stderr}  FAILED${\n} (Failures=
    END
