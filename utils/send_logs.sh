##############################################################################
# Copyright (c) 2019 AT&T Intellectual Property.                             #
#                                                                            #
# Licensed under the Apache License, Version 2.0 (the "License"); you may    #
# not use this file except in compliance with the License.                   #
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

# This has dependecy on ~/.netrc, this perticular file tested on ATT labs.
#$ cat ~/.netrc
#machine nexus.akraino.org login <username> password <password>


set -e -u
echo "==> send_logs.sh"

# Deploying logs to LFNexus log server ##
# BUILD_NUMBER and JOB_NAME should be set by Jenkins

NEXUS_URL=https://nexus.akraino.org
SILO=att-blu-val
JENKINS_HOSTNAME=http://192.168.62.220/
BUILD_URL="${JENKINS_HOSTNAME}/job/${JOB_NAME}/${BUILD_NUMBER}/"
#NEXUS_PATH="${SILO}/job/${JOB_NAME}/${BUILD_NUMBER}"
NEXUS_PATH="${SILO}/bluval_results/${BLUEPRINT}/${VERSION}/${TIMESTAMP}"

#mv /opt/akraino/validation/results /root/jenkins/workspace/validation/
zip -r results.zip results
echo "executing lftools deploy nexus-zip $NEXUS_URL logs $NEXUS_PATH results.zip"
lftools deploy nexus-zip $NEXUS_URL logs $NEXUS_PATH results.zip
rm results.zip
sudo rm -rf results

echo "executing lftools deploy logs $NEXUS_URL $NEXUS_PATH $BUILD_URL"
lftools deploy logs $NEXUS_URL $NEXUS_PATH $BUILD_URL

