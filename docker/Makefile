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

SUBDIRS := $(wildcard */.)
SUBDIRS_NAME := $(SUBDIRS:/.=)
SUBDIRS_BUILD := $(SUBDIRS:/.=-build)
SUBDIRS_PUSH := $(SUBDIRS:/.=-push)

.PHONY: all
all: $(SUBDIRS_NAME)

.PHONY: build-all
build-all: $(SUBDIRS_BUILD)

.PHONY: push-all
push-all: $(SUBDIRS_PUSH)

.PHONY: $(SUBDIRS_NAME)
$(SUBDIRS_NAME):
	$(MAKE) -C $@

.PHONY: $(SUBDIRS_BUILD)
$(SUBDIRS_BUILD):
	$(MAKE) -C $(@:-build=) build

.PHONY: $(SUBDIRS_PUSH)
$(SUBDIRS_PUSH):
	$(MAKE) -C $(@:-push=) push-image

include build.mk
