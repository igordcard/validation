#!/bin/bash

##############################################################################
# Copyright (c) 2019 AT&T, ENEA Nokia and others                             #
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

if [ -z "$AKRAINO_HOME" ]
then
    echo "AKRAINO_HOME not available. Setting ..."
    AKRAINO_HOME="$(readlink -f "$(dirname "$0")/../..")"
fi

# Allow overriding VALIDATION_DIR and/or RESULTS_DIR via env vars
VALIDATION_DIR=${VALIDATION_DIR:-"${AKRAINO_HOME}/validation"}
RESULTS_DIR=${RESULTS_DIR:-"${AKRAINO_HOME}/results"}

echo "AKRAINO_HOME=$AKRAINO_HOME"
echo "VALIDATION_DIR=$VALIDATION_DIR"
echo "RESULTS_DIR=$RESULTS_DIR"

if [ "$#" -eq 0 ]
then
    echo 'Usage: sh blucon.sh [OPTIONS] BLUEPRINT

    Invokes blucon.py and passes parameters as it is.
    You can pass all the parameters blucon.py accepts,
    and as of now here is the list

    Options:
        -l, --layer TEXT
        -n, --network TEXT
        -t, --tag TEXT
        -o, --optional_also
        --help               Show this message and exit.'

    exit 1
fi

echo "Building docker image"
img="akraino/validation:blucon-$(git rev-parse --abbrev-ref HEAD || echo local)"
docker build -t "$img" "$VALIDATION_DIR/bluval"

set -x

docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v "$RESULTS_DIR":/opt/akraino/results \
    -v "$VALIDATION_DIR":/opt/akraino/validation \
    "$img" "$@"
