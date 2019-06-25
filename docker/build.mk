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
REGISTRY ?= akraino
NAME ?= validation
TAG_PRE ?= $(notdir $(CURDIR))
TAG_VER ?= latest
DOCKERFILE ?= Dockerfile
WORK_DIR ?= $(CURDIR)
MTOOL ?= $(dir $(realpath $(lastword $(MAKEFILE_LIST))))/manifest-tool

# git submodule & patch locations for upstream patching before build
GIT_ROOT    := $(shell git rev-parse --show-toplevel)
GIT_DIR     := $(shell git rev-parse --git-dir)
PATCH_DIR   := $(shell pwd)/patches
AKRAINO_TAG := akraino-validation
SHELL        = /bin/sh
PATCHES      = $(shell find $(PATCH_DIR) $(PATCH_DIR)/$$(uname -m) \
                 -maxdepth 1 -name '*.patch' 2> /dev/null)

export GIT_COMMITTER_NAME?=Akraino Validation
export GIT_COMMITTER_EMAIL?=validation@akraino.org

# get the architecture of the host
HOST_ARCH = amd64
ifeq ($(shell uname -m), aarch64)
    HOST_ARCH = arm64
endif

$(MTOOL):
	wget https://github.com/estesp/manifest-tool/releases/download/v0.9.0/manifest-tool-linux-$(HOST_ARCH) -O $@
	sudo chmod +x $@

.PHONY: .build
.build::
	cd $(WORK_DIR) && \
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

.submodules_init:
	cd $(GIT_ROOT) && git submodule update --init 2>/dev/null; \
	touch $@

.submodules_patched: .submodules_init $(PATCHES)
	$(MAKE) .submodules_clean
	@cd $(GIT_ROOT) && git submodule -q foreach ' \
		if [ $$name = $$(basename $(TAG_PRE)) ] && [ -n "$(PATCHES)" ]; then \
			git tag $(AKRAINO_TAG)-upstream && \
			git checkout -q -b akraino-validation && \
			echo "`tput setaf 2`-- patching $$name`tput sgr0`";\
			git am -3 --ignore-whitespace --patch-format=mbox \
				--committer-date-is-author-date $(PATCHES) && \
			git tag $(AKRAINO_TAG) || exit 1; \
		fi'; \
	touch $@

.PHONY: .submodules_clean
.submodules_clean:
	@cd $(GIT_ROOT) && git submodule -q foreach ' \
		git am -q --abort > /dev/null 2>&1; \
		git checkout -q -f $(AKRAINO_TAG)-upstream > /dev/null 2>&1; \
		git branch -q -D akraino-validation > /dev/null 2>&1; \
		git tag | grep $(AKRAINO_TAG) | xargs git tag -d > /dev/null 2>&1; \
		git reset -q --hard HEAD; \
		git clean -xdff'
	@rm -f .submodules_patched
