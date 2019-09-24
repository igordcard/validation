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

set -ex

# Container name
CONTAINER_NAME="akraino-validation-ui"
# Image data
REGISTRY=akraino
NAME=validation
TAG_PRE=ui
TAG_VER=latest
# Container input parameters
MARIADB_AKRAINO_PASSWORD=""
JENKINS_URL="https://jenkins.akraino.org/"
JENKINS_USERNAME="demo"
JENKINS_USER_PASSWORD="demo"
JENKINS_JOB_NAME="validation"
DB_IP_PORT=""
NEXUS_PROXY=""
JENKINS_PROXY=""
CERTDIR=$(pwd)

for ARGUMENT in "$@"
do
    KEY=$(echo $ARGUMENT | cut -f1 -d=)
    VALUE=$(echo $ARGUMENT | cut -f2 -d=)
    case "$KEY" in
            REGISTRY)              REGISTRY=${VALUE} ;;
            NAME)    NAME=${VALUE} ;;
            TAG_PRE)    TAG_PRE=${VALUE} ;;
            TAG_VER)    TAG_VER=${VALUE} ;;
            MARIADB_AKRAINO_PASSWORD)    MARIADB_AKRAINO_PASSWORD=${VALUE} ;;
            JENKINS_URL)    JENKINS_URL=${VALUE} ;;
            JENKINS_USERNAME)    JENKINS_USERNAME=${VALUE} ;;
            JENKINS_USER_PASSWORD)    JENKINS_USER_PASSWORD=${VALUE} ;;
            JENKINS_JOB_NAME)    JENKINS_JOB_NAME=${VALUE} ;;
            DB_IP_PORT)    DB_IP_PORT=${VALUE} ;;
            CONTAINER_NAME)    CONTAINER_NAME=${VALUE} ;;
            NEXUS_PROXY) NEXUS_PROXY=${VALUE} ;;
            JENKINS_PROXY) JENKINS_PROXY=${VALUE} ;;
            CERTDIR) CERTDIR=${VALUE} ;;
            *)
    esac
done

if [ -z "$DB_IP_PORT" ]
  then
    echo "ERROR: You must specify the database IP and port"
    exit 1
fi

if [ -z "$MARIADB_AKRAINO_PASSWORD" ]
  then
    echo "ERROR: You must specify the mariadb akraino user password"
    exit 1
fi

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$TAG_VER"
docker run --detach --name $CONTAINER_NAME --network="host" -v "$(pwd)/server.xml:/usr/local/tomcat/conf/server.xml" -v "$CERTDIR/bluval.key:/usr/local/tomcat/bluval.key" -v "$CERTDIR/bluval.crt:/usr/local/tomcat/bluval.crt" -v "$(pwd)/root_index.jsp:/usr/local/tomcat/webapps/ROOT/index.jsp" -e DB_IP_PORT="$DB_IP_PORT" -e MARIADB_AKRAINO_PASSWORD="$MARIADB_AKRAINO_PASSWORD" -e JENKINS_URL="$JENKINS_URL" -e JENKINS_USERNAME="$JENKINS_USERNAME" -e JENKINS_USER_PASSWORD="$JENKINS_USER_PASSWORD" -e JENKINS_JOB_NAME="$JENKINS_JOB_NAME" -e NEXUS_PROXY="$NEXUS_PROXY" -e JENKINS_PROXY="$JENKINS_PROXY" $IMAGE
sleep 10
