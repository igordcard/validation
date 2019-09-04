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
Documentation       Tests to validate Helm chart repositories.
Resource            helm_repository.resource
Suite Setup         Run Keywords
...                 Open Connection And Log In
...                 Build Chart Archive
...                 Get Default Repository
Suite Teardown      Close All Connections


*** Test Cases ***
Chart Storing
    Upload Chart to Repository
    Chart Upload Should Have Succeeded
    Update Repository Info
    Find Chart In Repository
    Chart Should Be Available
    Inspect Chart
    Chart Should Be Accessible

Upload Already Uploaded Chart
    [Setup]  Fail If Previous Test Failed
    Upload Chart to Repository
    Chart Upload Should Have Failed

Chart Removal
    Delete Chart
    Chart Delete Should Have Succeeded
    Update Repository Info
    Find Chart In Repository
    Chart Should Not Be Available

Delete Already Deleted Chart
    [Setup]  Fail If Previous Test Failed
    Delete Chart
    Chart Delete Should Have Failed
