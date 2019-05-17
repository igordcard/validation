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

CONTAINER_NAME="validation-ui"
REGISTRY=akraino
NAME=validation
TAG_PRE=`echo "${PWD##*/}"`
TAG_VER=latest
HOST_ARCH=amd64
postgres_db_user_pwd=""
jenkins_url=""
jenkins_user_name=""
jenkins_user_pwd=""
jenkins_job_name=""
nexus_results_url=""
proxy_ip=""
proxy_port=""

# get the architecture of the host
if [ "`uname -m`" = "aarch64" ]
  then
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
            postgres_db_user_pwd)    postgres_db_user_pwd=${VALUE} ;;
            jenkins_url)    jenkins_url=${VALUE} ;;
            jenkins_user_name)    jenkins_user_name=${VALUE} ;;
            jenkins_user_pwd)    jenkins_user_pwd=${VALUE} ;;
            jenkins_job_name)    jenkins_job_name=${VALUE} ;;
            nexus_results_url)    nexus_results_url=${VALUE} ;;
            proxy_ip)    proxy_ip=${VALUE} ;;
            proxy_port)    proxy_port=${VALUE} ;;
            *)
    esac
done

if [ -z "$postgres_db_user_pwd" ]
  then
    echo "ERROR: You must specify the postgresql root user password"
    exit 1
fi

if [ -z "$jenkins_url" ]
  then
    echo "ERROR: You must specify the Jenkins Url"
    exit 1
fi

if [ -z "$jenkins_user_name" ]
  then
    echo "ERROR: You must specify the Jenkins username"
    exit 1
fi

if [ -z "$jenkins_user_pwd" ]
  then
    echo "ERROR: You must specify the Jenkins user password"
    exit 1
fi

if [ -z "$jenkins_job_name" ]
  then
    echo "ERROR: You must specify the Jenkins job name"
    exit 1
fi

if [ -z "$nexus_results_url" ]
  then
    echo "ERROR: You must specify the Nexus Url"
    exit 1
fi

IMAGE="$REGISTRY"/"$NAME":"$TAG_PRE"-"$HOST_ARCH"-"$TAG_VER"
docker run --name $CONTAINER_NAME --network="host" -it --rm -e postgres_db_user_pwd="$postgres_db_user_pwd" -e jenkins_url="$jenkins_url" -e jenkins_user_name="$jenkins_user_name" -e jenkins_user_pwd="$jenkins_user_pwd" -e jenkins_job_name="$jenkins_job_name" -e nexus_results_url="$nexus_results_url" -e proxy_ip="$proxy_ip" -e proxy_port="$proxy_port" $IMAGE
sleep 10
