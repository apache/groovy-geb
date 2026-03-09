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
# test-reproducible-builds.sh - Verify that the Geb build is reproducible.
#
# Required by ASF Security policy for projects that build/sign on CI:
#   https://cwiki.apache.org/confluence/display/SECURITY/Reproducible+Builds
#
# Builds all jar artifacts twice from a clean state and compares SHA-256
# checksums. SOURCE_DATE_EPOCH is set from the last git commit to ensure
# timestamp-dependent outputs are deterministic.
#
# Any differing jars are preserved under etc/bin/results/ for inspection.
# That directory is git-ignored.
#
# Must be run from within a Geb git checkout. Takes no arguments.
#
# Usage:
#   etc/bin/test-reproducible-builds.sh
#

set -euo pipefail

export SOURCE_DATE_EPOCH=$(git log -1 --pretty=%ct)

CWD=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "${SCRIPT_DIR}/../.."

rm -rf "${SCRIPT_DIR}/results" || true
mkdir -p "${SCRIPT_DIR}/results/first"
mkdir -p "${SCRIPT_DIR}/results/second"

echo "================================================================"
echo " Testing Reproducible Builds for Apache Groovy Geb"
echo " SOURCE_DATE_EPOCH=${SOURCE_DATE_EPOCH}"
echo "================================================================"
echo ""

echo "Cleaning project..."
git clean -xdf --exclude='etc/bin' --exclude='.idea' --exclude='.gradle'

echo ""
echo "--- First build ---"
./gradlew jar --rerun-tasks --no-build-cache
find . -path ./etc -prune -o -type f -path '*/build/libs/*.jar' -print0 | while IFS= read -r -d '' f; do
  shasum -a 256 "$f"
done | sort > "${SCRIPT_DIR}/results/first.txt"
find . -path ./etc -prune -o -type f -path '*/build/libs/*.jar' -print0 | xargs -0 -I{} cp --parents {} "${SCRIPT_DIR}/results/first/" 2>/dev/null || \
  find . -path ./etc -prune -o -type f -path '*/build/libs/*.jar' -print0 | while IFS= read -r -d '' f; do
    mkdir -p "${SCRIPT_DIR}/results/first/$(dirname "$f")"
    cp "$f" "${SCRIPT_DIR}/results/first/$f"
  done

echo ""
echo "--- Cleaning for second build ---"
git clean -xdf --exclude='etc/bin' --exclude='.idea' --exclude='.gradle'

echo ""
echo "--- Second build ---"
./gradlew jar --rerun-tasks --no-build-cache
find . -path ./etc -prune -o -type f -path '*/build/libs/*.jar' -print0 | while IFS= read -r -d '' f; do
  shasum -a 256 "$f"
done | sort > "${SCRIPT_DIR}/results/second.txt"
find . -path ./etc -prune -o -type f -path '*/build/libs/*.jar' -print0 | while IFS= read -r -d '' f; do
    mkdir -p "${SCRIPT_DIR}/results/second/$(dirname "$f")"
    cp "$f" "${SCRIPT_DIR}/results/second/$f"
  done

echo ""
echo "--- Comparing builds ---"
cd "${SCRIPT_DIR}/results"
if diff -u first.txt second.txt > diff.txt 2>&1; then
  echo "✅ All jar artifacts are identical between the two builds."
  rm -f diff.txt
else
  echo "❌ Some jar artifacts differ between builds:"
  cat diff.txt
  echo ""
  echo "Differing artifacts have been preserved in:"
  echo "  ${SCRIPT_DIR}/results/first/"
  echo "  ${SCRIPT_DIR}/results/second/"
fi

cd "$CWD"
