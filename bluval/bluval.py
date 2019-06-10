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
"""This module parses yaml file, reads layers, testcases and executes each
testcase
"""

import subprocess
from pathlib import Path
import click
import yaml

def run_testcase(testcase):
    """Runs a single testcase
    """
    show_stopper = testcase.get('show_stopper', False)
    what = testcase.get('what')
    mypath = Path(__file__).absolute()
    results_path = mypath.parents[2].joinpath("results/"+testcase.get('layer')+"/"+what)
    test_path = mypath.parents[1].joinpath("tests/"+testcase.get('layer')+"/"+what)

    # add to the variables file the path to where to sotre the logs
    variables_file = mypath.parents[1].joinpath("tests/variables.yaml")
    variables_dict = yaml.safe_load(variables_file.open())
    variables_dict['log_path'] = str(results_path)
    variables_file.write_text(str(variables_dict))

    # run the test
    args = ["robot", "-V", str(variables_file), "-d", str(results_path), str(test_path)]

    print('Executing testcase {}'.format(testcase['name']))
    print('          show_stopper {}'.format(show_stopper))
    print('Invoking {}'.format(args))
    try:
        status = subprocess.call(args, shell=False)
        if status != 0 and show_stopper:
            print('Show stopper testcase failed')
            return status
    except OSError:
        print('Error while executing {}'.format(args))
        return -1
    return status


def validate_layer(blueprint, layer):
    """validates a layer by validating all testcases under that layer
    """
    print('## Layer {}'.format(layer))
    for testcase in blueprint[layer]:
        testcase['layer'] = layer
        run_testcase(testcase)


def validate_blueprint(yaml_loc, layer):
    """Parse yaml file and validates given layer. If no layer given all layers
    validated
    """
    with open(yaml_loc) as yaml_file:
        yamldoc = yaml.safe_load(yaml_file)
    blueprint = yamldoc['blueprint']
    if layer is None:
        for each_layer in blueprint['layers']:
            validate_layer(blueprint, each_layer)
    else:
        validate_layer(blueprint, layer)


@click.command()
@click.argument('blueprint')
@click.option('--layer', '-l')
def main(blueprint, layer):
    """Takes blueprint name and optional layer. Validates inputs and derives
    yaml location from blueprint name. Invokes validate on blue print.
    """
    mypath = Path(__file__).absolute()
    yaml_loc = mypath.parents[0].joinpath('bluval-{}.yaml'.format(blueprint))
    if layer is not None:
        layer = layer.lower()
    validate_blueprint(yaml_loc, layer)


if __name__ == "__main__":
    # pylint: disable=no-value-for-parameter
    main()
