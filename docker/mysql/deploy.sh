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

# Use this script if the persistent storage does not exist

set -ex

DOCKER_VOLUME_NAME="akraino-validation-mysql"
# Container name
CONTAINER_NAME="akraino-validation-mysql"
# Container input variables
MYSQL_ROOT_PASSWORD=""
MYSQL_USER="akraino"
MYSQL_PASSWORD=""
# Image data
REGISTRY=akraino
NAME=validation
TAG_PRE=mysql
TAG_VER=latest

while [ $# -gt 0 ]; do
   if [[ $1 == *"--"* ]]; then
        v="${1/--/}"
        declare $v="$2"
   fi
   shift
done

if [ -z "$MYSQL_ROOT_PASSWORD" ]
  then
    echo "ERROR: You must specify the mysql database root password"
    exit 1
fi

if [ -z "$MYSQL_PASSWORD" ]
  then
    echo "ERROR: You must specify the mysql database user password"
    exit 1
fi

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$TAG_VER"
chmod 0444 "/$(pwd)/mysql.conf"
docker run --detach --name $CONTAINER_NAME -v $DOCKER_VOLUME_NAME:/var/lib/mysql -v "$(pwd)/mysql.conf:/etc/mysql/conf.d/my.cnf" -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" -e MYSQL_DATABASE="akraino_bluvalui" -e MYSQL_USER="$MYSQL_USER" -e MYSQL_PASSWORD="$MYSQL_PASSWORD" $IMAGE
sleep 10
