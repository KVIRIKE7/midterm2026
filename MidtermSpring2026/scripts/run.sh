#!/bin/bash
set -e
cd "$(dirname "$0")/.."
scripts/compile.sh
java -cp out Main "$@"