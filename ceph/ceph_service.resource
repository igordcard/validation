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
Library           BuiltIn
Library           SSHLibrary
Resource          variables.resource


*** Keywords ***
Open Connection And Log In
    Open Connection          ${HOST}
    Login With Public Key    ${USERNAME}  ${KEYFILE}

Start Ceph Manager
    [Arguments]              @{nodes}
    :FOR  ${node}  IN  @{nodes}
    \   ${result}=           Run Manager Start  ${node}

Start Ceph Monitor
    [Arguments]              @{nodes}
    :FOR  ${node}  IN  @{nodes}
    \   ${result}=           Run Monitor Start  ${node}

Start Ceph OSD
    [Arguments]              @{nodes}
    :FOR  ${node}  IN  @{nodes}
    \   ${result}=           Run Ceph OSD Start  ${node}

Kill Ceph Manager
    [Arguments]              @{nodes}
    :FOR  ${node}  IN  @{nodes}
    \   ${stdout}=           Execute Command  ${SSH_CMD} ${node} "sudo pkill -u ceph ceph-mgr"

Kill Ceph Monitor
    [Arguments]              @{nodes}
    :FOR  ${node}  IN  @{nodes}
    \   ${stdout}=           Execute Command  ${SSH_CMD} ${node} "sudo pkill -u ceph ceph-mon"

Kill Ceph OSD
    [Arguments]              @{nodes}
    :FOR  ${node}  IN  @{nodes}
    \   ${stdout}=           Execute Command  ${SSH_CMD} ${node} "sudo pkill -u ceph ceph-osd"

Ceph Should Be Healthy
    ${stdout}=               Execute Command  ceph health --connect-timeout 20  sudo=True
    Should Contain  ${stdout}  HEALTH_OK

Ceph Health Should Be Degraded
    ${stdout}=               Execute Command  ceph health --connect-timeout 20  sudo=True
    Should Contain  ${stdout}  HEALTH_WARN

Number Of OSDs Up
    ${stdout}=               Execute Command  ceph -f json osd stat  sudo=True
    ${stat}=                 Evaluate  json.loads($stdout)  json
    [Return]                 ${stat["num_up_osds"]}

Number Of OSDs Up Should Be
    [Arguments]              ${num_expected}
    ${num_up_osds}=          Number Of OSDs Up
    Should Be Equal          ${num_up_osds}  ${num_expected}

Run Manager Start
    [Arguments]              ${node}
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl start ceph-mgr@${node}"
    ${stdout}=               Execute Command  ${SSH_CMD} ${node} "systemctl is-active ceph-mgr@${node}"
    Return From Keyword If   '${stdout}' == 'active'
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl reset-failed ceph-mgr@${node}"
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl start ceph-mgr@${node}"
    ${stdout}=               Execute Command  ${SSH_CMD} ${node} "systemctl is-active ceph-mgr@${node}"
    Should Be Equal As Strings  ${stdout}  active

Run Monitor Start
    [Arguments]              ${node}
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl start ceph-mon@${node}"
    ${stdout}=               Execute Command  ${SSH_CMD} ${node} "systemctl is-active ceph-mon@${node}"
    Return From Keyword If   '${stdout}' == 'active'
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl reset-failed ceph-mon@${node}"
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl start ceph-mon@${node}"
    ${stdout}=               Execute Command  ${SSH_CMD} ${node} "systemctl is-active ceph-mon@${node}"
    Should Be Equal As Strings  ${stdout}  active

Run Ceph OSD Start
    [Arguments]              ${node}
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl -a start ceph-osd@*"
    ${stdout}=               Execute Command  ${SSH_CMD} ${node} "systemctl -a is-active ceph-osd@*"
    Return From Keyword If   '${stdout}' == 'active'
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl -a reset-failed ceph-osd@*"
    Execute Command          ${SSH_CMD} ${node} "sudo systemctl -a start ceph-osd@*"
    ${stdout}=               Execute Command  ${SSH_CMD} ${node} "systemctl -a is-active ceph-osd@*"
    Should Be Equal As Strings  ${stdout}  active
