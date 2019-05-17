
Akraino Blueprint Validation UI
========

This project contains the source code of the Akraino Blueprint Validation UI.

This UI consists of the front-end and back-end parts.
The front-end part is based on HTML, CSS, and AngularJS technologies.
The back-end part is based on Spring MVC and Apache Tomcat technologies.

Based on these instructions, a user can provide the prerequisites, compile the source code and deploy the UI.

Download the project
--------------------

.. code-block:: console

    git clone "https://gerrit.akraino.org/r/validation"

Prerequisites
---------------

- Database

A PostgreSQL database instance is needed with the appropriate relations in order for the back-end system to store and retrieve data.
Configure the postgreSQL root password in the variable POSTGRES_PASSWORD and execute the following commands in order to build and deploy this database container:

.. code-block:: console

    cd validation/docker/postgresql
    make build
    ./deploy.sh POSTGRES_PASSWORD=password

Below, some data that is initialized in the aforementioned database is illustrated (note that this data is used mainly for testing purposes):

.. code-block:: console

    Timeslots:
    id:1 , start date and time: now() (i.e. the time of the postgreSQL container deployment), duration: 10 (sec), lab: 0 (i.e. AT&T)
    id:2 , start date and time: now() (i.e. the time of the postgreSQL container deployment), duration: 1000 (sec), lab: 0 (i.e. AT&T)

    Blueprints:
    id: 1 , name : 'dummy'
    id: 2 , name : 'Unicycle'

    Blueprint Instances:
    id: 1, blueprint_id: 1 (i.e. dummy), version: "0.0.2-SNAPSHOT", layer: 0 (i.e. Hardware), layer_description: "Dell Hardware", timeslot id: 1
    id: 2, blueprint_id: 2 (i.e. Unicycle), version: "0.0.1-SNAPSHOT", layer: 0 (i.e. Hardware), layer_description: "Dell Hardware", timeslot id: 2

For more information about this data, please refer to the file:

    validation/docker/postgresql/akraino-blueprint_validation_db.sql

Based on this data, the UI enables the user to select an appropriate blueprint instance for validation.

Currently, this data cannot be retrieved dynamically by the UI (see limitations subsection).

For this reason, in cases of new blueprint data, a user should define new entries in this database.

For example, if a user wants to define a new timeslot with the following data:

    start date and time:now, duration: 123 in secs, lab: Community

the following file should be created:

name: dbscript
content:
    insert into akraino.timeslot values(5, now(), 123, 2);

Then, the following command should be executed:

.. code-block:: console

    psql -h <IP of the postgreSQL container> -p 6432 -U admin -f ./dbscript

Furthermore, if a user wants to define a new blueprint, namely "newBlueprint" and a new instance of this blueprint with the following data:

    version: "0.0.1-SNAPSHOT", layer: 2 (i.e. K8s), layer_description: "K8s with High Availability Ingress controller", timeslot id: 5 (i.e. the new timeslot)

the following file should be created:

name: dbscript
content:
    insert into akraino.blueprint (blueprint_id, blueprint_name) values(4, 'newBlueprint');
    insert into akraino.blueprint_instance (blueprint_instance_id, blueprint_id, version, layer, layer_description, timeslot_id) values(6, 4, '0.0.1-SNAPSHOT', 2, 'K8s with High Availability Ingress controller', 5);

Then, the following command should be executed:

.. code-block:: console

    psql -h <IP of the postgreSQL container> -p 6432 -U admin -f ./dbscript

The UI will automatically retrieve this new data and display it to the user.

- Jenkins Configuration

The Blueprint validation UI will trigger job executions in a Jenkins instance.

This instance must have the following option enabled: "Manage Jenkins -> Configure Global Security -> Prevent Cross Site Request Forgery exploits".

Also, currently corresponding Jenkins job should accept the following as input parameters: "SUBMISSION_ID", "BLUEPRINT", "LAYER" and "UI_IP".
The "SUBMISSION_ID" and "UI_IP" parameters (i.e. IP address of the UI host machine-this is needed by the Jenkins instance in order to send back Job completion notification) are created and provided by the backend part of the UI.
The "BLUEPRINT" and "LAYER" parameters are configured by the UI user.

Moreover, as the Jenkins notification plugin (https://wiki.jenkins.io/display/JENKINS/Notification+Plugin) seems to ignore proxy settings, the corresponding Jenkins job must be configured to execute the following command at the end (Post-build Actions)

.. code-block:: console

    curl -v -H "Content-Type: application/json" -X POST --insecure --silent http://$UI_IP:8080/AECBlueprintValidationUI/api/jenkinsJobNotification/ --data '{"submissionId": "'"$SUBMISSION_ID"'" , "name":"'"$JOB_NAME"'", "buildNumber":"'"$BUILD_NUMBER"'"}'

Finally, the Jenkins instance must be accessible from the UI host without using system proxy.

- Nexus server

All the blueprint validation results are stored in Nexus server.

These results must be available in the following url:

    https://nexus.akraino.org/content/sites/logs/"lab"-blu-val/job/validation/"Jenkins job number"/results/"name_of_the_test_suite".

where "lab" is the name of the lab (for example 'att'), "Jenkins job number" is the number of the Jenkins job that produced this result, and "name_of_the_test_suite" is the name of the test suite.
If multiple test suites must run, multiple directories should be created.

Moreover, the results should be stored in the 'output.xml' file using the following format:

TBD

Finally, the Nexus server must be accessible from the UI (with or without using system proxy).


Compiling
---------

.. code-block:: console

    cd validation/ui
    mvn clean install

Deploying
---------

In the context of deploying, the following data is needed:

- The postgres root user password
- The Jenkins url
- The Jenkins username and password
- The name of Jenkins Job
- The Url of the Nexus results
- The host system's proxy ip and port

These variables must be configured as content of the deploy script input parameters. Execute the following commands in order to build and deploy the UI container:

.. code-block:: console

    cd validation/docker/ui
    make build
    ./deploy.sh postgres_db_user_pwd=password jenkins_url=http://192.168.2.2:8080 jenkins_user_name=name jenkins_user_pwd=jenkins_pwd jenkins_job_name=job1 nexus_results_url=https://nexus.akraino.org/content/sites/logs proxy_ip=172.28.40.9 proxy_port=3128

If no proxy exists, just do not define proxy ip and port variables.

The UI should be available in the following url:

    http://localhost:8080/AECBlueprintValidationUI

Limitations
-----------

- The UI has been tested using Chrome and Firefox browsers.
- The UI is not connected to any LDAP server. Currently, any user can login.
- The UI and postgreSQL containers must be deployed on the same server.
- The back-end part of the UI does not take into account the configured timeslot. It immediately triggers the corresponding Jenkins Job.
- Results data manipulation (filtering, graphical representation, indexing in time order, etc) is not supported.
- Only the following labs are supported: AT&T, Ericsson, Community and Arm.
- Only the following tabs are functional: 'Committed Submissions', 'Blueprint Validation Results -> Get by submission id'.
- The UI configures only the "BLUEPRINT" and "LAYER" input parameters of the Jenkins job.
- The available blueprints and timeslots must be manually configured in the PostgreSQL database.
- The Jenkins instance must be accessible from the UI host without using system proxy.