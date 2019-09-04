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
Library            BuiltIn
Library            Collections
Library            OperatingSystem
Library            Process
Library            String


*** Variables ***
${REPORTDIR}       ${LOG_PATH}/${SUITE_NAME.replace(' ','_')}
${WORKSPACE}       sut
${WORKDIR}         ${TEMPDIR}/tempest-run/${WORKSPACE}


*** Keywords ***
Setup Workspace
   Create Directory    ${WORKDIR}
   ${result}=          Run Process  tempest  init  --name  ${WORKSPACE}  ${WORKDIR}
   Should Be Equal As Integers  ${result.rc}  0

Remove Workspace
   Run Process         tempest  workspace  remove  --name  ${WORKSPACE}
   Remove Directory    ${WORKDIR}  recursive=True

Create Tempest Configuration File
   ${env}=             Get Deployment Credential Variables
   ${result}=          Run Process  discover-tempest-config
   ...                     --create
   ...                     --out  ${WORKDIR}/etc/tempest.conf
   ...                     DEFAULT.log_dir  ${WORKDIR}/logs
   ...                     DEFAULT.log_file  tempest.log
   ...                     oslo_concurrency.lock_path  ${WORKDIR}/tempest_lock
   ...                     auth.use_dynamic_credentials  true
   ...                   env=${env}
   Should Be Equal As Integers  ${result.rc}  0

Get Deployment Credential Variables
   ${env}=             Get Environment Variables
   ${openrc}=          Get File  /root/openrc
   ${str}=             Get Lines Matching Regexp  ${openrc}  ^export .*
   @{lines}=           Split To Lines  ${str}
   :FOR  ${line}  IN  @{lines}
   \   ${str}=             Remove String Using Regexp  ${line}  ^export${SPACE}
   \   ${key}  ${value}    Split String  ${str}  separator==
   \   Set To Dictionary   ${env}  ${key}=${value}
   Should Not Be Empty  ${env}
   [Return]            ${env}

Run Tempest Refstack Tests
   ${result}=          Run Process  tempest  run
   ...                     --workspace  ${WORKSPACE}
   ...                     --load-list  ${CURDIR}/test_list.txt
   ...                     --blacklist-file  ${CURDIR}/blacklist.txt
   ...                     --concurrency  4
   ...                   stdout=${WORKDIR}/logs/tempest_run.log
   ...                   stderr=STDOUT
   Copy Files          ${WORKDIR}/logs/*  ${REPORTDIR}/
   Should Contain      ${result.stdout}  - Failed: 0
