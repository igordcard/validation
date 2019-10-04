#!/bin/bash
#
# Copyright (c) 2019 AT&T Intellectual Property.  All other rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Use this script if the persistent storage already exists and you want to use its data

set -ex

DOCKER_VOLUME_NAME="akraino-validation-mysql"
# Container name
CONTAINER_NAME="akraino-validation-mysql"
# Image data
REGISTRY=akraino
NAME=validation
TAG_PRE=mysql
TAG_VER=latest
MYSQL_HOST_PORT=3307

while [ $# -gt 0 ]; do
   if [[ $1 == *"--"* ]]; then
        v="${1/--/}"
        declare $v="$2"
   fi
   shift
done

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$TAG_VER"
docker run --detach --name $CONTAINER_NAME --publish $MYSQL_HOST_PORT:3306 -v $DOCKER_VOLUME_NAME:/var/lib/mysql -v "$(pwd)/mysql.conf:/etc/mysql/conf.d/my.cnf" $IMAGE
sleep 10
