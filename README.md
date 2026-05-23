# Promptt Coordinate Cracker

<p align="center">
  <img src="examples/GUI_20058x_82y_86z.png" alt="Promptt Coordinate Cracker GUI showing a Minecraft screenshot-derived pattern and scan controls" width="850">
</p>

**Promptt Coordinate Cracker** is a research-oriented Java/Swing application for studying coordinate-derived block-rendering states in Minecraft Java screenshots. It lets a researcher encode visible block rotations, mirrored side states, and model variants in a 7×7×7 local pattern, select search bounds, and scan for world coordinates that are consistent with the observed rendering states.

The project is designed as a reproducible coordinate-fingerprinting lab, not as a general seed cracker or camera-position estimator. It reports candidate block coordinates for the selected reference block under the chosen Minecraft version, surface mode, facing mode, and bounds.

Use this only for screenshots, events, worlds, and servers where coordinate inference is allowed. Dense patterns can reveal location information.

## Lineage, inspiration, and credit

Promptt Coordinate Cracker was inspired by [DerBejijing/BlockRotationExploit](https://github.com/DerBejijing/BlockRotationExploit). That project deserves clear credit for the core discovery and the first public proof-of-concept workflow this repository builds on.

### What BlockRotationExploit achieved

BlockRotationExploit identified and demonstrated that Minecraft Java's apparently random block rotations are deterministic functions of block coordinates. It framed the resulting coordinate exploit: if a screenshot contains enough readable rotated blocks, those rotations can be used to narrow the possible world coordinates where the screenshot was taken.

The original project also provided a working GPL-3.0 Java/Swing proof of concept with:

- a 7×7×7 relative pattern editor centered on a reference block;
- GUI controls for marking known and unknown block rotations;
- facing/rotation locking so known camera direction could reduce the search space;
- Y-range, X/Z radius, thread-count, Minecraft-version, and output-file controls;
- support for the legacy `1.12.2` and `1.16.5` coordinate-randomness formulas;
- pattern save/load behavior and coordinate result output;
- reusable matrix/vector helpers and build scripts;
- exploratory master/slave command-line modes, documented by the original as not functional yet.

The original README also openly described the project as unmaintained and beta-quality, including known reliability issues in the rotation handling. Promptt Coordinate Cracker does not erase that work; it extends the research direction that BlockRotationExploit made practical.

### What this version contributes

This repository keeps the same basic research premise—coordinate-derived rendering states can fingerprint locations—but expands it into a broader, more documented, and more defensive research tool. The main additions are a block-profile model, modern Minecraft `1.21.11` variant support, surface-aware matching, strict pattern validation, richer GUI feedback, a redesigned scanner, optional GPU and SMT acceleration paths, embedded texture previews, regression tests, and a full documentation set.

## Research scope

This tool answers one narrow question:

> If the reference block were at coordinate `(x, y, z)`, would Minecraft's deterministic block-model selection render every trusted observation in the same visible state entered in the editor?

A match means the entered observations are consistent with a candidate coordinate. It does not by itself prove screenshot origin, player position, camera orientation, seed, dimension, or server identity. Confidence depends on observation quality, search bounds, and the number of independent matches that remain.

## Highlights

- 7×7×7 pattern editor with per-cell block profiles and profile-aware state validation.
- Surface-aware scanning for wall/side, floor/top, and ceiling/bottom observations.
- Minecraft `1.21.11` model-variant support, while retaining legacy `1.12.2` and `1.16.5` predictors.
- Origin-outward CPU scanner with primitive compiled observations, result guardrails, and a word-level bitmask sieve for compatible patterns.
- Optional persistent OpenCL helper for compatible Minecraft `1.21.11` GPU scans.
- Optional exact SMT bit-vector backend for dense Minecraft `1.21.11` patterns.
- Plain-text `.pattern`-style files that support explicit block-profile tokens.
- Self-contained GUI texture previews generated from embedded Java ARGB data; original vanilla PNG assets are not committed.
- Dependency-free regression test entry point covering pattern parsing, offset transforms, scan-region coverage, GPU wire mappings, the CPU sieve, result ordering, and texture loading.

## Feature and optimization inventory

### Pattern editor and GUI workflow

- Keeps the original 7×7×7 relative-pattern concept, with the center tile of layer 4 treated as the reference block.
- Adds a separate block-profile matrix beside the visible-state matrix, so a cell can mean `deepslate:1`, `stone:0`, `sand_top:3`, and so on instead of only a raw rotation number.
- Adds block type and rotation/variant dropdowns for the selected tile.
- Adds left-click cycling, right-click clearing, toggle known/unknown, clear-tile, clear-current-layer, and clear-all-layers controls.
- Adds selected-tile status, pattern summary, warning text, tooltips, and a structured three-step layout: build pattern, configure search, run scan.
- Adds a results viewer table inside the GUI instead of relying only on the output file.
- Adds a **Copy /tp** button for the selected result row.
- Adds displayed speed, ETA, checked-candidate count, scan-volume estimate, and match-count updates.
- Adds a configurable **Max matches** guardrail so weak patterns cannot accidentally flood the GUI and output buffers without an explicit opt-in.

### Minecraft versions and block-profile model

- Preserves the legacy `1.12.2` and `1.16.5` coordinate predictors from the original lineage.
- Adds `1.21.11` support for modern coordinate-derived model-variant selection.
- Distinguishes raw Minecraft model-variant pools from visible GUI states. For example, a block can have four raw variants but only two readable side states, or only one useful side state.
- Implements explicit block profiles in `EnumBlockType` rather than treating every observed block as the same four-rotation grass-style signal.
- Supports useful side/wall candidates such as stone, infested stone, upright deepslate, upright infested deepslate, sculk, and bedrock.
- Supports top/bottom profiles for stone, infested stone, deepslate, infested deepslate, sculk, bedrock, dirt, sand, red sand, rooted dirt, grass block, podzol, mycelium, dirt path, and concrete powder colors.
- Marks one-state side profiles as non-selectable in the dropdown because they do not add coordinate information.
- Documents caveats for placement-sensitive deepslate axes, snowy grass/podzol/mycelium states, visually weak top/bottom candidates, and disabled candidates such as netherrack.
- Provides strict token lookup so unknown or removed static tokens do not silently default to a supported block profile.

### Surface and facing support

- Keeps the original idea that locking a known facing can reduce ambiguity, but generalizes it beyond the wall-facing-only model.
- Adds explicit surface modes for wall/side, floor/top, and ceiling/bottom observations.
- Adds canonical view directions for wall north/east/south/west, floor north/east/south/west, and ceiling north/east/south/west.
- Adds composite scan modes so the user can try all facings for the selected surface when direction is unknown.
- Adds offset transforms for wall planes and horizontal floor/ceiling planes, with regression tests covering the transforms.
- Adds profile/surface validation so mixed side/top/bottom profiles are flagged before a scan starts.
- Writes facing labels in output, such as `facing wall north` or `facing ceiling west`, so result files preserve the orientation assumption that matched.

### Pattern files and serialization

- Adds explicit `block_type:state` tokens, for example `deepslate:1`, `stone:0`, or `sand_top:3`.
- Keeps backward compatibility with older numeric-only pattern files by loading them as legacy deepslate-side style observations.
- Supports `?`, `.`, and `unknown` for ignored cells.
- Writes self-describing pattern files with supported-token comments.
- Performs strict load-time validation for layer count, row width, token names, state ranges, and unsupported static blocks.
- Separates pattern data into a state matrix and a block-type matrix through `PatternData` and `PatternCodec`.

### Texture and asset handling

- Adds texture previews for the GUI editor instead of relying only on abstract colored/rotated tiles.
- Uses embedded Java ARGB texture data for default vanilla previews, so the repository and jar do not ship original vanilla PNG assets.
- Allows deliberate texture overrides from a resource-pack zip, extracted assets folder, `assets/minecraft/textures/block`, `assets/coordinatecracker/textures/block`, or a direct block-PNG directory.
- Supports `-Dcoordinatecracker.assets=...`, the `COORDINATECRACKER_ASSETS` environment variable, and GUI-selected texture sources.
- Handles animated texture strips by using the first frame for the preview.
- Documents the asset policy and the limits of matching raw preview textures to real screenshots affected by tinting, lighting, shaders, mipmapping, perspective, scaling, or compression.

### CPU scanner redesign and optimizations

- Replaces the original fixed quadrant-style thread partition with origin-outward square-band scheduling over the full inclusive `[-radius, +radius]` search area.
- Writes completed scan regions in origin-outward order so nearby candidates are reported before distant ones even when worker threads finish out of order.
- Adds a tunable scan-band size through `-Dcoordinatecracker.scanBandSize=<blocks>`.
- Uses cooperative cancellation with interruption instead of the original deprecated `Thread.stop()` termination path.
- Uses atomic counters for matches, completed iterations, UI progress, and worker completion.
- Throttles progress and match-count UI updates to reduce Swing overhead during hot scans.
- Opens the result file once with a large buffered writer instead of reopening the file for every match.
- Buffers region matches and flushes them in stable scan order.
- Sorts matches within a region by X/Z distance from origin, then by tie-breakers, for deterministic, useful output order.
- Compiles editable GUI patterns into primitive `CompiledObservation` and `CompiledPattern` objects before scanning.
- Precomputes observation offsets, wanted states, visible-state mappings, model-variant counts, and view-direction metadata outside the candidate loop.
- Sorts observations by selectivity so the hot matcher checks stronger filters earlier and fails candidates sooner.
- Adds primitive-coordinate predictor overloads to avoid repeated `Vector3` allocation in hot paths.
- Reimplements the legacy `1.16.5` predictor with direct Java LCG arithmetic rather than allocating `java.util.Random` for every candidate observation.
- Adds a specialized `1.21.11` four-state variant fast path.
- Adds a word-level CPU sieve for compatible two-bit/four-state patterns using `StateMaskPlane`, `CandidateMask`, and `PlaneCache`.
- Uses cached per-Y state planes so repeated observations at related Y offsets can intersect bit masks instead of recomputing every candidate state independently.
- Chooses sieve chunk depth from a target memory budget through `-Dcoordinatecracker.sievePlaneBytes`.
- Adds match-limit and pending-buffer guardrails for underconstrained scans, including advanced tuning properties for large research runs.

### Optional GPU acceleration

- Adds an optional OpenCL acceleration path without making OpenCL a hard Java dependency.
- Uses an external helper executable in `gpu/opencl_coordinatecracker.c`, controlled by `-Dcoordinatecracker.gpuCommand=...`.
- Keeps a persistent helper process with an OpenCL context, program, and kernel alive across scan requests.
- Supports `CPU only`, `GPU auto`, and `GPU required` modes.
- In `GPU auto`, falls back to the CPU scanner when the helper is missing, unsupported, overflows, or fails.
- In `GPU required`, stops instead of falling back, which is useful for reproducible GPU-only experiments.
- Limits the GPU path to supported `1.21.11` four-state compatible observations and documents that support matrix.
- Includes GPU probe, timeout, max-match, pattern-count, observation-count, and scan-band tuning properties.
- Carries facing data through the GPU wire protocol so wall, floor, and ceiling matches can be labeled correctly.

### Optional exact SMT solver backend

- Adds an experimental exact bit-vector solver backend enabled by `-Dcoordinatecracker.smtSolverCommand="z3 -in"` or an equivalent QF_BV-capable command.
- Encodes the coordinate mixing and 48-bit LCG behavior for dense `1.21.11` patterns.
- Enumerates solver models and verifies each solver result with the normal Java matcher before accepting it.
- Falls back to the scanner on unsupported patterns, solver `unknown`, timeout, failure, or per-rectangle model-cap overflow.
- Provides tuning properties for solver timeout, max models per rectangle, and minimum observation count before trying SMT mode.

### Output, safety, and guardrails

- Outputs each candidate as `x y z facing <surface direction>`.
- Adds a GUI result table with a row cap separate from the full output file.
- Adds finite default max-match behavior to prevent accidental huge scans from overwhelming memory or the UI.
- Adds explicit error popups for invalid result paths, unavailable required GPU mode, scan failures, and guardrail trips.
- Treats underconstrained scans as a usability and resource-management problem, not just a raw brute-force problem.
- Keeps the responsible-use framing prominent because dense coordinate fingerprints can reveal location information.

### Build, tests, and documentation

- Keeps simple shell and batch build scripts for a dependency-free Java build.
- Adds `test.sh` and a dependency-free regression test suite.
- Adds long-form documentation in `docs/`, including getting started, tutorial, architecture, GPU setup, SMT setup, tuning, block-profile notes, pattern format, glossary, FAQ, asset policy, and maintainer review.
- Adds examples and screenshots under `examples/`.
- Adds a texture-bundle verification helper under `tools/`.
- Preserves a full-reference documentation file so information from the earlier long README split is not lost.

## Quick start

From a fresh clone with a JDK on `PATH`:

```bash
chmod +x test.sh build.sh
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

On Windows, build and run with:

```bat
build.bat
java -jar Promptts_Coordinate_Cracker.jar
```

The current repository does not include a native Windows test runner; `test.sh` can be run from a POSIX shell such as Git Bash or WSL.

If you only downloaded a release jar, run:

```bash
java -jar Promptts_Coordinate_Cracker.jar
```

## Basic workflow

1. Choose a visible reference block in the screenshot.
2. Place that reference block on the center tile of layer 4 in the 7×7×7 editor.
3. For each nearby observation you trust, set the block profile and visible rotation/variant.
4. Leave uncertain or unsupported blocks as unknown.
5. Choose Minecraft version, surface mode, facing mode, Y range, radius, thread count, acceleration mode, and max match limit.
6. Select or auto-generate a results file.
7. Start the scan and review the candidate coordinates in the results table and output file.

For a guided walkthrough, start with [Project walkthrough and methodology](docs/PROJECT_WALKTHROUGH.md), then use [Tutorial and workflow](docs/TUTORIAL.md) for the screenshot-to-scan procedure.

## Documentation

The detailed knowledge base lives in [`/docs`](docs/README.md). The documentation is organized so future Minecraft researchers can start from conceptual material and then move into setup, workflow, file formats, and implementation details.

| Need | Start here |
| --- | --- |
| Understand the whole project, research model, data flow, and limitations | [Project walkthrough and methodology](docs/PROJECT_WALKTHROUGH.md) |
| Install, build, test, and run the application | [Getting started](docs/GETTING_STARTED.md) |
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
| Verify nothing was removed from the original README split | [Full reference](docs/FULL_REFERENCE.md) |

## Repository layout

```text
src/       Java application source and texture-loading resources
gpu/       Optional OpenCL helper source and build scripts
test/      Dependency-free regression test entry point
docs/      Long-form documentation and maintainer knowledge base
examples/  Example pattern files and screenshots
tools/     Maintenance utilities
```

## Build requirements

- JDK with `javac`, `jar`, and `java` on `PATH`.
- `tar` for the POSIX build script.
- Optional: OpenCL runtime and compiler toolchain for GPU helper builds.
- Optional: an SMT solver executable if using the exact bit-vector backend.

## Texture preview note

The default build does not store or package original Minecraft texture PNG files. Instead, the GUI builds vanilla preview images from embedded Java ARGB pixel data generated from the previous vanilla texture bundle. Explicit user-supplied PNG/resource-pack sources are still supported for deliberate overrides. The coordinate predictor does not depend on image files. See [`docs/ASSET_POLICY.md`](docs/ASSET_POLICY.md).

## License

This project is licensed under the GPL-3.0 license. See [`LICENSE`](LICENSE). Minecraft is owned by Mojang/Microsoft.

Because this project is inspired by and builds from the research direction established by BlockRotationExploit, keep attribution to [DerBejijing/BlockRotationExploit](https://github.com/DerBejijing/BlockRotationExploit) intact in forks and derivative work.
