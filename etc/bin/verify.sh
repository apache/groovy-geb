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
# verify.sh - End-to-end release verification for Apache Groovy Geb.
#
# Downloads staged artifacts from dist.apache.org, verifies their checksums
# and GPG signatures, checks for required files (LICENSE, NOTICE, README.md),
# and runs the Apache RAT license audit against the extracted source.
#
# The individual steps are delegated to companion scripts in this directory:
#   download-release-artifacts.sh  - fetches the source distribution and hashes
#   verify-source-distribution.sh  - checks integrity, signatures, and contents
#
# Usage:
#   verify.sh <dev|release> <version> [download-dir]
#
# Examples:
#   verify.sh dev 8.0.1 /tmp/geb-verify   # verify a staging candidate
#   verify.sh release 8.0.0               # verify a published release
#

set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 ['dev' or 'release'] [semantic.version] <optional download location>"
  echo ""
  echo "  Example: $0 dev 8.0.0 /tmp/geb-verify"
  exit 1
fi

DIST_TYPE=$1
VERSION=$2
DOWNLOAD_LOCATION="${3:-downloads}"

if [[ "${DIST_TYPE}" != "dev" && "${DIST_TYPE}" != "release" ]]; then
  echo "Error: DIST_TYPE must be either 'dev' or 'release', got '${DIST_TYPE}'"
  echo "Usage: $0 ['dev' or 'release'] [semantic.version] <optional download location>"
  exit 1
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
VERSION=${VERSION#v} # in case someone prefixes a v

cleanup() {
  echo "❌ Verification failed. ❌"
}
trap cleanup ERR

mkdir -p "${DOWNLOAD_LOCATION}"
DOWNLOAD_LOCATION="$(cd "${DOWNLOAD_LOCATION}" && pwd)"

echo "================================================================"
echo " Verifying Apache Groovy Geb ${VERSION} (${DIST_TYPE})"
echo "================================================================"
echo ""

echo "Downloading KEYS file ..."
curl -f -L -o "${DOWNLOAD_LOCATION}/SVN_KEYS" "https://dist.apache.org/repos/dist/release/groovy/KEYS"
echo "✅ KEYS Downloaded"
echo ""

echo "Downloading Artifacts ..."
"${SCRIPT_DIR}/download-release-artifacts.sh" "${DIST_TYPE}" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ Artifacts Downloaded"
echo ""

echo "Verifying Source Distribution ..."
"${SCRIPT_DIR}/verify-source-distribution.sh" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ Source Distribution Verified"
echo ""

echo "Using Java at ..."
which java
java -version
echo ""

PROJECT_ROOT="${SCRIPT_DIR}/../.."

echo "Applying License Audit (RAT) ..."
"${PROJECT_ROOT}/gradlew" -p "${DOWNLOAD_LOCATION}/src/groovy-geb-${VERSION}" rat
echo "✅ RAT passed"
echo ""

echo "================================================================"
echo " ✅✅✅ Automatic verification finished for Geb ${VERSION}."
echo "================================================================"
echo ""
echo "The extracted source is available at:"
echo "  ${DOWNLOAD_LOCATION}/src/groovy-geb-${VERSION}"
echo ""
echo "Next steps for manual verification:"
echo "  1. Review the extracted source"
echo "  2. Run the reproducible build test:"
echo "       ${SCRIPT_DIR}/test-reproducible-builds.sh"
echo "  ...which will build the project twice from the extracted source and compare jar checksums."
