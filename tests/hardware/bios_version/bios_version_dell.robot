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
#${BIOS_REVISION}   1.3
${LOG}            /opt/akraino/validation/bios_version/print_bios.txt

*** Test Cases ***
Get HW Details
        [Documentation]         Verify HW details
        Start Command           dmidecode | grep -A3 '^System Information'   sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}               ${SYSINFO}

Verify BIOS Revision
        [Documentation]         Verify BIOS Revision
        Start Command           dmidecode | more | grep 'BIOS Revision'    sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}               BIOS Revision: ${BIOS_REVISION}

Check NUMA and CPU
        [Documentation]         NUMAs and CPU components
        ${output}=              Execute Command         lscpu
        Append To File          ${LOG}  ${output}${\n}
        Should Contain          ${output}       CPU(s):                88

Verify Block Devices
        [Documentation]         Reads the sysfs filesystem
        ${output}=              Execute Command         lsblk
        Append To File          ${LOG}  ${output}${\n}
        Should Contain          ${output}       sdg4

*** Keywords ***
Open Connection And Log In
  Open Connection       ${HOST}
  Login With Public Key    ${USERNAME}  /root/.ssh/${USERNAME}_id_rsa

