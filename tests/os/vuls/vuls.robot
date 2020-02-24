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
Library           Process

*** Variables ***
${LOG_PATH}       /opt/akraino/validation/tests/os/vuls

*** Test Cases ***
Run Vuls test
    Set Environment Variable  GOROOT  /root/go
    Set Environment Variable  GOPATH  /root/go/src
    Set Environment Variable  PATH  /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/root/go/bin:/root/go/src/bin
    Set Environment Variable  LC_ALL  en_US.UTF-8
    Set Environment Variable  LANG  en_US.UTF-8

    ${rc} =  Run And Return Rc  install -D /opt/akraino/validation/tests/os/vuls/config /root/.ssh/
    Should Be Equal As Integers  ${rc}  0

    ${rc} =  Run And Return Rc  sed -i 's/HOST/${HOST}/g' config.toml
    Should Be Equal As Integers  ${rc}  0

    ${rc} =  Run And Return Rc  sed -i 's/USERNAME/${USERNAME}/g' config.toml
    Should Be Equal As Integers  ${rc}  0

    ${rc} =  Run And Return Rc  tar xvzf db.tar.gz -C /opt/akraino/validation/tests/os/vuls/
    Should Be Equal As Integers  ${rc}  0
 
    ${rc} =  Run And Return Rc  vuls scan -config config.toml -ssh-config
    Should Be Equal As Integers  ${rc}  0

    ${rc}  ${output} =  Run And Return Rc And Output  vuls report
    Should Be Equal As Integers  ${rc}  0
    Append To File  ${LOG_PATH}/vuls.log  ${output}${\n}
