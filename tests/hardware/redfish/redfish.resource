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
Library             JSONLibrary
Library             OperatingSystem
Library             Process


*** Variables ***
${REPORTDIR}        ${LOG_PATH}/${SUITE_NAME.replace(' ','_')}
${REDFISHDIR}       ${TEMPDIR}/Redfish


*** Keywords ***
Update Config File
    ${conf}=        Load JSON From File  ${REDFISHDIR}/framework_conf.json
    ${conf}=        Update Value To Json  ${conf}  $.password  ${BMC_PASSWORD}
    ${conf}=        Convert JSON To String  ${conf}
    Create File     ${REDFISHDIR}/framework_conf.json  ${conf}

Run Suite Against Target Node
    [Arguments]     ${ip}
    ${result}=      Run Process  python  test_framework.py
    ...                 --directory  ${REDFISHDIR}
    ...                 --rhost  ${ip}
    ...                 --user  ${BMC_USER}
    ...                 --interpreter  python
    ...                 --secure  Always
    ...               cwd=${REDFISHDIR}
    Copy Files      ${REDFISHDIR}/reports/output-*/results*.json  ${REPORTDIR}/${ip}
    Copy Files      ${REDFISHDIR}/output-*/*.html  ${REPORTDIR}/${ip}
    Should Be Equal As Integers  ${result.rc}  0
    Should Not Contain  ${result.stderr}  FAILED${\n} (Failures=

Run Usecase Checkers Suite
    FOR  ${ip}  IN  @{BMC_IP}
        Copy Directory  /opt/akraino/Redfish-Test-Framework  ${REDFISHDIR}
        Copy Directory  /opt/akraino/Redfish-Usecase-Checkers  ${REDFISHDIR}/Redfish-Usecase-Checkers
        Create Directory  ${REDFISHDIR}/reports
        Update Config File
        Run Suite Against Target Node  ${ip}
        Remove Directory    ${REDFISHDIR}  recursive=True
    END
