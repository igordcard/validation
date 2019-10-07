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
MYSQL_USER="akraino"
MYSQL_PASSWORD=""
JENKINS_URL="https://jenkins.akraino.org/"
JENKINS_USERNAME="demo"
JENKINS_USER_PASSWORD="demo"
JENKINS_JOB_NAME="validation"
DB_IP_PORT=""
NEXUS_PROXY=""
JENKINS_PROXY=""
CERTDIR=$(pwd)
ENCRYPTION_KEY=""
UI_ADMIN_PASSWORD=""
TRUST_ALL="false"
USE_NETWORK_HOST="false"

while [ $# -gt 0 ]; do
   if [[ $1 == *"--"* ]]; then
        v="${1/--/}"
        declare $v="$2"
   fi
   shift
done

if [ -z "$DB_IP_PORT" ]
  then
    echo "ERROR: You must specify the database IP and port"
    exit 1
fi

if [ -z "$MYSQL_PASSWORD" ]
  then
    echo "ERROR: You must specify the mysql user password"
    exit 1
fi

if [ -z "$ENCRYPTION_KEY" ]
  then
    echo "ERROR: You must specify the encryption key"
    exit 1
fi

if [ -z "$UI_ADMIN_PASSWORD" ]
  then
    echo "ERROR: You must specify the UI admin password"
    exit 1
fi

echo "Note: If there is a password already stored in database, the supplied UI_ADMIN_PASSWORD will be ignored."

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$TAG_VER"
if [[ $USE_NETWORK_HOST = "true" ]]
  then
    docker run --detach --name $CONTAINER_NAME --network="host" -v "$(pwd)/server.xml:/usr/local/tomcat/conf/server.xml" -v "$CERTDIR/bluval.key:/usr/local/tomcat/bluval.key" -v "$CERTDIR/bluval.crt:/usr/local/tomcat/bluval.crt" -v "$(pwd)/root_index.jsp:/usr/local/tomcat/webapps/ROOT/index.jsp" -e DB_IP_PORT="$DB_IP_PORT" -e MYSQL_USER="$MYSQL_USER" -e MYSQL_PASSWORD="$MYSQL_PASSWORD" -e JENKINS_URL="$JENKINS_URL" -e JENKINS_USERNAME="$JENKINS_USERNAME" -e JENKINS_USER_PASSWORD="$JENKINS_USER_PASSWORD" -e JENKINS_JOB_NAME="$JENKINS_JOB_NAME" -e NEXUS_PROXY="$NEXUS_PROXY" -e JENKINS_PROXY="$JENKINS_PROXY" -e ENCRYPTION_KEY="$ENCRYPTION_KEY" -e UI_ADMIN_PASSWORD="$UI_ADMIN_PASSWORD" -e TRUST_ALL="$TRUST_ALL" $IMAGE
  else
    docker run --detach --name $CONTAINER_NAME -v "$(pwd)/server.xml:/usr/local/tomcat/conf/server.xml" -v "$CERTDIR/bluval.key:/usr/local/tomcat/bluval.key" -v "$CERTDIR/bluval.crt:/usr/local/tomcat/bluval.crt" -v "$(pwd)/root_index.jsp:/usr/local/tomcat/webapps/ROOT/index.jsp" -e DB_IP_PORT="$DB_IP_PORT" -e MYSQL_USER="$MYSQL_USER" -e MYSQL_PASSWORD="$MYSQL_PASSWORD" -e JENKINS_URL="$JENKINS_URL" -e JENKINS_USERNAME="$JENKINS_USERNAME" -e JENKINS_USER_PASSWORD="$JENKINS_USER_PASSWORD" -e JENKINS_JOB_NAME="$JENKINS_JOB_NAME" -e NEXUS_PROXY="$NEXUS_PROXY" -e JENKINS_PROXY="$JENKINS_PROXY" -e ENCRYPTION_KEY="$ENCRYPTION_KEY" -e UI_ADMIN_PASSWORD="$UI_ADMIN_PASSWORD" -e TRUST_ALL="$TRUST_ALL" $IMAGE
fi
sleep 10
