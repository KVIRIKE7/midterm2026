#!/bin/bash
set -e
cd "$(dirname "$0")/.."
scripts/compile.sh
echo ""
echo "--- Original self-test ---"
java -cp out Main --self-test
echo ""
echo "--- Characterization tests ---"
java -cp out CharacterizationTests