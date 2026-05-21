#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$ROOT_DIR/src"
TEST_DIR="$ROOT_DIR/test"
BUILD_DIR="$ROOT_DIR/build"
TEST_CLASSES_DIR="$BUILD_DIR/test-classes"
SOURCES_FILE="$BUILD_DIR/test-sources.txt"
TEST_MAIN="io.github.promptt001.coordinatecracker.cracker.CoordinateCrackerRegressionTest"

require_tool() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "Error: '$1' was not found on PATH. Install a JDK and try again." >&2
        exit 1
    fi
}

require_tool javac
require_tool java

rm -rf "$TEST_CLASSES_DIR"
mkdir -p "$TEST_CLASSES_DIR"
mkdir -p "$BUILD_DIR"

find "$SRC_DIR" "$TEST_DIR" -name '*.java' | sort > "$SOURCES_FILE"

if [[ ! -s "$SOURCES_FILE" ]]; then
    echo "Error: no Java sources found under $SRC_DIR or $TEST_DIR" >&2
    exit 1
fi

echo "Compiling Java sources and tests..."
javac -encoding UTF-8 -d "$TEST_CLASSES_DIR" @"$SOURCES_FILE"

echo "Running regression tests..."
java -cp "$TEST_CLASSES_DIR" "$TEST_MAIN"
