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
BluVal is a diagnostic toolset framework to validate different layers in the
Akraino infrastructure developed and used in Akraino edge stack. BluVal
integrates different test cases, its development employs a declarative approach
that is version controlled in LF Gerrit. They are integrated in CI/CD tool
chain where peer Jenkins jobs can run the test cases and the results are
reported in LF Repo (Nexus). The test cases cover all blueprint layers in the
cluster.

Installation and execution
==========================
Bluval tool can be ran directly from the repo, or can be called from a container.


When ran directly, minimum requirements are python verson 3.5. To setup the
environment follow the commands below.

.. code-block:: console

    ns156u@aknode82:~$ git clone https://gerrit.akraino.org/r/validation.git
    ns156u@aknode82:~$ cd validation
    ns156u@aknode82:~/validation$ python -m venv .py35
    ns156u@aknode82:~/validation$ source .py35/bin/activate
    (.py35) ns156u@aknode82:~/validation$ pip install -r bluval/requirements.txt

To run the tests for a certain blueprint, follow the commands below. Optionally
the layer of testing can be specified too.

.. code-block:: console
    (.py35) ns156u@aknode82:~/validation$ python bluval/bluval.py -l \
                             hardware dummy # this will run hardware test cases of dummy blue print
    (.py35) ns156u@aknode82:~/validation$ deactivate


When ran from a container, docker needs to be installed on the machine.
To run the tests for a certain blueprint, follow the steps below. Optionally
the layer of testing can be specified too.

Note that before issuing the blucon command, you need to fill in the volumes
that will be mounted in the container. These are locations of the config files
or access files (ssh keys or clients configs) that will be used to connect to
the cluster. Also custom volumes can be added here.

.. code-block:: console

    ns156u@aknode82:~$ git clone https://gerrit.akraino.org/r/validation.git
    ns156u@aknode82:~$ cd validation
    ns156u@aknode82:~$ vi bluval/volumes.yaml # fill in the volumes to be \
						mounted in the container
    ns156u@aknode82:~$ python3 bluval/blucon.py dummy -l hardware
