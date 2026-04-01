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
export IMAGE_REPOSITORY="gebish/ci"
export IMAGE_TAG="v13"

while getopts v: flag
do
    case "${flag}" in
        v) VERSION=${OPTARG};;
    esac
done

export IMAGE="${IMAGE_REPOSITORY}:${IMAGE_TAG}"

docker run -v ${WORKING_DIRECTORY}:${WORKING_DIRECTORY} -v ${HOME_DIRECTORY}/.gradle:/gradle-home -w ${WORKING_DIRECTORY} ${IMAGE} /bin/bash -c "Xvfb :99 -screen 1 1280x1024x16 -nolisten tcp > /dev/null 2>&1 & export DISPLAY=:99 ; ./gradlew --no-daemon --max-workers 4 --parallel $*"