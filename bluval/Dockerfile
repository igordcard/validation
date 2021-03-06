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

# ref: https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#use-multi-stage-builds
FROM python:3.6-alpine3.10

RUN apk --no-cache add --update \
        gcc \
        git \
        docker-cli \
        libc6-compat \
        libc-dev \
        libffi \
        libffi-dev \
        make \
        openssl-dev

# Build binaries
RUN git clone https://gerrit.akraino.org/r/validation /opt/akraino/validation
RUN pip3 install -r /opt/akraino/validation/bluval/requirements.txt

WORKDIR /opt/akraino/validation
ENTRYPOINT  ["python3", "-B", "bluval/blucon.py"]
