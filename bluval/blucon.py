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
"""This module parses yaml file, reads layers runs container for each layer.
"""

import subprocess
import sys
import traceback
from pathlib import Path

import click
import yaml

from bluutil import BluvalError
from bluutil import ShowStopperError

def invoke_docker(bluprint, layer):
    """Start docker container for given layer
    """
    cmd = ("docker run"
           " -v $HOME/.ssh:/root/.ssh"
           " -v $HOME/.kube/config:/root/.kube/config"
           " -v $VALIDATION_HOME/tests/variables.yaml:"
           "/opt/akraino/validation/tests/variables.yaml"
           " -v $AKRAINO_HOME/results:/opt/akraino/results"
           " akraino/validation:{0}-latest"
           " bin/sh -c"
           " 'cd /opt/akraino/validation "
           "&& python bluval/bluval.py -l {0} {1}'").format(layer, bluprint)
    args = [cmd]
    try:
        print('Invoking {}'.format(args))
        subprocess.call(args, shell=True)
    except OSError:
        #print('Error while executing {}'.format(args))
        raise BluvalError(OSError)


def invoke_dockers(yaml_loc, layer, blueprint_name):
    """Parses yaml file and starts docker container for one/all layers
    """
    with open(str(yaml_loc)) as yaml_file:
        yamldoc = yaml.safe_load(yaml_file)
    blueprint = yamldoc['blueprint']
    if layer is None or layer == "all":
        for each_layer in blueprint['layers']:
            invoke_docker(blueprint_name, each_layer)
    else:
        invoke_docker(blueprint_name, layer)


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
    try:
        invoke_dockers(yaml_loc, layer, blueprint)
    except ShowStopperError as err:
        print('ShowStopperError:', err)
    except BluvalError as err:
        print('Unexpected BluvalError', err)
        raise
    except:
        print("Exception in user code:")
        print("-"*60)
        traceback.print_exc(file=sys.stdout)
        print("-"*60)
        raise


if __name__ == "__main__":
    # pylint: disable=no-value-for-parameter
    main()
