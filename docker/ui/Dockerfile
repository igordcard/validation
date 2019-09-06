#
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM maven:3.6.1-jdk-11 as build
RUN git clone https://gerrit.akraino.org/r/validation /opt/akraino/validation
RUN cd /opt/akraino/validation/ui && mvn clean package

FROM tomcat:8.5.37
COPY  --from=build /opt/akraino/validation/ui/target/bluvalui.war /usr/local/tomcat/webapps
