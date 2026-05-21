#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
OUT="coordinatecracker-opencl-helper"
if [[ "$(uname -s)" == "Darwin" ]]; then
    cc -O2 -Wall -Wextra opencl_coordinatecracker.c -framework OpenCL -o "$OUT"
else
    cc -O2 -Wall -Wextra opencl_coordinatecracker.c -lOpenCL -o "$OUT"
fi
echo "Built gpu/$OUT"
