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
    -v /home/jenkins/k8s_results:/opt/akraino/results/ \
    akraino/validation:k8s-latest

By default, the container will run the k8s conformance test. If you want to
enter the container, add */bin/sh* at the end of the command above

The mariadb container
=====================

Building and pushing the container
----------------------------------

To build just the postgresql container, use the command:

.. code-block:: console

   make mariadb-build [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

To both build and push the container, use the command:

.. code-block:: console

   make mariadb [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

Using the container
-------------------
In order for the container to be easily created, the deploy.sh script has been developed. This script accepts the following as input parameters:

CONTAINER_NAME, name of the container, default value is akraino-validation-mariadb
MARIADB_ROOT_PASSWORD, the desired mariadb root user password, this variable is required
UI_ADMIN_PASSWORD, the desired Blueprint Validation UI password for the admin user, this variable is required
UI_AKRAINO_PASSWORD, the desired Blueprint Validation UI password for the akraino user, this variable is required
REGISTRY, registry of the mariadb image, default value is akraino
NAME, name of the mariadb image, default value is validation
TAG_PRE, first part of the image version, default value is mariadb
TAG_VER, last part of the image version, default value is latest
MARIADB_HOST_PORT, port on which mariadb is exposed on host, default value is 3307

In order to deploy the container, this script can be executed with the appropriate parameters.

Example (assuming the default variables have been utilized for building the image using the make command):

.. code-block:: console

    cd validation/docker/mariadb
    ./deploy.sh MARIADB_ROOT_PASSWORD=password UI_ADMIN_PASSWORD=admin UI_AKRAINO_PASSWORD=akraino

Also, in order to re-deploy the database (it is assumed that the corresponding mariadb container has been stopped and deleted) while the persistent storage already exists (currently, the directory /var/lib/mariadb of the host is used), a different approach should be used after the image build process.

To this end, another script has been developed, namely deploy_with_existing_storage.sh which easily deploys the container. This script accepts the following items as input parameters:

CONTAINER_NAME, the name of the container, default value is akraino-validation-mariadb
MARIADB_ROOT_PASSWORD, the desired mariadb root user password, this variable is required
REGISTRY, the registry of the mariadb image, default value is akraino
NAME, the name of the mariadb image, default value is validation
TAG_PRE, the first part of the image version, default value is mariadb
TAG_VER, the last part of the image version, default value is latest
MARIADB_HOST_PORT, the port on which mariadb is exposed on host, default value is 3307

In order to deploy the container, this script can be executed with the appropriate parameters.

Example (assuming the default variables have been utilized for building the image using the make command):

.. code-block:: console

    cd validation/docker/mariadb
    ./deploy_with_existing_persistent_storage.sh MARIADB_ROOT_PASSWORD=password

More info can be found at the UI README file.

The ui container
================

Building and pushing the container
----------------------------------

To build just the UI container, use the command:

.. code-block:: console

    make ui-build [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

To both build and push the container, use the command:

.. code-block:: console

    make ui [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

Using the container
-------------------
In order for the container to be easily created, the deploy.sh script has been developed. This script accepts the following as input parameters:

CONTAINER_NAME, the name of the contaner, default value is akraino-validation-ui
DB_CONNECTION_URL, the URL connection with the akraino database of the maridb instance, this variable is required
MARIADB_ROOT_PASSWORD, the mariadb root user password, this variable is required
REGISTRY, the registry of the mariadb image, default value is akraino
NAME, the name of the mariadb image, default value is validation
TAG_PRE, the first part of the image version, default value is ui
TAG_VER, the last part of the image version, default value is latest
JENKINS_URL, the URL of the Jenkins instance, this variable is required
JENKINS_USERNAME, the Jenkins user name, this variable is required
JENKINS_USER_PASSWORD, the Jenkins user password, this variable is required
JENKINS_JOB_NAME, the name of Jenkins job capable of executing the blueprint validation tests, this variable is required
NEXUS_PROXY, the needed proxy in order for the Nexus server to be reachable, default value is none
JENKINS_PROXY, the needed proxy in order for the Jenkins server to be reachable, default value is none

Note that, for a functional UI, the following prerequisites are needed:

- The mariadb container in up and running state
- A Jenkins instance capable of running the blueprint validation test
- A Nexus repo in which all the test results are stored.

More info can be found at the UI README file.

In order to deploy the container, the aforementioned script can be executed with the appropriate parameters.

Example (assuming the default variables have been utilized for building the image using the make command):

.. code-block:: console

    cd validation/docker/ui
    ./deploy.sh DB_CONNECTION_URL=172.17.0.3:3306/akraino MARIADB_ROOT_PASSWORD=password JENKINS_URL=http://192.168.2.2:8080 JENKINS_USERNAME=name JENKINS_USER_PASSWORD=jenkins_pwd JENKINS_JOB_NAME=job1

The kube-conformance container
==============================

Building and pushing the container
----------------------------------

To build just the kube-conformance container, use the command:

.. code-block:: console

    make kube-conformance-build [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

To both build and push the container, use the command:

.. code-block:: console

    make kube-conformance [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

Using the container
-------------------

This is a standalone container able to launch Kubernetes end-to-end tests,
for the purposes of conformance testing.

It is a thin wrapper around the `e2e.test` binary in the upstream Kubernetes
distribution, which drops results in a predetermined location for use as a
[Heptio Sonobuoy](https://github.com/heptio/sonobuoy) plugin.

To learn more about conformance testing and its Sonobuoy integration, read the
[conformance guide](https://github.com/heptio/sonobuoy/blob/master/docs/conformance-testing.md).

Example:

.. code-block:: console

    docker run -ti akraino/validation:kube-conformance-v1.15

By default, the container will run the `run_e2e.sh` script. If you want to
enter the container, add */bin/sh* at the end of the command above

Normally, this conainer is not used directly, but instead leveraged via
sonobuoy.

The sonobuoy-plugin-systemd-logs container
==========================================

Building and pushing the container
----------------------------------

To build just the sonobuoy-plugin-systemd-logs container, use the command:

.. code-block:: console

    make sonobuoy-plugin-systemd-logs-build [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

To both build and push the container, use the command:

.. code-block:: console

    make sonobuoy-plugin-systemd-logs [ REGISTRY=<dockerhub_registry> NAME=<image_name>]

Using the container
-------------------

This is a simple standalone container that gathers log information from
systemd, by chrooting into the node's filesystem and running `journalctl`.

This container is used by [Heptio Sonobuoy](https://github.com/heptio/sonobuoy)
for gathering host logs in a Kubernetes cluster.

Example:

.. code-block:: console

    docker run -ti akraino/validation:sonobuoy-plugin-systemd-logs-latest

By default, the container will run the `get_systemd_logs.sh` script. If you
want to enter the container, add */bin/sh* at the end of the command above.

Normally, this conainer is not used directly, but instead leveraged via
sonobuoy.
