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
Documentation     Check BIOS and HW details
Library           SSHLibrary
Library           OperatingSystem
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections

*** Variables ***
#${HOST}           localhost
#${USERNAME}       localadmin
#${SYSINFO}        PowerEdge R740xd
#${BIOS_REVISION}  1.3
#${BLK_DEV_REGEXP} ([sh]d[a-z]+|nvme)[0-9]+
${SSH_KEYFILE}     /root/.ssh/id_rsa
${LOG}             ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}.log

*** Test Cases ***
Get HW Details
        [Documentation]         Verify HW details
        Start Command           cat /sys/class/dmi/id/product_name
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}               ${SYSINFO}

Verify BIOS Revision
        [Documentation]         Verify BIOS Revision
        Start Command           printf "BIOS Revision: %u.%u" $(sudo od -t u1 -j 20 -N 2 -A none /sys/firmware/dmi/tables/DMI)
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}               BIOS Revision: ${BIOS_REVISION}

Check NUMA and CPU
        [Documentation]         NUMAs and CPU components
        ${output}=              Execute Command         lscpu
        Append To File          ${LOG}  ${output}${\n}
        Should Match Regexp     ${output}               CPU\\(s\\):\\s+\\d+

Verify Block Devices
        [Documentation]         Reads the sysfs filesystem
        ${output}=              Execute Command         lsblk
        Append To File          ${LOG}  ${output}${\n}
        Should Match Regexp     ${output}               ${BLK_DEV_REGEXP}

*** Keywords ***
Open Connection And Log In
        Open Connection         ${HOST}
        Login With Public Key   ${USERNAME}  ${SSH_KEYFILE}
