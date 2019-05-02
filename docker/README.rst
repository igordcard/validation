.. ############################################################################
.. Copyright (c) 2019 AT&T, ENEA AB, Nokia and others                         #
..                                                                            #
.. Licensed under the Apache License, Version 2.0 (the "License");            #
.. you maynot use this file except in compliance with the License.            #
..                                                                            #
.. You may obtain a copy of the License at                                    #
..       http://www.apache.org/licenses/LICENSE-2.0                           #
..                                                                            #
.. Unless required by applicable law or agreed to in writing, software        #
.. distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  #
.. WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           #
.. See the License for the specific language governing permissions and        #
.. limitations under the License.                                             #
.. ############################################################################


Overview
========

The Makefile in this directory is used to build and push all
the validation containers. The default registry is **akraino** on
dockerhub, but only CI jenkins slaves are authorized to push
images to that registry. If you want to push to your own test registry, set
the REGISTRY variables as in the commands below.

To build and push the images:
   make all [ REGISTRY=<dockerhub_registry> ]
To just build the containers, use the command:
   make build-all [ REGISTRY=<dockerhub_registry> ]

The k8s container
=================

To build just the k8s container, use the command:
   make k8s-build [ REGISTRY=<dockerhub_registry> ]
To both build and push the container, use the command:
   make k8s [ REGISTRY=<dockerhub_registry> ]

Container should be started with the admin.conf file mounted:
docker run -ti -v /home/jenkins/admin.conf:/root/.kube/config \
<dockerhub_registry>/validation:k8s-latest /bin/sh
