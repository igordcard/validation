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

.. code-block:: console

    make all [ REGISTRY=<dockerhub_registry> ]

To just build the containers, use the command:

.. code-block:: console

    make build-all [ REGISTRY=<dockerhub_registry> ]

The k8s container
=================

Building and pushing the container
----------------------------------

To build just the k8s container, use the command:

.. code-block:: console

    make k8s-build [ REGISTRY=<dockerhub_registry> ]

To both build and push the container, use the command:

.. code-block:: console

    make k8s [ REGISTRY=<dockerhub_registry> ]

Using the container
-------------------

The k8s image is meant to be ran from a server that has access to the
kubernetes cluster (jenkins slave, jumpserver, etc).

Before running the image, copy the folder ~/.kube from your kubernetes
master node to a local folder (e.g. /home/jenkins/k8s_access).

Container needs to be started with the kubernetes access folder mounted.
Optionally, the results folder can be mounted as well; this way the logs are
stored on the local server.

.. code-block:: console

    docker run -ti -v /home/jenkins/k8s_access:/root/.kube/ \
    -v /home/jenkins/k8s_results:/opt/akraino/validation/results/ \
    akraino/validation:k8s-latest

By default, the container will run the k8s conformance test. If you want to
enter the container, add */bin/sh* at the end of the command above


The postgresql container
=================

Building and pushing the container
----------------------------------

To build just the postgresql container, use the command:

.. code-block:: console

   make postgresql-build [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

To both build and push the container, use the command:

.. code-block:: console

   make postgresql [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

Using the container
-------------------
If you want to deploy the container, you can run the corresponding deploy.sh script with the appropriate parameters.

Example:

.. code-block:: console
    ./deploy.sh POSTGRES_PASSWORD=password


The ui container
=================

Building and pushing the container
----------------------------------

To build just the ui container, you must first compile the ui project.
Then use the command:

.. code-block:: console

   make ui-build [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

To both build and push the container, use the command:

.. code-block:: console

   make ui [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

Using the container
-------------------
If you want to deploy the container, you can run the corresponding deploy.sh script with the appropriate parameters.
Note, that you must also build and run the postgresql container for a functional UI.

Example:

.. code-block:: console
    ./deploy.sh postgres_db_user_pwd=password jenkins_url=http://192.168.2.2:8080 jenkins_user_name=name jenkins_user_pwd=jenkins_pwd jenkins_job_name=job1 nexus_results_url=https://nexus.akraino.org/content/sites/logs proxy_ip=172.28.40.9 proxy_port=3128
