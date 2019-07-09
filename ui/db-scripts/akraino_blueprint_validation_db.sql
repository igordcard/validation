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

commit;
