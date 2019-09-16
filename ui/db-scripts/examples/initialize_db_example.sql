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

insert into lab (id, lab, silo) values(1, 'att', 'att-blu-val');

insert into timeslot values(1, 'now', null, 1);

insert into blueprint (id, blueprint_name) values(1, 'dummy');
insert into blueprint (id, blueprint_name) values(2, 'rec');

insert into blueprint_layer (id, layer) values(1, 'hardware');
insert into blueprint_layer (id, layer) values(2, 'os');
insert into blueprint_layer (id, layer) values(3, 'container');
insert into blueprint_layer (id, layer) values(4, 'k8s');
insert into blueprint_layer (id, layer) values(5, 'helm');
insert into blueprint_layer (id, layer) values(6, 'openstack');

insert into blueprint_instance (id, blueprint_id, version) values(1, 1, 'master'); /* master version is assigned to dummy */
insert into blueprint_instance (id, blueprint_id, version) values(2, 2, 'master'); /* master version is assigned to rec */

insert into blueprint_instance_blueprint_layer (blueprint_instance_id, blueprint_layer_id) values(1, 1); /* hardware layer is assigned to dummy*/
insert into blueprint_instance_blueprint_layer (blueprint_instance_id, blueprint_layer_id) values(2, 1); /* hardware layer is assigned to rec*/
insert into blueprint_instance_blueprint_layer (blueprint_instance_id, blueprint_layer_id) values(2, 2); /* os layer is assigned to rec*/
insert into blueprint_instance_blueprint_layer (blueprint_instance_id, blueprint_layer_id) values(2, 4); /* k8s layer is assigned to rec*/

commit;
