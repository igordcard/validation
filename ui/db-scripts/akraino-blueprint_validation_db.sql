/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

SET FOREIGN_KEY_CHECKS=1;

use akraino;

DROP TABLE IF EXISTS submission;
DROP TABLE IF EXISTS blueprint_instance_for_validation;
DROP TABLE IF EXISTS blueprint;
DROP TABLE IF EXISTS silo;
DROP TABLE IF EXISTS timeslot;
DROP TABLE IF EXISTS lab;

create table lab (
   id bigint not NULL AUTO_INCREMENT,
   lab text not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id)
);

create table timeslot (
   id bigint not NULL AUTO_INCREMENT,
   start_date_time text,
   duration text,
   lab_id bigint not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT lab_id_fk FOREIGN KEY (lab_id)
      REFERENCES lab (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

create table silo (
   id bigint not NULL AUTO_INCREMENT,
   silo text not NULL,
   lab_id bigint not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT lab_id_fk2 FOREIGN KEY (lab_id)
      REFERENCES lab (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE blueprint
(
   id bigint not NULL AUTO_INCREMENT,
   blueprint_name varchar(20) not NULL unique,
   CONSTRAINT id_pk PRIMARY KEY (id)
);

CREATE TABLE blueprint_instance_for_validation
(
   id bigint not NULL AUTO_INCREMENT,
   blueprint_id bigint not NULL,
   version text not NULL,
   layer text not NULL,
   layer_description text not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT blueprint_id_fk FOREIGN KEY (blueprint_id)
      REFERENCES blueprint (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE submission
(
   id bigint not NULL AUTO_INCREMENT,
   status text not NULL,
   jenkins_queue_job_item_url text,
   nexus_result_url text,
   blueprint_instance_for_validation_id bigint not NULL,
   timeslot_id bigint not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT blueprint_instance_for_validation_id_fk FOREIGN KEY (blueprint_instance_for_validation_id)
      REFERENCES blueprint_instance_for_validation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT timeslot_id_fk FOREIGN KEY (timeslot_id)
      REFERENCES timeslot (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (21, 'New Submission', 1, 10, 'newSubmission', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', 'icon-building-home');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (22, 'Committed Submissions', 1, 10, 'committedSubmissions', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', 'icon-building-home');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (23, 'Validation Results', 1, 10, 'report.htm', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', 'icon-misc-piechart');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (24, 'Get by submission id', 23, 10, 'getBySubmissionId', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (25, 'Get by blueprint name', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (26, 'Get by layer', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (27, 'Get by execution dates', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (28, 'Get by number of successful runs', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (29, 'Get by number of successful last runs', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (30, 'Get by number of failed last runs', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');
insert into fn_menu (MENU_ID, LABEL, PARENT_ID, SORT_ORDER, ACTION, FUNCTION_CD, ACTIVE_YN, SERVLET, QUERY_STRING, EXTERNAL_URL, TARGET, MENU_SET_CD, SEPARATOR_YN, IMAGE_SRC) VALUES (31, 'Get all', 23, 10, '', 'menu_tab', 'Y', NULL, NULL, NULL, NULL, 'APP', 'N', '/static/fusion/images/reports.png');

-- fn_user
Insert into fn_user (USER_ID,ORG_ID,MANAGER_ID,FIRST_NAME,MIDDLE_NAME,LAST_NAME,PHONE,FAX,CELLULAR,EMAIL,ADDRESS_ID,ALERT_METHOD_CD,HRID,ORG_USER_ID,ORG_CODE,LOGIN_ID,LOGIN_PWD,LAST_LOGIN_DATE,ACTIVE_YN,CREATED_ID,CREATED_DATE,MODIFIED_ID,MODIFIED_DATE,IS_INTERNAL_YN,ADDRESS_LINE_1,ADDRESS_LINE_2,CITY,STATE_CD,ZIP_CODE,COUNTRY_CD,LOCATION_CLLI,ORG_MANAGER_USERID,COMPANY,DEPARTMENT_NAME,JOB_TITLE,TIMEZONE,DEPARTMENT,BUSINESS_UNIT,BUSINESS_UNIT_NAME,COST_CENTER,FIN_LOC_CODE,SILO_STATUS) values (2,null,null,'akraino',null,null,null,null,null,null,null,null,null,'akraino',null,'akraino','akraino_password',now(),'Y',null,now(),1,now(),'N',null,null,null,'NJ',null,'US',null,null,null,null,null,10,null,null,null,null,null,null);

-- fn_role
Insert into fn_role (ROLE_ID,ROLE_NAME,ACTIVE_YN,PRIORITY) values (17,'Blueprint Validation UI user','Y',5);

-- fn_role_function
Insert into fn_role_function (ROLE_ID,FUNCTION_CD) values (17,'menu_home');
Insert into fn_role_function (ROLE_ID,FUNCTION_CD) values (17,'menu_tab');

-- fn_user_role
Insert into fn_user_role (USER_ID,ROLE_ID,PRIORITY,APP_ID) values (2,17,null,1);

commit;
