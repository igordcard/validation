##############################################################################
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.        #
#                                                                            #
# Licensed under the Apache License, Version 2.0 (the "License");            #
# you maynot use this file except in compliance with the License.            #
#                                                                            #
# You may obtain a copy of the License at                                    #
#       http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                            #
# Unless required by applicable law or agreed to in writing, software        #
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  #
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           #
# See the License for the specific language governing permissions and        #
# limitations under the License.                                             #
##############################################################################

FROM alpine:latest
RUN apk --no-cache add --update git
RUN git clone https://gerrit.akraino.org/r/validation /opt/akraino/validation

FROM mariadb:10.4.6
COPY --from=0 /opt/akraino/validation/ui/db-scripts/EcompSdkDDLMySql_2_4_Common.sql /docker-entrypoint-initdb.d
COPY --from=0 /opt/akraino/validation/ui/db-scripts/EcompSdkDDLMySql_2_4_OS.sql /docker-entrypoint-initdb.d
COPY --from=0 /opt/akraino/validation/ui/db-scripts/EcompSdkDMLMySql_2_4_Common.sql /docker-entrypoint-initdb.d
COPY --from=0 /opt/akraino/validation/ui/db-scripts/EcompSdkDMLMySql_2_4_OS.sql /docker-entrypoint-initdb.d
COPY --from=0 /opt/akraino/validation/ui/db-scripts/akraino_blueprint_validation_db.sql /docker-entrypoint-initdb.d
