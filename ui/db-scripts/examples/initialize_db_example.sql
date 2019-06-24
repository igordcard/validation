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

insert into lab values(1, 0); /* 0 stands for AT&T lab */

insert into timeslot values(1, 'now', null, 1);

insert into silo values(1, 'att-blu-val', 1);

insert into blueprint (id, blueprint_name) values(1, 'dummy');
insert into blueprint (id, blueprint_name) values(2, 'Unicycle');
insert into blueprint (id, blueprint_name) values(3, 'REC');

insert into blueprint_instance_for_validation (id, blueprint_id, version, layer, layer_description) values(1, 1, 'latest', 0, 'Dummy Hardware');  /* 0 Stands for hardware layer */
insert into blueprint_instance_for_validation (id, blueprint_id, version, layer, layer_description) values(2, 3, 'latest', 0, 'AT&T Hardware'); /* 0 Stands for hardware layer */
insert into blueprint_instance_for_validation (id, blueprint_id, version, layer, layer_description) values(3, 3, 'latest', 1, 'OS of the AT&T platform'); /* 1 Stands for OS layer */
insert into blueprint_instance_for_validation (id, blueprint_id, version, layer, layer_description) values(4, 3, 'latest', 2, 'K8s of the AT&T platform'); /* 2 Stands for K8s layer */
insert into blueprint_instance_for_validation (id, blueprint_id, version, layer, layer_description) values(5, 3, 'latest', 7, 'All layers'); /* 7 Stands for all layers */
insert into blueprint_instance_for_validation (id, blueprint_id, version, layer, layer_description) values(6, 2, 'latest', 0, 'Unicycle Hardware'); /* 0 Stands for hardware layer */

commit;
