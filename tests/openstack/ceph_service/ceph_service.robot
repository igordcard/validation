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
Documentation     Tests to verify the availability and recovery of failed
...               Ceph services
Resource          ceph_service.resource
Suite Setup       Open Connection And Log In
Suite Teardown    Close All Connections


*** Test Cases ***
Failure Of Single Monitor And Manager
    @{nodes}  Create list  ${NODENAME}-1
    Kill Ceph Monitor  @{nodes}
    Kill Ceph Manager  @{nodes}
    Sleep  5s
    Ceph Health Should Be Degraded
    Start Ceph Monitor  @{nodes}
    Start Ceph Manager  @{nodes}
    Sleep  10s
    Ceph Should Be Healthy

Failure Of Two Monitors And Managers
    @{nodes}  Create list  ${NODENAME}-1  ${NODENAME}-2
    Kill Ceph Monitor  @{nodes}
    Kill Ceph Manager  @{nodes}
    Sleep  5s
    Start Ceph Monitor  @{nodes}
    Start Ceph Manager  @{nodes}
    Sleep  10s
    Ceph Should Be Healthy

Failure Of Single Object Storage Daemon
    @{nodes}  Create list  ${NODENAME}-1
    ${num_up_osds}   Number Of OSDs Up
    Kill Ceph OSD  @{nodes}
    Sleep  5s
    Number Of OSDs Up Should Be  ${num_up_osds-1}
    Ceph Health Should Be Degraded
    Start Ceph OSD  @{nodes}
    Sleep  10s
    Number Of OSDs Up Should Be  ${num_up_osds}
    Ceph Should Be Healthy

Failure Of Two Object Storage Daemons
    @{nodes}  Create list  ${NODENAME}-1  ${NODENAME}-2
    ${num_up_osds}   Number Of OSDs Up
    Kill Ceph OSD  @{nodes}
    Sleep  5s
    Number Of OSDs Up Should Be  ${num_up_osds-2}
    Ceph Health Should Be Degraded
    Start Ceph OSD  @{nodes}
    Sleep  10s
    Number Of OSDs Up Should Be  ${num_up_osds}
    Ceph Should Be Healthy
