#!/usr/bin/env bash
set -e -u -o pipefail

if [[ -n "${DEBUG-}" ]]; then
  set -x
fi

echo "Building for: ${WORKER_TYPE} ${BUILD_TARGET} ${SCRIPTING_TYPE}"

cd "$(dirname "$0")/../"

./gradlew build -x generateDocumentation