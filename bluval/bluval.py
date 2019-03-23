#!/usr/bin/python3
##############################################################################
# Copyright (c) 2019 AT&T Intellectual Property.                             #
# Copyright (c) 2019 Nokia.                                                  #
#                                                                            #
# Licensed under the Apache License, Version 2.0 (the "License"); you may    #
# not use this file except in compliance with the License.                   #
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
"""This module parses yaml file, reads sections, testcases and executes each
testcase
"""

import subprocess
import sys
import yaml

def run_testcase(testcase):
    """Runs a single testcase
    """
    show_stopper = testcase.get('show_stopper', False)

    print('Executing testcase {}'.format(testcase['name']))
    print('          show_stopper {}'.format(show_stopper))
    cmd = 'robot {}'.format(testcase['what'])
    print('Invoking {}'.format(cmd))
    try:
        status = subprocess.call(cmd, shell=True)
        if status != 0 and testcase['show_stopper']:
            print('Show stopper testcase failed')
            return status
    except OSError:
        print('Error while executing {}'.format(cmd))
        return -1
    return status


def parse_yaml(testcase_loc):
    """Parse yaml file and do tasks on each testcase
    """
    with open(testcase_loc) as testcase_file:
        testcases = yaml.safe_load(testcase_file)
    blueprint = testcases['blueprint']
    for section in blueprint['sections']:
        print('## Section {}'.format(section))
        for testcase in blueprint[section]:
            run_testcase(testcase)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print('usage: bluval.py <testcase.yaml>')
        sys.exit(1)
    parse_yaml(sys.argv[1])
