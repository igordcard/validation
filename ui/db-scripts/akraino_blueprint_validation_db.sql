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

DROP TABLE IF EXISTS w_robot_test_result;
DROP TABLE IF EXISTS validation_test_result;
DROP TABLE IF EXISTS submission;
DROP TABLE IF EXISTS blueprint_instance_blueprint_layer;
DROP TABLE IF EXISTS blueprint_instance;
DROP TABLE IF EXISTS blueprint_layer;
DROP TABLE IF EXISTS blueprint;
DROP TABLE IF EXISTS timeslot;
DROP TABLE IF EXISTS blueprint_instance_timeslot;
DROP TABLE IF EXISTS lab;

create table lab (
   id bigint not NULL AUTO_INCREMENT,
   lab text not NULL unique,
   silo text not NULL unique,
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

CREATE TABLE blueprint
(
   id bigint not NULL AUTO_INCREMENT,
   blueprint_name varchar(20) not NULL unique,
   CONSTRAINT id_pk PRIMARY KEY (id)
);

CREATE TABLE blueprint_layer
(
   id bigint not NULL AUTO_INCREMENT,
   layer text not NULL unique,
   CONSTRAINT id_pk PRIMARY KEY (id)
);

CREATE TABLE blueprint_instance
(
   id bigint not NULL AUTO_INCREMENT,
   blueprint_id bigint not NULL,
   version text not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT blueprint_id_fk FOREIGN KEY (blueprint_id)
      REFERENCES blueprint (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   unique (version, blueprint_id)
);

CREATE TABLE blueprint_instance_blueprint_layer
(
   blueprint_instance_id bigint not NULL,
   blueprint_layer_id bigint not NULL,
   CONSTRAINT blueprint_instance_id_fk2 FOREIGN KEY (blueprint_instance_id)
      REFERENCES blueprint_instance (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT blueprint_layer_id_fk FOREIGN KEY (blueprint_layer_id)
      REFERENCES blueprint_layer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   unique (blueprint_instance_id, blueprint_layer_id)
);

CREATE TABLE blueprint_instance_timeslot
(
   blueprint_instance_id bigint not NULL,
   timeslot_id bigint not NULL,
   CONSTRAINT blueprint_instance_id_fk3 FOREIGN KEY (blueprint_instance_id)
      REFERENCES blueprint_instance (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT timeslot_id_fk FOREIGN KEY (timeslot_id)
      REFERENCES timeslot (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   unique (blueprint_instance_id, timeslot_id)
);

CREATE TABLE submission
(
   id bigint not NULL AUTO_INCREMENT,
   status text not NULL,
   timeslot_id bigint not NULL,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT timeslot_id_fk2 FOREIGN KEY (timeslot_id)
      REFERENCES timeslot (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE validation_test_result
(
   id bigint not NULL AUTO_INCREMENT,
   blueprint_instance_id bigint not NULL,
   all_layers boolean,
   lab_id bigint not NULL,
   timestamp text,
   optional boolean,
   result boolean,
   submission_id bigint,
   date_of_storage text,
   CONSTRAINT id_pk PRIMARY KEY (id),
   CONSTRAINT lab_id_fk2 FOREIGN KEY (lab_id)
      REFERENCES lab (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT submission_id_fk FOREIGN KEY (submission_id)
      REFERENCES submission (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT blueprint_instance_id_fk FOREIGN KEY (blueprint_instance_id)
      REFERENCES blueprint_instance (id) MATCH SIMPLE
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
