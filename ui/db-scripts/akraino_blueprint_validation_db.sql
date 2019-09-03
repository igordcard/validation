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

DROP TABLE IF EXISTS blueprint_instance_for_validation;
DROP TABLE IF EXISTS blueprint;
DROP TABLE IF EXISTS silo;
DROP TABLE IF EXISTS timeslot;
DROP TABLE IF EXISTS lab;
DROP TABLE IF EXISTS w_robot_test_result;
DROP TABLE IF EXISTS validation_test_result;
DROP TABLE IF EXISTS submission;

create table lab (
   id bigint not NULL AUTO_INCREMENT,
   lab text not NULL unique,
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
   lab_id bigint not NULL unique,
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
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   unique (version, layer, blueprint_id)
);

CREATE TABLE submission
(
   id bigint not NULL AUTO_INCREMENT,
   status text not NULL,
   timeslot_id bigint not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT timeslot_id_fk FOREIGN KEY (timeslot_id)
      REFERENCES timeslot (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE validation_test_result
(
   id bigint not NULL AUTO_INCREMENT,
   blueprint_name varchar(20) not NULL,
   version text not NULL,
   lab_id bigint not NULL,
   timestamp text,
   all_layers boolean,
   optional boolean,
   result boolean,
   submission_id bigint,
   date_of_storage text,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT lab_id_fk3 FOREIGN KEY (lab_id)
      REFERENCES lab (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT submission_id_fk FOREIGN KEY (submission_id)
      REFERENCES submission (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   unique (timestamp, lab_id)
);

CREATE TABLE w_robot_test_result
(
   id bigint not NULL AUTO_INCREMENT,
   layer text not NULL,
   validation_test_result_id bigint not NULL,
   robot_test_results LONGTEXT,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT validation_test_result_id_fk FOREIGN KEY (validation_test_result_id)
      REFERENCES validation_test_result (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   unique (layer, validation_test_result_id)
);

commit;
