#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$ROOT_DIR/src"
BUILD_DIR="$ROOT_DIR/build"
CLASSES_DIR="$BUILD_DIR/classes"
SOURCES_FILE="$BUILD_DIR/sources.txt"
JAR_FILE="$ROOT_DIR/Promptts_Coordinate_Cracker.jar"
MAIN_CLASS="io.github.promptt001.coordinatecracker.Main"

require_tool() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "Error: '$1' was not found on PATH. Install a JDK and try again." >&2
        exit 1
    fi
}

require_tool javac
require_tool jar
require_tool tar

rm -rf "$BUILD_DIR" "$JAR_FILE"
mkdir -p "$CLASSES_DIR"

find "$SRC_DIR" -name '*.java' | sort > "$SOURCES_FILE"

if [[ ! -s "$SOURCES_FILE" ]]; then
    echo "Error: no Java sources found under $SRC_DIR" >&2
    exit 1
fi

echo "Compiling Java sources..."
javac -encoding UTF-8 -d "$CLASSES_DIR" @"$SOURCES_FILE"

echo "Copying resources..."
(
    cd "$SRC_DIR"
    find . -type f ! -name '*.java' -print | tar -cf - -T -
) | (
    cd "$CLASSES_DIR"
    tar -xf -
)

echo "Building $JAR_FILE..."
jar cfe "$JAR_FILE" "$MAIN_CLASS" -C "$CLASSES_DIR" .

rm -rf "$BUILD_DIR"

echo "Successfully built $JAR_FILE"
