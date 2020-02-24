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
FROM ubuntu:18.04 as build

# Install dependencies
COPY pip-requirements.txt /wheels/requirements/pip-requirements.txt
RUN apt-get update && apt-get -y install \
    python3-pip \
    gcc \
    git \
    libc-dev \
    libffi6 \
    libffi-dev \
    make \
    libssl-dev \
    golint \
    sqlite3 \
    debian-goodies \
    build-essential \
    autoconf automake autotools-dev m4 \
    linux-headers-generic \
    libaio-dev libattr1-dev libcap-dev

# Build binaries
WORKDIR /wheels
RUN pip3 install wheel
RUN pip3 wheel -r /wheels/requirements/pip-requirements.txt
RUN git clone https://gerrit.akraino.org/r/validation /opt/akraino/validation

# Build ltp tests
RUN mkdir -p /opt/akraino /root/src
WORKDIR  /root/src
RUN git clone https://github.com/linux-test-project/ltp.git
WORKDIR /root/src/ltp
RUN make autotools && \
    ./configure --prefix=/opt/ltp && \
    make -j $(getconf _NPROCESSORS_ONLN) 2>&1 | tee ../build-log.txt && \
    make install 2>&1 | tee ../install-log.txt
RUN tar czvf /opt/akraino/ltp.tar.gz /opt/ltp
WORKDIR /root/src
RUN git clone https://github.com/CISOfy/lynis && tar czvf /opt/akraino/lynis-remote.tar.gz ./lynis

# Copy binaries into the final container and install robot framework
FROM ubuntu:18.04
COPY --from=build /wheels /wheels
COPY --from=build /opt/akraino/validation /opt/akraino/validation
COPY --from=build /opt/akraino/ltp.tar.gz /opt/akraino/ltp.tar.gz
COPY --from=build /opt/akraino/lynis-remote.tar.gz /opt/akraino/lynis-remote.tar.gz


RUN apt-get update && apt-get -y upgrade && apt-get -y install \
    python3-pip python3.6 \
    sqlite \
    debian-goodies \
    wget \
    curl apt-transport-https ca-certificates software-properties-common && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - && \
    add-apt-repository "deb https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" && \
    apt-get update && \
    apt-get install -y docker-ce && \
    cd /usr/bin && ln -s python3 python && \
    pip3 install -r /wheels/requirements/pip-requirements.txt \
                 -f /wheels && \
    rm -rf /wheels && \
    rm -rf /root/.cache/pip/* && \
    rm -rf /var/cache/apt/* && \
    rm -rf /var/lib/apt/lists/*

# Fetches vuls databases
SHELL ["/bin/bash", "-c"]
RUN wget https://dl.google.com/go/go1.12.6.linux-amd64.tar.gz -P /root/ && \
    cd /root/ && \
    tar -xzf go1.12.6.linux-amd64.tar.gz -C /root/ && \
    export GOROOT=/root/go && \
    export GOPATH=/root/go/src && \
    export PATH=$PATH:/root/go/bin:/root/go/src/bin && \
    mkdir -p /root/go/src/github.com/future-architect && \
    cd /root/go/src/github.com/future-architect && \
    git clone https://github.com/future-architect/vuls && \
    cd vuls && \
    make install && \
    mkdir -p /root/go/src/github.com/kotakanbe && \
    git -C /root/go/src/github.com/kotakanbe clone https://github.com/kotakanbe/go-cve-dictionary.git && \
    cd /root/go/src/github.com/kotakanbe/go-cve-dictionary/ && \
    make install && \
    mkdir /opt/akraino/validation/tests/os/vuls/ && \
    for i in $(seq 2002 "$(date +"%Y")"); do go-cve-dictionary fetchnvd -http-proxy=${HTTP_PROXY} -dbpath /opt/akraino/validation/tests/os/vuls/cve.sqlite3 -years "$i"; done && \
    git -C /root/go/src/github.com/kotakanbe clone https://github.com/kotakanbe/goval-dictionary.git && \
    cd /root/go/src/github.com/kotakanbe/goval-dictionary && \
    make install && \
    goval-dictionary fetch-ubuntu -http-proxy=${HTTP_PROXY} -dbpath=/opt/akraino/validation/tests/os/vuls/oval.sqlite3 16 && \
    cd /opt/akraino/validation/tests/os/vuls && \
    tar cvzf db.tar.gz *.sqlite3 && \
    rm *.sqlite3

# Install bluval dependencies
RUN pip3 install -r /opt/akraino/validation/bluval/requirements.txt
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
