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
    echo "AKRAINO_HOME not available. Setting..."
    this_file="$(readlink -f $0)"
    bluval_dir="$(dirname $this_file)"
    validation_dir="$(dirname $bluval_dir)"
    parent_dir="$(dirname $validation_dir)"
    export AKRAINO_HOME="$parent_dir"
fi
echo "AKRAINO_HOME=$AKRAINO_HOME"

if [ "$#" -eq 0 ]
then
    echo 'Usage: sh blucon.sh [OPTIONS] BLUEPRINT

    Invokes blucon.py and passes parameters as it is.
    You can pass all the parameters blucon.py accepts,
    and as of now here is the list

    Options:
        -l, --layer TEXT
        -n, --network TEXT
        -o, --optional_also
        --help               Show this message and exit.'

    exit 1
fi

echo "Building docker image"
image_tag=$( (git branch || echo "* local") | grep "^\*" | awk '{print $2}')
docker build -t akraino/validation:blucon-$image_tag $AKRAINO_HOME/validation/bluval

set -x

docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $AKRAINO_HOME/results:/opt/akraino/results \
    -v $AKRAINO_HOME/validation:/opt/akraino/validation \
    akraino/validation:blucon-$image_tag "$@"
