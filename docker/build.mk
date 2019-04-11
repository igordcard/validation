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


# declare the variables
REGISTRY ?= # TBD
NAME ?= akraino_validation
TAG_PRE ?= $(notdir $(CURDIR))
TAG_VER ?= latest
DOCKERFILE ?= Dockerfile
MTOOL ?= $(dir $(realpath $(lastword $(MAKEFILE_LIST))))/manifest-tool

# get the architecture of the host
HOST_ARCH = amd64
ifeq ($(shell uname -m), aarch64)
    HOST_ARCH = arm64
endif

$(MTOOL):
	wget https://github.com/estesp/manifest-tool/releases/download/v0.9.0/manifest-tool-linux-$(HOST_ARCH) -O $@
	sudo chmod +x $@

.PHONY: .build
.build:
	docker build \
		-t $(REGISTRY)/$(NAME):$(TAG_PRE)-$(HOST_ARCH)-$(TAG_VER) \
		-f $(DOCKERFILE) \
		.

.PHONY: .push_image
.push_image: .build
	docker push $(REGISTRY)/$(NAME):$(TAG_PRE)-$(HOST_ARCH)-$(TAG_VER)

.PHONY: .push_manifest
.push_manifest: $(MTOOL)
	$(MTOOL) push from-args \
		--platforms linux/amd64,linux/arm64 \
		--template $(REGISTRY)/$(NAME):$(TAG_PRE)-ARCH-$(TAG_VER) \
		--target $(REGISTRY)/$(NAME):$(TAG_PRE)-$(TAG_VER)
