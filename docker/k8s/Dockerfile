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
FROM golang:alpine3.9 as build

# Sonobuoy supports Kubernetes versions 1.11, 1.12 and 1.13
ARG SB_TAG=v0.14.3
# Determine the latest stable git tag at build time based on stable major version
ARG K8S_VER=1.15

# Install dependencies
COPY pip-requirements.txt /wheels/requirements/pip-requirements.txt

RUN apk --no-cache add --update \
        curl \
        openssl \
        python3 \
        bash \
        findutils \
        gcc \
        git \
        grep \
        libc-dev \
        libffi \
        libffi-dev \
        make \
        openssl-dev \
        python3-dev \
        py3-pip \
        rsync

# Build binaries; detect the architecture automatically (default is amd64)
RUN git clone https://github.com/kubernetes/kubernetes /src/k8s.io/kubernetes
RUN if [ $(uname -m) == 'aarch64' ]; then HOST_ARCH=arm64; else HOST_ARCH=amd64; fi && \
    echo "Building docker on $HOST_ARCH" && \
    cd /src/k8s.io/kubernetes && \
    git checkout $(curl \
        https://storage.googleapis.com/kubernetes-release/release/stable-$K8S_VER.txt) && \
    make kubectl ginkgo && \
    make WHAT=test/e2e/e2e.test ARCH=$HOST_ARCH
RUN go get -d github.com/heptio/sonobuoy && \
    cd /go/src/github.com/heptio/sonobuoy && \
    git checkout $SB_TAG && \
    go install
RUN git clone https://gerrit.akraino.org/r/validation /opt/akraino/validation
RUN cat /opt/akraino/validation/bluval/requirements.txt >> \
    /wheels/requirements/pip-requirements.txt

WORKDIR /wheels
RUN pip3 install wheel
RUN pip3 wheel -r /wheels/requirements/pip-requirements.txt

# Copy binaries in the final contaier and install robot framework
FROM python:3.6-alpine3.9
COPY --from=build /src/k8s.io/kubernetes/_output/bin /usr/local/bin
COPY --from=build /go/bin/sonobuoy /bin/sonobuoy
COPY --from=build /wheels /wheels
COPY --from=build /opt/akraino/validation/ /opt/akraino/validation/

RUN pip3 install -r /wheels/requirements/pip-requirements.txt \
                 -f /wheels && \
     rm -rf /wheels && \
     rm -rf /root/.cache/pip/*
