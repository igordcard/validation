# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

## [0.0.1-SNAPSHOT] - 4 June 2019
### Added
- A user can commit and be informed about submissions of blueprint validation requests.
- Appropriate dockerfiles have been added for the UI and the required postgreSQL db.
- .gitignore file has been updated
- Integration with Jenkins completed
- The URL of the result is retrieved and displayed
- Multi-threading is now supported
- Notification callback from Jenkins implemented
- Results are retrieved from Nexus
- Results are displayed
- UI and postgreSQL docker projects support the makefile automation build and push process
- README file is included
- CHANGELOG is included
- Coala static code analysis performed for Java and JS files

### Changed

### Removed

## [0.0.1-SNAPSHOT] - 5 June 2019
### Added

### Changed
- PostgreSQL database model has been refactored in order to support 4 tables, namely timeslot, blueprint, blueprint_instance, submission
- Trailing spaces removed from comments in javascript files

### Removed

## [0.0.1-SNAPSHOT] - 6 June 2019
### Added
- Community lab is now supported

### Changed
- README file is updated.
- Coala static analysis issues fixed for Javascript files
- README.md has been renamed to README.rst

### Removed

## [0.0.1-SNAPSHOT] - 7 June 2019
### Added
- Arm lab is now supported

### Changed
- README file is updated.

### Removed

## [0.0.1-SNAPSHOT] - 10 June 2019
### Added

### Changed
- Trailing spaces removed from all files.
- README file is updated.

### Removed

## [0.1.0-SNAPSHOT] - 24 June 2019
### Added
- The following database initialization scripts of ONAP portal SDK project have been added (but modified in order to support the Akraino database) : epsdk-app-common/db-scripts/EcompSdkDDLMySql_2_4_Common.sql, epsdk-app-os/db-scripts/EcompSdkDDLMySql_2_4_OS.sql, epsdk-app-common/db-scripts/EcompSdkDMLMySql_2_4_Common.sql and epsdk-app-os/db-scripts/EcompSdkDMLMySql_2_4_OS.sql. The copyrights of these files have not been changed.
- Proxies for connecting with Nexus and Jenkins are now supported.
- io.fabric8/docker-maven-plugin can now be used for creating UI container for development purposes

### Changed
- Adaptation to ONAP portal SDK completed. Version 2.4.0 (Casablanca) has been used.
- The new URL of the results stored in Nexus is now used.
- Jenkins API is used by utilizing lower case for blueprint names and layers
- README file is updated
- Bug fixed when test suite is selected
- Jenkins job name is dynamically defined in the Nexus result URL
- Shell script input variables are now declared using capital letters

### Removed

## [0.1.0-SNAPSHOT] - 2 Jule 2019
### Added
- "All" blueprint layers is now supported.
- Pop up windows inform UI user about the status of result retrieval
- Blueprint version is now passed as a parameter to the Jenkins validation job
- Database tables have been added, namely 'lab' and 'silo'

### Changed
- Start date time and duration are initialized to 'now' and null respectively, as they are not taken into account
- As the blueprint, bluprint_instance_for_validation and timeslot tables must be initialized differently for each lab use case, the previous initialization data was extracted from the db-scripts/akraino-blueprint_validation_db_example.sql file and was placed inside a new file, namely db-scripts/examples/initialize_db_example.sql.
- The table 'blueprint_instance' has been renamed to 'blueprint_instance_for_validation'
- DB scripts are now copied to the /docker-entrypoint-initdb.d directory
- Lab field has been removed from timeslot table and placed in the submission table.
- README file has been updated
- Definition of system architecture removed from the pom.xml

### Removed

## [0.1.0-SNAPSHOT] - 8 Jule 2019
### Added
- Declarative info of the results is now displayed
- README enhanced in order to display instructions regarding how each docker image can be built independently.
- A new script, namely validation/docker/mariadb/deploy_with_existing_persistent_storage.sh has been developed in order to deploy the mariadb container when the persistent storage already exists. README files have been updated accordingly.
- Searching matching fields have been added to the angularJS application
- The angularJS application displays the submission id received from the back-end system
- Whenever a new blueprint instance for validation is submitted, the corresponding labels are deleted
- Scroll up/down feature is supported for the committed submissions
- Loading gif is displayed while the angularJS application is trying to fetch results
- Sanity checks in angularJS app

### Changed
- /db-scripts/akraino-blueprint_validation_db.sql has been renamed to /db-scripts/akraino_blueprint_validation_db.sql
- User info declaration has been moved from the /db-scripts/akraino-blueprint_validation_db.sql and placed into /db-scripts/EcompSdkDMLMySql_2_4_Common.sql and /db-scripts/EcompSdkDMLMySql_2_4_OS.sql
- Results can be retrieved only for submissions whose state is 'Completed'
- README file has been updated to use impersonal phrases

### Removed
- Deletion of submissions

## [0.1.0-SNAPSHOT] - 23 August 2019
### Added

### Changed

### Removed
- Unused credentials in music.properties file

## [0.2.0-SNAPSHOT] - 30 August 2019
### Added
- Partial loop is supported
- LAB parameter is now being sent towards Jenkins
- Logout process is supported
- Results are retrieved by timestamp, last run and based on date
- For DB results, a common thread-safe adapter is used

### Changed
- The common Nexus URL is now also used for full control loop mode.
- Only the IP and port are needed to be defined regarding DB identification process
- 'v1' has been added in REST API URLs
- Small letters are used for all the view names
- Bug with infinite nested ng-repeat loops fixed
- The name of war file has been changed from 'AECBlueprintValidationUI' to 'bluvalui'.
- Bug fixed regarding the display of the overall result.
- Host verifiers and trust certificates have been moved from clients to 'UiInitializer' spring component.
- UI docker container is detached when it is deployed
- README file has been updated to include instructions regarding installation of needed tools for the development mode
- 200 most recent results are retrieved on each nexus scan loop
- A common diplay test suite view is used now
- DB results have been divided in order to support layer as a key
- DB akraino user is used instead of DB root user
- test.info.yaml parsing is supported
- Selection of optional test cases is supported
- Pagination table is supported for committed submissions

### Removed

## [0.2.1-SNAPSHOT] - 16 September 2019
### Added
- Blueprint names and versions are retrieved from Nexus and stored in database.

### Changed
- A common class is used for manipulating the Nexus and database validation results.
- If a result had been fetched in the past from Nexus, it is not being fetched again.

### Removed

## [0.3.1-SNAPSHOT] - 20 September 2019
### Added
- Tabs for CRUD operations regarding labs, blueprints, layers, timeslots and blueprint instances have been added.

### Changed
- Timeslot is now referenced by a blueprint instance
- The UI searches for results only under the 'bluval_results' directory in Nexus

### Removed

## [0.3.2-SNAPSHOT] - 24 September 2019
### Added
- Redirection of all HTTP requests to the corresponding HTTPS resource.

### Changed

### Removed

## [0.3.3-SNAPSHOT] - 25 September 2019
### Added
- Encryption of passwords stored in database.

### Changed
- Password of users that try to login is taken into account

### Removed

## [0.3.4-SNAPSHOT] - 26 September 2019
### Added
- Prevent XSS attacks

### Changed

### Removed

## [0.4.0-SNAPSHOT] - 26 September 2019
### Added
- User creation
- Support of AES PKCS#5 for encrypting/decrypting passwords in database

### Changed

### Removed

## [0.4.1-SNAPSHOT] - 27 September 2019
### Added
- Contact us and support URLs have been set to null

### Changed
- MariaDB has been substituted with MySQL

### Removed

## [0.4.2-SNAPSHOT] - 1 October 2019
### Added

### Changed
- Redirection bug during session timeouts and unauthorized accesses of resources fixed.

### Removed

## [0.4.3-SNAPSHOT] - 3 October 2019
### Added
- Full CRUD operations on user data structure is supported.

### Changed
- 'akraino' database has been renamed to 'akraino_bluvalui'

### Removed

## [0.4.4-SNAPSHOT] - 4 October 2019 - Tagged as 2.0.0
### Added
- The user can define whether the UI can trust all SSL certificates or not.
- The mysql user name can be configured.

### Changed
- New approach is used for interpreting shell script input variables. Now, all symbols are recognized.

### Removed

## [0.4.5-SNAPSHOT] - 15 November 2019
### Added

### Changed
- The files 'server.xml' and index.jsp are embedded inside the UI docker image during build stage

### Removed

## [0.4.6-SNAPSHOT] - 02 December 2019
### Added

### Changed
- Validation results are ignored when there are no robot test results associated with them

### Removed

## [0.4.7-SNAPSHOT] - 21 January 2020
### Added
- Messages and Arguments of Robot keywords are displayed

### Changed
- The correct indentation is used for displaying Robot keywords
- Improvement of exception handling

### Removed
