# Coordinate Cracker for Minecraft Java

A coordinate-fingerprinting lab for Minecraft Java screenshots.

Promptt's Coordinate Cracker is a Swing tool for testing whether deterministic Minecraft block-rendering states can identify candidate world coordinates. You mark observed block rotations, mirrored sides, and model variants in a 7×7×7 editor, choose search bounds, and scan for matching coordinates.

Use this only for screenshots, events, worlds, and servers where coordinate inference is allowed. Dense patterns can reveal location information.

## Highlights

- 7×7×7 pattern editor with block-type-aware state validation.
- Surface-aware search for wall, floor, and ceiling observations.
- Origin-outward CPU scanner with a word-level bitmask sieve.
- Optional persistent OpenCL helper for compatible 1.21.11 GPU scans.
- Optional exact SMT bit-vector backend for dense 1.21.11 patterns.
- Pattern save/load format that supports explicit block profile tokens.

## Quick start

```bash
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

On Windows, build and run with:

```bat
build.bat
java -jar Promptts_Coordinate_Cracker.jar
```

The current repository does not include a Windows test runner; `test.sh` can be run from a POSIX shell such as Git Bash or WSL.

If you only downloaded a release jar, run:

```bash
java -jar Promptts_Coordinate_Cracker.jar
```

## Basic workflow

1. Choose a reference block in the screenshot and place it on the center tile of layer 4.
2. For each visible observation, set the block profile and visible rotation/variant.
3. Choose the Minecraft version, surface mode, facing mode, Y range, radius, thread count, acceleration mode, and max match limit.
4. Select or auto-generate a results file.
5. Start the scan and review matches in the results table and output file.

## Documentation

The detailed knowledge base lives in [`/docs`](docs/README.md).

| Need | Start here |
| --- | --- |
| Install, build, test, run | [Getting started](docs/GETTING_STARTED.md) |
| Learn the screenshot-to-scan workflow | [Tutorial and workflow](docs/TUTORIAL.md) |
| Understand core vocabulary | [Glossary](docs/GLOSSARY.md) |
| Answer common setup and workflow questions | [FAQ](docs/FAQ.md) |
| Understand `.pattern` / text pattern files | [Pattern file format](docs/PATTERN_FILES.md) |
| Pick valid blocks and texture sources | [Block profiles and textures](docs/BLOCK_PROFILES_AND_TEXTURES.md) |
| Configure OpenCL GPU mode | [GPU acceleration](docs/GPU_ACCELERATION.md) |
| Configure the exact SMT solver | [SMT solver backend](docs/SMT_SOLVER_BACKEND.md) |
| Tune runtime flags and guardrails | [Tuning properties](docs/TUNING.md) |
| Understand internals and limitations | [Architecture and detailed design](docs/ARCHITECTURE.md) |
| Review documentation and maintenance standards | [Maintainer review guide](docs/MAINTAINER_REVIEW.md) |
| Verify nothing was removed from the original README | [Full reference](docs/FULL_REFERENCE.md) |

## Repository layout

```text
src/       Java application source and texture-loading resources
gpu/       Optional OpenCL helper source and build scripts
test/      Dependency-free regression test entry point
docs/      Long-form documentation and maintainer knowledge base
examples/  Example pattern files and screenshots
```

## Build requirements

- JDK with `javac`, `jar`, and `java` on `PATH`.
- `tar` for the POSIX build script.
- Optional: OpenCL runtime and compiler toolchain for GPU helper builds.
- Optional: an SMT solver executable if using the exact bit-vector backend.

## Texture preview note

The default build does not store or package original Minecraft texture PNG files. Instead, the GUI builds exact vanilla preview images from embedded Java ARGB pixel data generated from the previous vanilla texture bundle. Explicit user-supplied PNG/resource-pack sources are still supported for deliberate overrides. The coordinate predictor does not depend on image files. See [`docs/ASSET_POLICY.md`](docs/ASSET_POLICY.md).

## License

This project is licensed under the GPL-3.0 license. See [`LICENSE`](LICENSE). Minecraft is owned by Mojang/Microsoft.
