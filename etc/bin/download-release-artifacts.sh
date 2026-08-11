#!/usr/bin/env bash
#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
#
# download-release-artifacts.sh - Download Geb release artifacts from dist.apache.org.
#
# Fetches the source distribution zip, its GPG signature (.asc), and checksum
# files (.sha256, .sha512) into a local directory for offline verification.
#
# Artifacts are downloaded from:
#   https://dist.apache.org/repos/dist/{dev|release}/groovy/geb/<version>/
#
# Usage:
#   download-release-artifacts.sh <dev|release> <version> [download-dir]
#

set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 ['dev' or 'release'] [semantic.version] <optional download location>"
  exit 1
fi

PROJECT_NAME='groovy-geb'
DIST_TYPE=$1
VERSION=$2
DOWNLOAD_LOCATION="${3:-downloads}"

if [[ "${DIST_TYPE}" != "dev" && "${DIST_TYPE}" != "release" ]]; then
  echo "Error: DIST_TYPE must be either 'dev' or 'release', got '${DIST_TYPE}'"
  echo "Usage: $0 ['dev' or 'release'] [version] <optional download location>"
  exit 1
fi

VERSION=${VERSION#v} # in case someone prefixes a v

echo "Downloading files to ${DOWNLOAD_LOCATION}"
mkdir -p "${DOWNLOAD_LOCATION}/src"

# Geb publishes a source distribution under the groovy dist area
BASE_URL="https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/geb/${VERSION}"

echo "Downloading source release files from ${BASE_URL} ..."
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip" \
  "${BASE_URL}/apache-${PROJECT_NAME}-src-${VERSION}.zip"
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip.asc" \
  "${BASE_URL}/apache-${PROJECT_NAME}-src-${VERSION}.zip.asc"
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip.sha256" \
  "${BASE_URL}/apache-${PROJECT_NAME}-src-${VERSION}.zip.sha256"
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip.sha512" \
  "${BASE_URL}/apache-${PROJECT_NAME}-src-${VERSION}.zip.sha512"

echo "✅ Source release artifacts downloaded to ${DOWNLOAD_LOCATION}/src"
