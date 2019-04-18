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
Documentation     HA tests: etcd, api-server, controller-manager, scheduler
Library           SSHLibrary
Library           OperatingSystem
Library           BuiltIn
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections

*** Variables ***
${HOST}           localhost
${USERNAME}       localadmin
${NODENAME}       aknode109
${LOG}            /opt/akraino/validation/ha/print_etcd_api_ctl-manager_sch.txt


## kubernetes-etcd

*** Test Cases ***
Verify etcd status
        [Documentation]         etcd label defined
        ${output}=              Execute Command        date
        Append To File          ${LOG}  ${output}${\n}
        Start Command           kubectl describe node ${NODENAME} | egrep "Labels|etcd"    sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-etcd=enabled


Failure of etcd in node
        [Documentation]         kubernetes etcd removed
        Start Command           kubectl label node ${NODENAME} kubernetes-etcd-    sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   3s
        Should Contain          ${stdout}              labeled
        Sleep                   40s
        ${output}=              Execute Command        kubectl describe node ${NODENAME} | egrep "Labels|etcd"    sudo=True
        Append To File          ${LOG}  ${output}${\n}


Enable etcd in node
        [Documentation]         etcd label re-established
        Start Command           kubectl label node ${NODENAME} kubernetes-etcd=enabled    sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   5s
        Should Contain          ${stdout}              labeled
        Sleep                   40s
        ${output}=              Execute Command        kubectl describe node ${NODENAME} | egrep "Labels|etcd"     sudo=True
        Append To File          ${LOG}  ${output}${\n}


Verify etcd health
        [Documentation]         etcd endpoint healthy
        Sleep                   25s
        Start Command           kubectl exec -it -n kube-system kubernetes-etcd-${NODENAME} etcdctl endpoint health      sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   5s
        Should Contain          ${stdout}              is healthy: successfully committed


Check etcd node list
        [Documentation]         member list started
        Start Command           kubectl exec -it -n kube-system kubernetes-etcd-${NODENAME} etcdctl member list      sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Should Contain          ${stdout}              started, ${NODENAME}

## kubernetes-api


Verify api-server status
        [Documentation]         kubernetes api-server state
        Start Command           kubectl describe node ${NODENAME} | egrep 'Label|kubernetes-apiserver'     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-apiserver=enabled


Failure of api-server in node
        [Documentation]         kubernetes api-server removed
        Start Command           kubectl label node ${NODENAME} kubernetes-apiserver-     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   3s
        Should Contain          ${stdout}              labeled
        Sleep                   40s
        ${output}=              Execute Command        kubectl describe node ${NODENAME} | egrep 'Label|kubernetes-apiserver'     sudo=True
        Append To File          ${LOG}  ${output}${\n}


Enable api-server in node
        [Documentation]         api-serverd label re-established
        Start Command           kubectl label node ${NODENAME} kubernetes-apiserver=enabled      sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   5s
        Should Contain          ${stdout}              labeled
        Sleep                   30s


Check api-server status
        [Documentation]         kubernetes api-server re-established
        Start Command           kubectl describe node ${NODENAME} | egrep 'Label|kubernetes-apiserver'     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-apiserver=enabled

##kubernetes controller-manager

Verify controller-manager status
        [Documentation]         kubernetes controller-manager state
        Start Command           kubectl describe node ${NODENAME} | egrep 'Label|controller-manager'     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-controller-manager=enabled


Failure of controller-manager in node
        [Documentation]         kubernetes controller removed
        Start Command           kubectl label node ${NODENAME} kubernetes-controller-manager-     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   3s
        Should Contain          ${stdout}              labeled
        Sleep                   40s
        ${output}=              Execute Command        kubectl describe node ${NODENAME} | egrep 'Label|controller-manager'     sudo=True
        Append To File          ${LOG}  ${output}${\n}


Enable controller-manager in node
        [Documentation]         controller-manager re-established
        Start Command           kubectl label node ${NODENAME} kubernetes-controller-manager=enabled      sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   5s
        Should Contain          ${stdout}              labeled
        Sleep                   30s


Check controller-manager status
        [Documentation]         kubernetes controller-manager re-established
        Start Command           kubectl describe node ${NODENAME} | egrep 'Label|controller-manager'     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-controller-manager=enabled

## kubernetes-scheduler

Verify k8s-scheduler status
        [Documentation]         kubernetes scheduler state
        Start Command           kubectl describe node ${NODENAME} | egrep 'Label|scheduler'     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-scheduler=enabled


Failure of scheduler in node
        [Documentation]         kubernetes scheduler removed
        Start Command           kubectl label node ${NODENAME} kubernetes-scheduler-     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   3s
        Should Contain          ${stdout}              labeled
        Sleep                   40s
        ${output}=              Execute Command        kubectl describe node ${NODENAME} | egrep 'Label|scheduler'     sudo=True
        Append To File          ${LOG}  ${output}${\n}


Enable scheduler in node
        [Documentation]         scheduler re-established
        Start Command           kubectl label node ${NODENAME} kubernetes-scheduler=enabled      sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   5s
        Should Contain          ${stdout}              labeled
        Sleep                   30s


Check k8s scheduler status
        [Documentation]         kubernetes scheduler re-established
        Start Command           kubectl describe node ${NODENAME} | egrep 'Label|scheduler'     sudo=True
        ${stdout}=              Read Command Output
        Append To File          ${LOG}  ${stdout}${\n}
        Sleep                   2s
        Should Contain          ${stdout}              kubernetes-scheduler=enabled


*** Keywords ***
Open Connection And Log In
  Open Connection       ${HOST}
  Login With Public Key    ${USERNAME}  /root/.ssh/${USERNAME}_id_rsa

