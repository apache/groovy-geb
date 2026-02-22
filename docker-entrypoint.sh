#!/bin/bash
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

# Check if Docker/Podman is available via the mounted socket
if [ -S /var/run/docker.sock ]; then
    echo "Docker socket found at /var/run/docker.sock"
else
    echo "ERROR: Docker socket not found at /var/run/docker.sock"
    echo "Make sure the host Docker/Podman socket is mounted"
    exit 1
fi

# Fix socket permissions to allow circleci user access
chmod 666 /var/run/docker.sock 2>/dev/null || true

# Start Xvfb for headless browser testing
Xvfb :99 -screen 1 1280x1024x16 -nolisten tcp > /dev/null 2>&1 &

# Drop to circleci user
exec gosu circleci "$@"