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

export WORK_DIR?=$(TAG_PRE)
export KUBE_VERSION=1.16
export TAG_VER=v$(KUBE_VERSION)

.PHONY: .build
.build:: .submodules_patched
	$(MAKE) -C $(TAG_PRE) e2e.test kubectl ginkgo

.PHONY: all
all: push-image .push_manifest

.PHONY: build
build: .build

.PHONY: push-image
push-image: .push_image

include ../build.mk
