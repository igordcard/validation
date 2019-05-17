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

drop sequence IF EXISTS akraino.seq_timeslot;
drop sequence IF EXISTS akraino.seq_blueprint;
drop sequence IF EXISTS akraino.seq_blueprint_instance;
drop sequence IF EXISTS akraino.seq_submission;

drop table IF EXISTS akraino.submission;
drop table IF EXISTS akraino.blueprint_instance;
drop table IF EXISTS akraino.blueprint;
drop table IF EXISTS akraino.timeslot;

CREATE SCHEMA IF NOT EXISTS akraino
 AUTHORIZATION postgres;

CREATE TABLE akraino.timeslot
(
   timeslot_id bigint not NULL unique,
   start_date_time text not NULL,
   duration int not NULL,
   lab text not NULL
)
WITH (
  OIDS = FALSE
)
;
ALTER TABLE akraino.timeslot
  OWNER TO postgres;

CREATE TABLE akraino.blueprint
(
   blueprint_id bigint not NULL,
   blueprint_name text not NULL unique,
   CONSTRAINT blueprint_id_pk PRIMARY KEY (blueprint_id)
)
WITH (
  OIDS = FALSE
)
;
ALTER TABLE akraino.blueprint
  OWNER TO postgres;

CREATE TABLE akraino.blueprint_instance
(
   blueprint_instance_id bigint not NULL,
   blueprint_id bigint not NULL,
   version text not NULL,
   layer text not NULL,
   layer_description text not NULL,
   timeslot_id bigint not NULL unique,
   CONSTRAINT blueprint_instance_id_pk PRIMARY KEY (blueprint_instance_id),
   CONSTRAINT blueprint_id_fk FOREIGN KEY (blueprint_id)
      REFERENCES akraino.blueprint (blueprint_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT timeslot_id_fk FOREIGN KEY (timeslot_id)
      REFERENCES akraino.timeslot (timeslot_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS = FALSE
)
;
ALTER TABLE akraino.blueprint_instance
  OWNER TO postgres;

CREATE TABLE akraino.submission
(
   submission_id bigint not NULL,
   status text not NULL,
   jenkins_queue_job_item_url text,
   nexus_result_url text,
   blueprint_instance_id bigint not NULL,
   CONSTRAINT submission_id_pk PRIMARY KEY (submission_id),
   CONSTRAINT blueprint_instance_id_fk FOREIGN KEY (blueprint_instance_id)
      REFERENCES akraino.blueprint_instance (blueprint_instance_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS = FALSE
)
;
ALTER TABLE akraino.submission
  OWNER TO postgres;

CREATE SEQUENCE akraino.seq_timeslot
  START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE akraino.seq_blueprint
  START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE akraino.seq_blueprint_instance
  START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE akraino.seq_submission
  START WITH 1 INCREMENT BY 1;

insert into akraino.timeslot values(1, now(), 10, 0);  /* stands for AT&T lab */
insert into akraino.timeslot values(2, now(), 1000, 0); /* stands for AT&T lab */
insert into akraino.timeslot values(3, now(), 10000, 0); /* stands for AT&T lab */
insert into akraino.timeslot values(4, now(), 100000, 0); /* stands for AT&T lab */
insert into akraino.timeslot values(5, now(), 100000, 0); /* stands for AT&T lab */

insert into akraino.blueprint (blueprint_id, blueprint_name) values(1, 'dummy');
insert into akraino.blueprint (blueprint_id, blueprint_name) values(2, 'Unicycle');
insert into akraino.blueprint (blueprint_id, blueprint_name) values(3, 'REC');

insert into akraino.blueprint_instance (blueprint_instance_id, blueprint_id, version, layer, layer_description, timeslot_id) values(1, 1, '0.0.2-SNAPSHOT', 0, 'Dell Hardware', 1);  /* 0 Stands for hardware layer */
insert into akraino.blueprint_instance (blueprint_instance_id, blueprint_id, version, layer, layer_description, timeslot_id) values(2, 2, '0.0.1-SNAPSHOT', 0, 'Dell Hardware', 2); /* 0 Stands for hardware layer */
insert into akraino.blueprint_instance (blueprint_instance_id, blueprint_id, version, layer, layer_description, timeslot_id) values(3, 2, '0.0.7-SNAPSHOT', 1, 'CentOS Linux 7 (Core)', 3); /* 1 Stands for OS layer */
insert into akraino.blueprint_instance (blueprint_instance_id, blueprint_id, version, layer, layer_description, timeslot_id) values(4, 3, '0.0.4-SNAPSHOT', 2, 'K8s with High Availability Ingress controller', 4); /* 2 Stands for k8s layer */
insert into akraino.blueprint_instance (blueprint_instance_id, blueprint_id, version, layer, layer_description, timeslot_id) values(5, 3, '0.0.8-SNAPSHOT', 2, 'K8s with High Availability Ingress controller', 5); /* 2 Stands for k8s layer */

commit;
