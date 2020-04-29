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
Library           SSHLibrary
Library           OperatingSystem
Library           BuiltIn
Suite Setup       Open Connection And Log In
Suite Teardown    Run Keywords
...               Cleanup ssh
...               Close All Connections

*** Test Cases ***
Run Vuls test
    Set Environment Variable  GOROOT  /root/go
    Set Environment Variable  GOPATH  /root/go/src
    Set Environment Variable  PATH  /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/root/go/bin:/root/go/src/bin
    Set Environment Variable  LC_ALL  en_US.UTF-8
    Set Environment Variable  LANG  en_US.UTF-8

    ${rc} =  Run And Return Rc  install -D ${CURDIR}/config /root/.ssh/
    Should Be Equal As Integers  ${rc}  0

    ${rc} =  Run And Return Rc  sed -i -e 's/HOST/${HOST}/g' -e 's/USERNAME/${USERNAME}/g' ${CURDIR}/config.toml
    Should Be Equal As Integers  ${rc}  0

    ${rc} =  Run And Return Rc  tar xvzf ${CURDIR}/db.tar.gz -C ${CURDIR}
    Should Be Equal As Integers  ${rc}  0

    ${os} =  SSHLibrary.Execute Command   source /etc/os-release && echo $ID

    Run Keyword IF  '${SSH_KEYFILE}' == 'None'  Create ssh_keyfile

    ${rc} =  Run And Return Rc  vuls scan -config ${CURDIR}/config.toml -ssh-config
    Should Be Equal As Integers  ${rc}  0

    Run Keyword IF  '${os}' == 'ubuntu'  Run vuls for ubuntu  ELSE IF  '${os}' == 'centos'  Run vuls for centos  ELSE  FAIL  Distro '${os}' not supported

    ${status} =  Evaluate  "Total: 0" in """${LOG}"""
    Run Keyword If  '${status}' == 'False'  FAIL  Vulnerabilities discovered
    ...                     non-critical

*** Keywords ***
Run vuls for ubuntu
    ${os_version} =  SSHLibrary.Execute Command  source /etc/os-release && echo $VERSION_ID | cut -d '.' -f1

    ${rc}  ${output} =  Run And Return Rc And Output  vuls report -config ${CURDIR}/config.toml -cvedb-sqlite3-path=${CURDIR}/cve.sqlite3 -ovaldb-sqlite3-path=${CURDIR}/oval_ubuntu_${os_version}.sqlite3
    Should Be Equal As Integers  ${rc}  0
    Append To File  ${LOG_PATH}/vuls.log  ${output}${\n}
    Set Global Variable  ${LOG}  ${output}

Run vuls for centos
    ${rc}  ${output} =  Run And Return Rc And Output  vuls report -config ${CURDIR}/config.toml -cvedb-sqlite3-path=${CURDIR}/cve.sqlite3 -ovaldb-sqlite3-path=${CURDIR}/oval_centos.sqlite3 -gostdb-sqlite3-path=${CURDIR}/gost_centos.sqlite3
    Should Be Equal As Integers  ${rc}  0
    Append To File  ${LOG_PATH}/vuls.log  ${output}${\n}
    Set Global Variable  ${LOG}  ${output}

Create ssh_keyfile
    ${rc} =  Run And Return Rc  ssh-keygen -t rsa -b 4096 -f /root/.ssh/id_rsa -N ""
    Should Be Equal As Integers  ${rc}  0

    ${rc} =  Run and Return Rc  sshpass -p '${PASSWORD}' ssh-copy-id -i /root/.ssh/id_rsa.pub '${USERNAME}'@'${HOST}'
    Should Be Equal As Integers  ${rc}  0

Cleanup ssh
    ${rc}  ${idssh} =  Run And Return Rc And Output  cat /root/.ssh/id_rsa.pub
    Should Be Equal As Integers  ${rc}  0
    ${rc} =  Run And Return Rc  ssh '${USERNAME}'@'${HOST}' "sed -i 's#${idssh}##' ~/.ssh/authorized_keys"
    Should Be Equal As Integers  ${rc}  0

Open Connection And Log In
    Open Connection  ${HOST}
    Run Keyword IF  '${SSH_KEYFILE}' != 'None'  Login With Public Key  ${USERNAME}  ${SSH_KEYFILE}  ELSE IF  '${PASSWORD}' != 'None'  Login  ${USERNAME}  ${PASSWORD}  ELSE  FAIL
