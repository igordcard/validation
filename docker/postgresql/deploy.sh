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

export DROOT=/var/lib
CONTAINER_NAME="validation_postgresql"
POSTGRES_HOST_PORT=6432
REGISTRY=akraino
NAME=validation
TAG_PRE=`echo "${PWD##*/}"`
TAG_VER=latest
POSTGRES_PASSWORD=""
HOST_ARCH=amd64

# get the architecture of the host
if [ "`uname -m`" = "aarch64" ]; then
    HOST_ARCH=arm64
fi

for ARGUMENT in "$@"
do
    KEY=$(echo $ARGUMENT | cut -f1 -d=)
    VALUE=$(echo $ARGUMENT | cut -f2 -d=)
    case "$KEY" in
            REGISTRY)              REGISTRY=${VALUE} ;;
            NAME)    NAME=${VALUE} ;;
            TAG_VER)    TAG_VER=${VALUE} ;;
            POSTGRES_PASSWORD)    POSTGRES_PASSWORD=${VALUE} ;;
            *)
    esac
done

if [ -z "$POSTGRES_PASSWORD" ]
  then
    echo "ERROR: You must specify at least the postgreSQL database password"
    exit 1
fi

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$HOST_ARCH"-"$TAG_VER"
docker run --detach --name $CONTAINER_NAME --restart=always --publish $POSTGRES_HOST_PORT:5432 --volume $DROOT/postgres:/var/lib/postgresql/data --env POSTGRES_USER=admin --env POSTGRES_PASSWORD="$POSTGRES_PASSWORD" $IMAGE
sleep 10
docker exec $CONTAINER_NAME /bin/bash -c "psql -h localhost -p 5432 -U admin -f /akraino-blueprint_validation_db.sql"
