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

# Directory on host in where database data are stored
HOST_STORAGE_DIR="/var/lib/mariadb"
# Container name
CONTAINER_NAME="akraino-validation-mariadb"
# Container input variables
MARIADB_ROOT_PASSWORD=""
# Image data
REGISTRY=akraino
NAME=validation
TAG_PRE=mariadb
TAG_VER=latest
MARIADB_HOST_PORT=3307

for ARGUMENT in "$@"
do
    KEY=$(echo $ARGUMENT | cut -f1 -d=)
    VALUE=$(echo $ARGUMENT | cut -f2 -d=)
    case "$KEY" in
            REGISTRY)              REGISTRY=${VALUE} ;;
            NAME)    NAME=${VALUE} ;;
            TAG_VER)    TAG_VER=${VALUE} ;;
            TAG_PRE)    TAG_PRE=${VALUE} ;;
            CONTAINER_NAME)    CONTAINER_NAME=${VALUE} ;;
            MARIADB_HOST_PORT)    MARIADB_HOST_PORT=${VALUE} ;;
            MARIADB_ROOT_PASSWORD)    MARIADB_ROOT_PASSWORD=${VALUE} ;;
            *)
    esac
done

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$TAG_VER"
docker run --detach --name $CONTAINER_NAME --publish $MARIADB_HOST_PORT:3306 --volume $HOST_STORAGE_DIR:/var/lib/mysql -v "/$(pwd)/mariadb.conf:/etc/mysql/conf.d/my.cnf" -e MYSQL_ROOT_PASSWORD="$MARIADB_ROOT_PASSWORD" $IMAGE
sleep 10
docker exec $CONTAINER_NAME /bin/bash -c 'rm -rf /docker-entrypoint-initdb.d/*.sql'
