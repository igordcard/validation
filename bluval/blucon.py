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

_PULL = False
_OPTIONAL_ALSO = False
_SUBNET = ""

def get_volumes(layer):
    """Create a list with volumes to mount in the container for given layer
    """
    mypath = Path(__file__).absolute()
    volume_yaml = yaml.safe_load(mypath.parents[0].joinpath("volumes.yaml").open())

    if layer not in volume_yaml['layers']:
        return ''
    if volume_yaml['layers'][layer] is None:
        return ''

    volume_list = ''
    for vol in volume_yaml['layers'][layer]:
        if volume_yaml['volumes'][vol]['local'] == '':
            continue
        volume_list = (volume_list + ' -v ' +
                       volume_yaml['volumes'][vol]['local'] + ':' +
                       volume_yaml['volumes'][vol]['target'])
    return volume_list


def pull_docker(layer, tag):
    """Pull docker image for given layer
    """
    cmd = ("docker pull akraino/validation:{0}-{1}"
           .format(layer, tag))

    args = [cmd]
    try:
        print('\nPulling image using {}'.format(args), flush=True)
        subprocess.call(args, shell=True)
    except OSError:
        raise BluvalError(OSError)


def invoke_docker(bluprint, layer, tag):
    """Start docker container for given layer
    """
    if _PULL:
        pull_docker(layer, tag)
    volume_list = get_volumes('common') + get_volumes(layer)
    cmd = ("docker run --rm --net=host" + volume_list + _SUBNET +
           " akraino/validation:{0}-{3}"
           " /bin/sh -c"
           " 'cd /opt/akraino/validation "
           "&& python -B bluval/bluval.py -l {0} {1} {2}'"
           .format(layer, ("-o" if _OPTIONAL_ALSO else ""), bluprint, tag))

    args = [cmd]
    try:
        print('\nInvoking {}'.format(args), flush=True)
        subprocess.call(args, shell=True)
    except OSError:
        #print('Error while executing {}'.format(args))
        raise BluvalError(OSError)


def invoke_dockers(yaml_loc, layer, blueprint_name, tag):
    """Parses yaml file and starts docker container for one/all layers
    """
    with open(str(yaml_loc)) as yaml_file:
        yamldoc = yaml.safe_load(yaml_file)
    blueprint = yamldoc['blueprint']
    if layer is None or layer == "all":
        for each_layer in blueprint['layers']:
            invoke_docker(blueprint_name, each_layer, tag)
    else:
        invoke_docker(blueprint_name, layer, tag)


@click.command()
@click.argument('blueprint')
@click.option('--layer', '-l')
@click.option('--network', '-n')
@click.option('--tag', '-t')
@click.option('--optional_also', '-o', is_flag=True)
@click.option('--pull', '-P', is_flag=True)
# pylint: disable=too-many-arguments
def main(blueprint, layer, network, tag, optional_also, pull):
    """Takes blueprint name and optional layer. Validates inputs and derives
    yaml location from blueprint name. Invokes validate on blueprint.
    """
    global _PULL # pylint: disable=global-statement
    global _OPTIONAL_ALSO  # pylint: disable=global-statement
    global _SUBNET # pylint: disable=global-statement
    mypath = Path(__file__).absolute()
    yaml_loc = mypath.parents[0].joinpath('bluval-{}.yaml'.format(blueprint))
    if layer is not None:
        layer = layer.lower()
    if pull:
        _PULL = True
        print("_PULL {}".format(_PULL))
    if optional_also:
        _OPTIONAL_ALSO = True
        print("_OPTIONAL_ALSO {}".format(_OPTIONAL_ALSO))
    if network is not None:
        _SUBNET = " --net=" + network
        print("Using", _SUBNET)
    if tag is None:
        tag = 'latest'
    print("Using tag {}".format(tag))
    try:
        invoke_dockers(yaml_loc, layer, blueprint, tag)
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
