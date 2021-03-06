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
Documentation     Redfish Test Framework is a tool and a model for organizing
...               and running a set of Redfish interoperability test
Resource          redfish.resource
Test Teardown     Run Keywords
...               Terminate All Processes
...               Uninstall Test Suite


*** Test Cases ***
Validate Common Use Cases
    [Setup]      Install Usecase Checkers Test Suite
    Start Suite
    Wait Until Suite Finishes
    Check Suite Results
