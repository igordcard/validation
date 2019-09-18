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
Documentation     Test to verify the recovery and health of etcd cluster
...               If the etcd node command line supports etcdctl3 then pass ${ETCD_VERSION} as "3"
...               If the etcd node command line supports etcdctl then pass ${ETCD_VERSION} as "${EMPTY}"
Resource          etcd_ha.resource


*** Test Cases ***
Failure Of Etcd Node
    Retrieve Etcd Config
    Etcd Cluster Should Be Healthy
    Delete Etcd Node
    Wait For Etcd Node To Recover
    Etcd Cluster Should Be Healthy
