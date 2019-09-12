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
Library             OperatingSystem
Library             Process
Library             SSHLibrary
Library             String


*** Variables ***
${REPORTDIR}        ${LOG_PATH}${/}${SUITE_NAME.replace(' ','_')}
${DESTDIR}          /tmp/helm-repo
${REPONAME}         default
${REPOURL}          Actual value set dynamically at suite setup
${CHARTARC}         Actual value set dynamically at suite setup
${CHARTNAME}        bluval-test-alpine


*** Keywords ***
Open Connection And Log In
    Open Connection         ${HOST}
    Login With Public Key   ${USERNAME}  ${SSH_KEYFILE}

Build Chart Archive
    Run Process             sed  -i  s/name: alpine/name: ${CHARTNAME}/  /opt/akraino/${CHARTNAME}/Chart.yaml
    Execute Command         rm -r ${DESTDIR}
    Put Directory           /opt/akraino/${CHARTNAME}  ${DESTDIR}/${CHARTNAME}  recursive=True
    ${stdout}=              Execute Command  helm package ${DESTDIR}/${CHARTNAME} -d ${DESTDIR}
    Should Contain          ${stdout}  Successfully packaged chart and saved it to:
    ${CHARTARC}=            Fetch From Right  ${stdout}  /
    Set Suite Variable      ${CHARTARC}

Get Default Repository
    ${REPOURL}=             Execute Command
    ...                     helm repo list | grep '^${REPONAME}\\([[:blank:]]\\|[^[:print:]]\\)' | cut -f2
    Set Suite Variable      ${REPOURL}
    Should Not Be Empty     ${REPOURL}

Fail If Previous Test Failed
    Run Keyword If          '${PREV_TEST_STATUS}'=='FAIL'  Fail  Skipping because '${PREV_TEST_NAME}' failed.

Upload Chart To Repository
    ${STATUS}=              Execute Command  curl -X PUT --data-binary "@${DESTDIR}/${CHARTARC}" ${REPOURL}/${CHARTARC}
    Set Test Variable       ${STATUS}

Chart Upload Should Have Succeeded
    Should Be Empty         ${STATUS}

Chart Upload Should Have Failed
    Should Not Be Empty     ${STATUS}

Update Repository Info
    ${stdout}=              Execute Command  helm repo update
    Should Contain          ${stdout}  Successfully got an update from the "${REPONAME}" chart repository

Inspect Chart
    ${STATUS}=              Execute Command  helm inspect chart ${REPONAME}/${CHARTNAME}
    ...                       return_stdout=False
    ...                       return_rc=True
    Set Test Variable       ${STATUS}

Find Chart In Repository
    ${STATUS}=              Execute Command  helm search -r '\\v${REPONAME}/${CHARTNAME}\\v'
    Set Test Variable       ${STATUS}

Chart Should Be Available
    Should Contain          ${STATUS}  ${REPONAME}/${CHARTNAME}

Chart Should Not Be Available
    Should Contain          ${STATUS}  No results found

Chart Should Be Accessible
    Should Be Equal As Integers  ${STATUS}  0

Delete Chart
    ${STATUS}=              Execute Command  curl -X DELETE ${REPOURL}/${CHARTARC}
    Set Test Variable       ${STATUS}

Chart Delete Should Have Succeeded
    Should Be Empty         ${STATUS}

Chart Delete Should Have Failed
    Should Not Be Empty     ${STATUS}
