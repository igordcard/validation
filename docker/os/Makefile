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

export OS_VULS_DB?=/opt/akraino/validation/tests/os/vuls/db.tar.gz

.PHONY: .init_db_cache
.init_db_cache:
	if [ ! -f $(OS_VULS_DB) ]; then \
		sudo mkdir -p $(dir $(OS_VULS_DB)); \
		sudo chown $(shell id -u):$(shell id -g) $(dir $(OS_VULS_DB)); \
		tar czf $(OS_VULS_DB) -T /dev/null; \
	fi

.PHONY: .build
.build::
	if [ -f $(OS_VULS_DB) ]; then \
		echo DB cache file found, will be used during the build; \
		cp $(OS_VULS_DB) .; \
	else \
		tar czf $(notdir $(OS_VULS_DB)) -T /dev/null; \
	fi
	date +%s > $(notdir $(OS_VULS_DB)).timestamp

.PHONY: all
all: push-image .push_manifest

.PHONY: build
build: .build

.PHONY: push-image
push-image: .push_image

include ../build.mk

.PHONY: .build
.build::
	if [ -f $(OS_VULS_DB) ]; then \
		echo DB cache file found, will be updated; \
		docker run -v $(OS_VULS_DB):$(OS_VULS_DB).host \
			--rm --entrypoint cp \
			$(REGISTRY)/$(NAME):$(TAG_PRE)-$(HOST_ARCH)-$(TAG_VER) \
			$(OS_VULS_DB) $(OS_VULS_DB).host; \
	fi
	rm -f $(notdir $(OS_VULS_DB))*
