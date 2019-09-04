##############################################################################
# Copyright (c) 2019 AT&T, ENEA AB, Nokia and others                         #
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
FROM python:3.6-alpine3.9 as build

ARG REFSTACK_TARGET=2019.06

# Install dependencies
COPY pip-requirements.txt /wheels/requirements/pip-requirements.txt
RUN apk --no-cache add --update \
        gcc \
        git \
        libc-dev \
        libffi \
        libffi-dev \
        make \
        openssl-dev \
        wget

# Build binaries
WORKDIR /wheels
RUN pip3 install wheel
RUN pip3 wheel -r /wheels/requirements/pip-requirements.txt
RUN git clone https://gerrit.akraino.org/r/validation /opt/akraino/validation
RUN wget -q -O /tmp/test_list.txt \
    "https://refstack.openstack.org/api/v1/guidelines/$REFSTACK_TARGET/tests?target=platform&type=required&alias=true&flag=false"

# Copy binaries in the final container and install requirements
FROM python:3.6-alpine3.9
COPY --from=build /wheels /wheels
COPY --from=build /opt/akraino/validation /opt/akraino/validation
COPY --from=build /tmp/test_list.txt /opt/akraino/validation/tests/openstack/tempest/test_list.txt

RUN pip3 install -r /wheels/requirements/pip-requirements.txt \
                 -f /wheels && \
     rm -rf /wheels && \
     rm -rf /root/.cache/pip/*

# Install blueval dependencies
RUN pip install -r /opt/akraino/validation/bluval/requirements.txt
