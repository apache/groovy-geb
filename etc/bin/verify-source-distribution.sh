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
# verify-source-distribution.sh - Verify a downloaded Geb source distribution.
#
# Expects the download directory to contain SVN_KEYS (the Groovy project KEYS
# file) and a src/ subdirectory with the zip, .asc, and .sha256 files, as
# produced by download-release-artifacts.sh.
#
# Performs the following checks:
#   1. SHA-256 checksum verification
#   2. GPG signature verification (using an isolated temporary keyring)
#   3. Extraction and presence of LICENSE, NOTICE, and README.md
#
# Usage:
#   verify-source-distribution.sh <version> [download-dir]
#

set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 [semantic.version] <optional download location>"
  exit 1
fi

VERSION=$1
DOWNLOAD_LOCATION="$(cd "${2:-downloads}" && pwd)"

VERSION=${VERSION#v} # in case someone prefixes a v

cd "${DOWNLOAD_LOCATION}/src"
ZIP_FILE="apache-groovy-geb-src-${VERSION}.zip"

if [ ! -f "${ZIP_FILE}" ]; then
  echo "Error: Could not find ${ZIP_FILE} in ${DOWNLOAD_LOCATION}/src"
  exit 1
fi

export GEB_GPG_HOME=$(mktemp -d)
cleanup() {
  rm -rf "${GEB_GPG_HOME}"
}
trap cleanup EXIT

echo "Verifying SHA-256 checksum..."
EXPECTED_HASH=$(cat "${ZIP_FILE}.sha256" | awk '{print $1}' | tr -d '\r\n')
ACTUAL_HASH=$(shasum -a 256 "${ZIP_FILE}" | awk '{print $1}')
if [ "${EXPECTED_HASH}" != "${ACTUAL_HASH}" ]; then
    echo "❌ SHA-256 checksum verification failed"
    echo "  Expected: ${EXPECTED_HASH}"
    echo "  Actual:   ${ACTUAL_HASH}"
    exit 1
fi
echo "✅ SHA-256 Checksum Verified"

echo "Importing GPG keys to independent GPG home ..."
gpg --homedir "${GEB_GPG_HOME}" --import "${DOWNLOAD_LOCATION}/SVN_KEYS"
echo "✅ GPG Keys Imported"

echo "Verifying GPG signature..."
gpg --homedir "${GEB_GPG_HOME}" --verify "${ZIP_FILE}.asc" "${ZIP_FILE}"
echo "✅ GPG Signature Verified"

SRC_DIR="groovy-geb-${VERSION}"

if [ -d "${SRC_DIR}" ]; then
  echo "Previous source directory found, removing..."
  rm -rf "${SRC_DIR}"
fi

echo "Extracting zip file..."
unzip -q "${ZIP_FILE}"

if [ ! -d "${SRC_DIR}" ]; then
  echo "Error: Expected extracted folder '${SRC_DIR}' not found."
  exit 1
fi

echo "Checking for required files..."
REQUIRED_FILES=("LICENSE" "NOTICE" "README.md")

for FILE in "${REQUIRED_FILES[@]}"; do
  if [ ! -f "${SRC_DIR}/${FILE}" ]; then
    echo "❌ Missing required file: ${FILE}"
    exit 1
  fi
  echo "✅ Found required file: ${FILE}"
done

echo "✅ All source distribution checks passed for Apache Groovy Geb ${VERSION}."
