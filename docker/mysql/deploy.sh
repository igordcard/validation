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
MYSQL_AKRAINO_PASSWORD=""
# Image data
REGISTRY=akraino
NAME=validation
TAG_PRE=mysql
TAG_VER=latest
MYSQL_HOST_PORT=3307

for ARGUMENT in "$@"
do
    KEY=$(echo $ARGUMENT | cut -f1 -d=)
    VALUE=$(echo $ARGUMENT | cut -f2 -d=)
    case "$KEY" in
            REGISTRY)              REGISTRY=${VALUE} ;;
            NAME)    NAME=${VALUE} ;;
            TAG_VER)    TAG_VER=${VALUE} ;;
            TAG_PRE)    TAG_PRE=${VALUE} ;;
            MYSQL_ROOT_PASSWORD)    MYSQL_ROOT_PASSWORD=${VALUE} ;;
            MYSQL_AKRAINO_PASSWORD)    MYSQL_AKRAINO_PASSWORD=${VALUE} ;;
            CONTAINER_NAME)    CONTAINER_NAME=${VALUE} ;;
            MYSQL_HOST_PORT)    MYSQL_HOST_PORT=${VALUE} ;;
            *)
    esac
done

if [ -z "$MYSQL_ROOT_PASSWORD" ]
  then
    echo "ERROR: You must specify the mysql database root password"
    exit 1
fi

if [ -z "$MYSQL_AKRAINO_PASSWORD" ]
  then
    echo "ERROR: You must specify the mysql database akraino user password"
    exit 1
fi

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$TAG_VER"
chmod 0444 "/$(pwd)/mysql.conf"
docker run --detach --name $CONTAINER_NAME --publish $MYSQL_HOST_PORT:3306 -v $DOCKER_VOLUME_NAME:/var/lib/mysql -v "/$(pwd)/mysql.conf:/etc/mysql/conf.d/my.cnf" -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" -e MYSQL_DATABASE="akraino_bluvalui" -e MYSQL_USER="akraino" -e MYSQL_PASSWORD="$MYSQL_AKRAINO_PASSWORD" $IMAGE
sleep 10
