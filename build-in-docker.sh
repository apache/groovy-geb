#!/usr/bin/env bash
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

export WORKING_DIRECTORY=`pwd`
export HOME_DIRECTORY=`echo ~`
export IMAGE="geb-build:latest"

# Remove existing container if it exists
docker rm -f geb-build-container 2>/dev/null || true

# For podman on macOS, we need to use --privileged and --security-opt to access the host's podman socket
# The socket will be available via podman's automatic socket forwarding
# Use --network=host so testcontainers can access other containers via localhost
docker run -it \
           --name geb-build-container \
           --privileged \
           --network=host \
           --security-opt label=disable \
           -v /var/run/docker.sock:/var/run/docker.sock:Z \
           -v ${WORKING_DIRECTORY}:${WORKING_DIRECTORY} \
           -v ${HOME_DIRECTORY}/.gradle:/gradle-home \
           -w ${WORKING_DIRECTORY} \
           ${IMAGE} \
           "$@"