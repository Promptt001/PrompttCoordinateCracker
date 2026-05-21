#!/usr/bin/env python3
"""Verify that a Coordinate Cracker texture bundle has every required 16x16 PNG.

Usage:
    python3 tools/verify-texture-bundle.py src/assets/coordinatecracker/textures/block

The script intentionally checks structure and dimensions only. It does not ship,
fetch, or reconstruct any third-party texture pixels.
"""

from __future__ import annotations

import re
import struct
import sys
from pathlib import Path

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
ENUM_FILE = Path("src/io/github/promptt001/coordinatecracker/data/EnumBlockType.java")


def required_filenames() -> list[str]:
    if not ENUM_FILE.exists():
        raise SystemExit(f"Could not find {ENUM_FILE}; run this from the repository root.")
    text = ENUM_FILE.read_text(encoding="utf-8")
    names = sorted(set(re.findall(r'"([a-z0-9_]+\.png)"', text)))
    if not names:
        raise SystemExit(f"Could not find texture filenames in {ENUM_FILE}.")
    return names


def png_dimensions(path: Path) -> tuple[int, int]:
    with path.open("rb") as handle:
        signature = handle.read(8)
        if signature != PNG_SIGNATURE:
            raise ValueError("not a PNG")
        length = struct.unpack(">I", handle.read(4))[0]
        chunk_type = handle.read(4)
        if chunk_type != b"IHDR" or length < 8:
            raise ValueError("missing PNG IHDR")
        width, height = struct.unpack(">II", handle.read(8))
        return width, height


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print("Usage: python3 tools/verify-texture-bundle.py <texture-block-directory>", file=sys.stderr)
        return 2

    bundle = Path(argv[1])
    if not bundle.is_dir():
        print(f"ERROR: {bundle} is not a directory", file=sys.stderr)
        return 2

    missing: list[str] = []
    wrong_size: list[str] = []
    invalid: list[str] = []

    for name in required_filenames():
        path = bundle / name
        if not path.exists():
            missing.append(name)
            continue
        try:
            width, height = png_dimensions(path)
        except Exception as exc:  # noqa: BLE001 - diagnostic script
            invalid.append(f"{name}: {exc}")
            continue
        if (width, height) != (16, 16):
            wrong_size.append(f"{name}: {width}x{height}")

    if missing or wrong_size or invalid:
        if missing:
            print("Missing PNGs:")
            for name in missing:
                print(f"  - {name}")
        if wrong_size:
            print("Wrong dimensions; expected 16x16:")
            for item in wrong_size:
                print(f"  - {item}")
        if invalid:
            print("Invalid PNG files:")
            for item in invalid:
                print(f"  - {item}")
        return 1

    print(f"OK: {len(required_filenames())} required texture PNGs are present and 16x16.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
