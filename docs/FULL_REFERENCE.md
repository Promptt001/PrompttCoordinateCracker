# Full reference

This file preserves the original long-form README content during the documentation split.

# Coordinate Cracker for Minecraft Java

A coordinate-fingerprinting lab for Minecraft Java screenshots.

## Intro: the block-rotation treasure hunt

Minecraft is very good at looking random while being completely deterministic. Many block textures and blockstate models are not chosen by true chance; they are picked from a coordinate-derived rendering seed. That means a cave wall, cliff face, or terrain screenshot can contain a tiny hidden fingerprint: a pattern of rotations, mirrored sides, and model variants that only appears at some world coordinates.

**Promptt's Coordinate Cracker** turns that premise into a hands-on Swing tool. You choose a reference block, mark the nearby visible block states in a 7×7×7 editor, set sane search bounds, and let the scanner find coordinates where Minecraft would render the same pattern.

Use it only for screenshots, events, worlds, and servers where coordinate inference is allowed. A dense enough pattern can reveal location information.

Fun optimization facts:

- The scanner works outward from world origin, so early results are usually the most plausible survival/server coordinates.
- Observations are compiled once into primitive offsets before scanning, avoiding matrix/enumeration lookups inside the hot loop.
- The CPU path can use a word-level bitmask sieve instead of checking every candidate one by one.
- Optional OpenCL mode keeps one persistent helper process alive instead of launching a new GPU process per work chunk.
- Dense 1.21.11 patterns can optionally be sent to an SMT bit-vector solver, then verified by the normal Java matcher before they are accepted.

Inspired by [DerBejijing/BlockRotationExploit](https://github.com/DerBejijing/BlockRotationExploit). Proudly assisted by AI.

## Table of contents

- [What this tool does](#what-this-tool-does)
- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Build, test, and run](#build-test-and-run)
  - [Linux and macOS](#linux-and-macos)
  - [Windows](#windows)
  - [Run the included jar](#run-the-included-jar)
- [Tutorial](#tutorial)
  - [1. Understand the exploit premise](#1-understand-the-exploit-premise)
  - [2. Pick a reference block](#2-pick-a-reference-block)
  - [3. Fill the 7×7×7 editor](#3-fill-the-777-editor)
  - [4. Use known block profiles only](#4-use-known-block-profiles-only)
  - [5. Choose Minecraft version and view direction](#5-choose-minecraft-version-and-view-direction)
  - [6. Set Y range, radius, threads, and max matches](#6-set-y-range-radius-threads-and-max-matches)
  - [7. Choose acceleration mode](#7-choose-acceleration-mode)
  - [8. Save or load a pattern file](#8-save-or-load-a-pattern-file)
  - [9. Choose a results file](#9-choose-a-results-file)
  - [10. Run, stop, and interpret results](#10-run-stop-and-interpret-results)
- [Pattern file format](#pattern-file-format)
- [Supported block profiles](#supported-block-profiles)
- [Texture sources](#texture-sources)
- [Optional GPU acceleration](#optional-gpu-acceleration)
  - [Linux GPU setup](#linux-gpu-setup)
  - [Windows GPU setup](#windows-gpu-setup)
  - [GPU support matrix](#gpu-support-matrix)
  - [GPU troubleshooting](#gpu-troubleshooting)
- [Optional SMT solver backend](#optional-smt-solver-backend)
- [Tuning properties](#tuning-properties)
- [Detailed design](#detailed-design)
  - [Design map](#design-map)
  - [1. Application architecture](#1-application-architecture)
  - [2. Runtime data model](#2-runtime-data-model)
  - [3. Pattern serialization](#3-pattern-serialization)
  - [4. GUI coordinate semantics](#4-gui-coordinate-semantics)
  - [5. View-direction transforms](#5-view-direction-transforms)
  - [6. Block-profile model](#6-block-profile-model)
  - [7. Minecraft predictors](#7-minecraft-predictors)
  - [8. Search scheduler](#8-search-scheduler)
  - [9. CPU matcher and sieve](#9-cpu-matcher-and-sieve)
  - [10. GPU backend](#10-gpu-backend)
  - [11. Exact bit-vector solver backend](#11-exact-bit-vector-solver-backend)
  - [12. Result ordering, cancellation, and match limits](#12-result-ordering-cancellation-and-match-limits)
  - [13. Texture loading](#13-texture-loading)
  - [14. Tests and validation](#14-tests-and-validation)
  - [15. Known limitations](#15-known-limitations)
- [Practical scanning advice](#practical-scanning-advice)
- [License](#license)

## What this tool does

Promptt's Coordinate Cracker searches for candidate world coordinates whose deterministic Minecraft block rendering states match a manually entered local pattern.

A normal workflow looks like this:

1. Open a screenshot.
2. Pick one visible block as the reference.
3. In the app, place that reference block at the center tile on depth layer 4.
4. Enter nearby blocks whose visible state can be classified confidently.
5. Choose the Minecraft version, possible wall/floor/ceiling surface orientation, and coordinate bounds.
6. Start the scan.
7. Review the candidate coordinates written to the result file and shown in the viewer.

The tool is most useful when you have several reliable observations from coordinate-randomized block profiles. Static blocks should be left unknown because they do not narrow the search.

## Requirements

Core application:

- Java JDK with `javac` and `jar` on your `PATH`.
- A desktop environment capable of running Swing.
- Bash for `test.sh` on Linux/macOS or Git Bash/WSL on Windows.

Optional GPU mode:

- C compiler.
- OpenCL headers.
- OpenCL ICD loader/import library.
- A GPU or CPU OpenCL runtime provided by your hardware vendor or OpenCL runtime package.

Optional SMT mode:

- A QF_BV-capable SMT solver command, such as `z3 -in`.
- Dense enough 1.21.11 observations to make solver mode worthwhile.

## Quick start

```bash
chmod +x test.sh build.sh
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

For a first scan:

1. Load `examples/TestPattern`.
2. Leave **MC version** on `1.21.11`.
3. Leave **Acceleration** on `CPU only`.
4. Use a small radius and a tight Y range.
5. Click **Start scan**.

## Build, test, and run

### Linux and macOS

```bash
chmod +x build.sh test.sh
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

`test.sh` compiles the source and regression tests, then runs:

```text
io.github.promptt001.coordinatecracker.cracker.CoordinateCrackerRegressionTest
```

`build.sh` compiles the Java sources, copies non-Java resources, and creates:

```text
Promptts_Coordinate_Cracker.jar
```

### Windows

From Command Prompt or PowerShell with a JDK on `PATH`:

```bat
build.bat
java -jar Promptts_Coordinate_Cracker.jar
```

Regression tests are currently provided through the Bash script. Use Git Bash or WSL to run:

```bash
./test.sh
```

### Run the included jar

When the jar is already present:

```bash
java -jar Promptts_Coordinate_Cracker.jar
```

## Tutorial

### 1. Understand the exploit premise

Minecraft Java uses coordinate-derived seeds for many blockstate/model choices. For some blocks, the same block type can render with different rotations or mirrored variants at different coordinates. A patch of blocks can therefore act like a coordinate fingerprint.

This tool checks candidate reference coordinates by asking:

> "If the reference block were at `(x, y, z)`, would every known nearby observation render with the visible state I entered?"

A match means the coordinate is consistent with the observed pattern. It does not prove that the screenshot came from that coordinate unless the pattern is strong and your observations are correct.

### 2. Pick a reference block

Choose a block in the screenshot that you can identify confidently. For wall, floor, or ceiling screenshots, use a block near the middle of the visible patch.

In the app:

- The reference block is the **center tile** of the editor.
- The reference block is on **pattern layer 4**.
- Every output line is a candidate coordinate for that reference block.

A good reference block makes offsets easier to count. Bad offset counting is one of the fastest ways to get zero results.

### 3. Fill the 7×7×7 editor

The GUI has a 7×7 grid and a pattern-layer spinner.

- **Layer 4** is the visible plane.
- In wall modes, lower/higher layers represent depth offsets into or out of the wall plane.
- In floor/top and ceiling/bottom modes, lower/higher layers represent vertical offsets below or above the visible horizontal plane.
- Grid columns are horizontal offsets from the reference block.
- Grid rows are vertical/screen-forward offsets from the reference block, depending on surface mode.
- The center tile on layer 4 is offset `(0, 0, 0)` from the reference block.

For each observation you trust:

1. Select the correct pattern layer.
2. Click the tile at the matching horizontal/vertical offset.
3. Select the correct **Block type**.
4. Select the observed **Rotation / variant** value.
5. Leave uncertain cells as **unknown**.

Controls:

| Control | Behavior |
| --- | --- |
| Left-click tile | Selects/cycles the tile's state. Unknown tiles use the current block-type brush. |
| Right-click tile | Clears the tile to unknown. |
| **Block type** dropdown | Sets the selected tile's block profile. Each row shows block name, face type, and available states. One-state profiles are omitted because they do not add useful coordinate signal. |
| **Rotation / variant** dropdown | Sets `unknown`, `0`, `1`, `2`, or `3`; the valid range depends on the selected block profile. |
| **Rotation -** / **Rotation +** | Cycles the selected tile state. |
| **Toggle known/unknown** | Switches between ignored and value `0`. |
| **Clear tile** | Clears the selected tile. |
| **Clear current layer** | Clears only the current pattern layer. |
| **Clear all layers** | Clears the full 7×7×7 pattern. |

Do not guess. A wrong known observation can eliminate the real coordinate.

### 4. Use known block profiles only

Only enter cells for supported coordinate-randomized block profiles. The dropdown is the source of truth: it separates side, top, bottom, and top/bottom profiles because a block with four raw model variants may have fewer readable states on a particular face.

Pattern-file tokens use the dropdown profile names. Examples:

```text
deepslate, deepslate_top, stone, stone_top, infested_stone, sculk, bedrock, sand_top, grass_block_top, grass_block_bottom, white_concrete_powder_top
```

Face-specific entries such as `deepslate_top`, `grass_block_top`, and `podzol_bottom` refer to top/bottom texture slots in the selected texture source. The UI warns in bold red and disables scanning when a pattern mixes incompatible face profiles, such as side and top observations in one selected plane. One-state side profiles are omitted from the dropdown because they do not add useful coordinate signal by themselves.

Static/non-randomized blocks should be left unknown. The loader intentionally rejects removed static tokens such as:

```text
tuff, gravel, granite, diorite, andesite, blackstone
```

Those blocks do not contribute coordinate-derived signal in this scanner.

### 5. Choose Minecraft version and view/surface

For modern screenshots and the included example pattern, use:

```text
MC version: 1.21.11
```

Legacy options are still present:

- `1.16.5`
- `1.12.2`

The modern mixed-block workflow is designed around `1.21.11`.

For **Surface**, choose exactly one visible plane:

- `Wall / side` for north/east/south/west vertical wall observations.
- `Floor / top` for top-face horizontal observations.
- `Ceiling / bottom` for bottom-face horizontal observations.

For **Facing**, either keep `try all selected facings` or lock the selected plane to `north`, `east`, `south`, or `west`. For example, `Floor / top` with `east` scans the floor-east transform only. `Wall / side` with `try all selected facings` scans the four wall-facing mappings.

The app no longer accepts multiple surface planes in one scan. This prevents impossible side/top/bottom mixes from silently producing bad searches.

### 6. Set Y range, radius, threads, and max matches

Use the smallest plausible search space.

| Setting | Meaning |
| --- | --- |
| **Y min** / **Y max** | Vertical world-coordinate range. Internally the scan uses `Y min` through `Y max - 1`. |
| **Radius** | Scans X and Z from `-radius` through `+radius`. |
| **Threads** | Worker count. The GUI exposes the supported thread counts from the application. |
| **Max matches** | Safety cap for weak patterns. A value of `0` disables the cap. |

Examples:

- Deep cave: try `-64..20`.
- Surface build: try `50..120`.
- Unknown modern world height: `-64..320`, but expect more work.
- First test run: small radius first, then widen only after the pattern is trusted.

The scan volume is roughly:

```text
(2 × radius + 1) × (Y max - Y min) × (2 × radius + 1)
```

The matcher tests one compiled orientation per selected surface/facing combination. `Wall / side` plus `try all selected facings` tests four mappings; locking a facing tests one mapping.

### 7. Choose acceleration mode

The **Acceleration** dropdown has three options:

| Mode | Behavior |
| --- | --- |
| **CPU only** | Default, dependency-free Java path. |
| **GPU auto** | Tries the OpenCL helper first, then falls back to CPU when unsupported or unavailable. |
| **GPU required** | Requires the OpenCL helper and stops with an error instead of falling back. |

Use **CPU only** for the first correctness pass. Switch to **GPU auto** after the pattern is known-good and your helper builds.

### 8. Save or load a pattern file

Use **Load pattern...** to load a text pattern file. The included example is:

```text
examples/TestPattern
```

Use **Save pattern...** to export the current 7×7×7 editor state. Saved files are plain text and can be reviewed or edited manually.

### 9. Choose a results file

Leave **Results file** blank to auto-create a timestamped file:

```text
coordinates_yyyy-MM-dd_HH-mm-ss.txt
```

Or enter a custom path. If no `.txt` extension is supplied, the GUI appends one.

The scanner appends to existing result files. Clear or rename old files when you want a fresh run.

### 10. Run, stop, and interpret results

Click **Start scan**.

During the scan, the UI shows:

- progress percentage;
- total matches;
- scan speed;
- ETA;
- checked candidate count;
- a result viewer table.

Click **Stop scan** to cancel safely. Cancellation is cooperative: workers are interrupted, GPU helpers are closed, and the result writer is flushed/closed.

Output format:

```text
x y z facing direction
```

Example:

```text
12 64 -8 facing north
12 64 -8 facing east
```

Each line is a candidate coordinate for the reference block at the center tile on pattern layer 4. The facing label is the surface orientation that matched.

Interpretation:

| Result | Usually means |
| --- | --- |
| Few matches | Pattern is strong enough to investigate manually. |
| Many matches | Pattern is underconstrained; add observations or reduce bounds. |
| Zero matches | A block state, block type, offset, version, Y range, or view direction may be wrong. |
| Same coordinate with multiple facings | View/surface was unset and more than one orientation mapping matched. |

## Pattern file format

Pattern files serialize all seven depth layers in visible GUI order.

Rules:

- Seven layers.
- Seven rows per layer.
- Seven whitespace-separated tokens per row.
- Dashed separator between complete layers.
- Blank lines and `#` comments are allowed.
- Unknown cells can be `?`, `.`, or `unknown`.

Token format:

```text
block_type:state
```

Example row:

```text
deepslate:1 stone:0 sand_top:2 ? ? sculk:1 dirt_top:3
```

Backward compatibility:

```text
0 1 4 4 0 1 0
```

Numeric-only legacy files load as deepslate side observations:

- `0`, `1`, `2`, `3` become deepslate side values.
- `4` becomes unknown.

The loader is intentionally strict. It rejects malformed dimensions, unsupported block tokens, invalid states, and removed static block tokens so bad data does not silently become a misleading scan.

## Supported block profiles

### Terms used in this section

| Term | Meaning |
| --- | --- |
| Raw pool | How many coordinate-selected model entries Minecraft can choose from. |
| Side states | Useful visible states on a vertical wall/side face. |
| Top states | Useful visible states on the upward/floor face. |
| Bottom states | Useful visible states on the downward/ceiling face. |
| Placement affects it? | Whether the player's placement direction changes the blockstate/model used. |

The important rule is:

```text
A block having 4 coordinate-randomized model variants does not automatically mean
it gives 4 readable rotations on every face.
```

For side cracking, sand-style `y=0/90/180/270` cube rotations collapse to one visible side state.

### Dropdown entries

The dropdown now shows only profiles with more than one useful GUI state. Names use the standard black list text instead of state-count coloring.

### Best side/wall candidates

For side/wall coordinate cracking, the genuinely useful vanilla candidates are:

```text
stone
infested_stone
deepslate, only when axis=y
infested_deepslate, only when axis=y
sculk
bedrock
```

`netherrack` is deliberately disabled for now. In 1.21.11 it has a 16-entry raw pool and needs a dedicated 16-entry mapping; treating it as a four-state block is wrong.

### Implemented cube-face profiles

| Block or group | Raw pool | Side profile | Top/bottom profile | Placement/state caveat | Practical verdict |
| --- | ---: | --- | --- | --- | --- |
| Stone | 4 | `stone`, 2 states | `stone_top`, 4 states | Placement does not affect the randomized variant. | Good side candidate; side is normal vs mirrored, not clean 0/90/180/270. |
| Infested Stone | 4 | `infested_stone`, 2 states | `infested_stone_top`, 4 states | Placement does not affect the randomized variant; visually identical to stone. | Useful only when you know it is infested. |
| Sculk | 4 | `sculk`, 2 states | `sculk_top`, 4 states | Placement does not affect the randomized variant. | Good side candidate; same visible-side class pattern as stone. |
| Bedrock | 4 | `bedrock`, 2 states | `bedrock_top`, 4 states | Placement does not affect the randomized variant, but bedrock is not normally survival-placeable. | Good if observable; side should be treated like stone/sculk, not as four clean side rotations. |
| Deepslate, `axis=y` | 4 | `deepslate`, 2 states | `deepslate_top`, 4 states | Placement can change the saved `axis`; natural terrain deepslate is normally the safe case. | Good, but only when upright. |
| Infested Deepslate, `axis=y` | 4 | `infested_deepslate`, 2 states | `infested_deepslate_top`, 4 states | Placement can change the saved `axis`; visually identical to normal deepslate. | Same caveat as deepslate. |
| Deepslate or Infested Deepslate, `axis=x/z` | 4 per axis | Not implemented as the upright side profile | Not implemented as the upright top/bottom profile | Player-placed sideways deepslate changes which faces show side texture vs end texture. | Do not interpret sideways player-placed deepslate as natural upright deepslate. |
| Netherrack | 16 | Disabled | Disabled | Placement does not affect the randomized variant. | Potentially strong, but needs dedicated 16-entry support. |
| Dirt | 4 | `dirt`, 1 state | `dirt_top`, 4 states | Placement does not affect the randomized variant. | Not useful on sides; top/bottom are technically rotated but weak visually. |
| Sand | 4 | `sand`, 1 state | `sand_top`, 4 states | Placement does not affect the randomized variant. | Not useful on sides; top/bottom are weak visually. |
| Red Sand | 4 | `red_sand`, 1 state | `red_sand_top`, 4 states | Placement does not affect the randomized variant. | Same as sand. |
| Rooted Dirt | 4 | `rooted_dirt`, 1 state | `rooted_dirt_top`, 4 states | Placement does not affect the randomized variant. | Not useful on sides; possible top/bottom candidate with careful visual aid. |
| Grass Block, `snowy=false` | 4 | `grass_block`, 1 state | `grass_block_top`, 4 states; `grass_block_bottom`, 4 states | Snow above changes `snowy` state. | Side is not useful. Top may be usable if grass-top rotation can be read; bottom is dirt-like. |
| Grass Block, `snowy=true` | 1 | Not useful | Not useful | Snow-covered side model overrides the normal randomized profile. | Treat as non-useful for cracking. |
| Podzol, `snowy=false` | 4 | `podzol`, 1 state | `podzol_top`, 4 states; `podzol_bottom`, 4 states | Snow above changes `snowy` state. | Side is not useful. Top is more plausible than grass/dirt; bottom is dirt-like. |
| Podzol, `snowy=true` | 1 | Not useful | Not useful | Snow-covered side model overrides the normal randomized profile. | Treat as non-useful for cracking. |
| Mycelium, `snowy=false` | 4 | `mycelium`, 1 state | `mycelium_top`, 4 states; `mycelium_bottom`, 4 states | Snow above changes `snowy` state. | Top-only candidate; side is not useful and bottom is dirt-like. |
| Mycelium, `snowy=true` | 1 | Not useful | Not useful | Snow-covered side model overrides the normal randomized profile. | Treat as non-useful for cracking. |
| Dirt Path | 4 | `dirt_path`, 1 state | `dirt_path_top`, 4 states; `dirt_path_bottom`, 4 states | Placement does not affect the randomized variant. | Side is not useful. Top may be usable; bottom is usually not practical. |
| Concrete Powder colors | 4 | `<color>_concrete_powder`, 1 state | `<color>_concrete_powder_top`, 4 states | Placement does not affect the randomized variant. | Same geometry class as sand/dirt: no side support signal; top/bottom may be technically usable but visually noisy. |

Concrete powder colors in this category:

```text
white_concrete_powder
orange_concrete_powder
magenta_concrete_powder
light_blue_concrete_powder
yellow_concrete_powder
lime_concrete_powder
pink_concrete_powder
gray_concrete_powder
light_gray_concrete_powder
cyan_concrete_powder
purple_concrete_powder
blue_concrete_powder
brown_concrete_powder
green_concrete_powder
red_concrete_powder
black_concrete_powder
```

Each color also has a top/bottom token formed by appending `_top`, such as `white_concrete_powder_top`. `_bottom` and `_top_bottom` aliases are accepted for the combined horizontal profile.

### Non-full-block randomized candidates

These exist in vanilla's coordinate-randomized model data, but they are excluded because this tool's workflow is for normal cube-face observations.

| Block | Raw coordinate pool | Why it is excluded |
| --- | ---: | --- |
| Lily Pad | 4 | Top-view object, not a wall/floor cube-face block. |
| Turtle Egg | 4 per `eggs`/`hatch` state | Top-view object with awkward state management. |
| Sea Pickle | 4 per `pickles`/`waterlogged` state | Object orientation, not a clean cube face. |
| Bamboo | 4 | Growth/state changes complicate the model. |
| Chorus Plant | Multipart randomized variants | Shape/connectivity makes it unsuitable for simple face rotation. |
| Fire / Soul Fire | Multipart randomized variants | Animated/multipart state and spread behavior make it unsuitable. |

### Placement behavior summary

Placement does **not** let the player choose the coordinate-randomized variant for these supported block families:

```text
stone, infested_stone, sculk, bedrock, dirt, sand, red_sand, rooted_dirt,
grass_block, podzol, mycelium, dirt_path, concrete_powder colors
```

Breaking and replacing the same block at the same coordinates should give the same coordinate-derived model choice, assuming the same blockstate.

Placement **does** matter for deepslate-like blocks because the saved blockstate can be:

```mcfunction
axis=x
axis=y
axis=z
```

For this project, use only:

```mcfunction
minecraft:deepslate[axis=y]
minecraft:infested_deepslate[axis=y]
```

A sideways player-placed deepslate block should not be interpreted as natural upright deepslate.

Snow can disable useful grass/podzol/mycelium randomization. For `grass_block`, `podzol`, and `mycelium`, `snowy=false` has the four-entry randomized pool, while `snowy=true` switches to the snow-covered side model and should be considered non-useful for cracking.

For serious scans, verify the visual mapping for your exact block face and texture source. Resource packs, lighting, shader effects, and compression can all make classification harder.

## Texture sources

The GUI uses preview textures for manual classification. The predictor itself does not depend on image files.

By default, the GUI uses embedded exact vanilla ARGB texture data from `EmbeddedVanillaTextures.java`. This keeps the app self-contained without requiring the original PNG files in the repository or jar.

Startup texture search order:

1. JVM property:
   ```bash
   -Dcoordinatecracker.assets=/path/to/resource-pack-or-assets
   ```
2. Environment variable:
   ```bash
   COORDINATECRACKER_ASSETS=/path/to/resource-pack-or-assets
   ```
3. Embedded vanilla ARGB texture data.
4. Last GUI-selected texture source stored in Java preferences, only if embedded data is unavailable.
5. Explicit missing-texture placeholders.

Supported external texture source shapes:

- resource-pack zip;
- extracted assets folder;
- directory containing `assets/minecraft/textures/block/*.png`;
- directory containing `assets/coordinatecracker/textures/block/*.png`;
- directory containing block PNGs directly.

In the GUI, click **Choose texture source...** to select a resource pack, licensed texture pack, or extracted assets folder.

Texture note: embedded vanilla previews and loaded PNGs are rendered without labels, borders, tinting, antialiasing, or direction-dot overlays. For exact screenshot matching, remember that raw vanilla asset pixels can still differ from a screenshot because of biome tint, lighting, shader/resource-pack changes, mipmapping, perspective, scaling, and compression. See [Asset policy](ASSET_POLICY.md).

## Optional GPU acceleration

GPU mode uses an external OpenCL helper instead of bundling a native Java binding into the main jar. The Java app sends scan rectangles and compiled observations to the helper over stdin; the helper returns matches over stdout.

Helper source:

```text
gpu/opencl_coordinatecracker.c
```

### Linux GPU setup

Install JDK/build tools:

```bash
sudo apt update
sudo apt install default-jdk build-essential
```

Install OpenCL headers and ICD loader. On Debian/Ubuntu-style systems:

```bash
sudo apt install opencl-headers ocl-icd-opencl-dev
```

Install or update your GPU vendor driver/runtime so OpenCL devices are visible.

Build the Java app and helper:

```bash
chmod +x build.sh test.sh gpu/build-opencl-helper.sh
./test.sh
./build.sh
./gpu/build-opencl-helper.sh
```

Run with the helper path:

```bash
java -Dcoordinatecracker.gpuCommand="$PWD/gpu/coordinatecracker-opencl-helper" \
  -jar Promptts_Coordinate_Cracker.jar
```

In the GUI, start with **GPU auto**.

### Windows GPU setup

Windows GPU mode is optional. The normal Java scanner works without it, so get the app running on CPU first and then add the GPU helper. GPU mode is a three-part setup:

1. **Java/JDK** for the main app and `build.bat`.
2. **MSVC** for compiling the native OpenCL helper executable.
3. **OpenCL runtime + development files** so Windows can both *run* OpenCL on your GPU and *build* against `CL/cl.h` / `OpenCL.lib`.

The most common Windows failure is mixing those requirements up. A GPU driver may provide the OpenCL runtime but not the headers and import library needed for compilation. Conversely, an SDK may provide `CL/cl.h` and `OpenCL.lib` but still cannot make a GPU appear if the vendor driver is missing or broken.

#### Recommended Windows setup path

For most users, use this route:

| Step | Install | Why |
| --- | --- | --- |
| 1 | A current JDK, such as [Eclipse Temurin](https://adoptium.net/temurin/releases/) | Provides `java`, `javac`, and `jar`. |
| 2 | [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) or [Visual Studio Community](https://visualstudio.microsoft.com/downloads/) | Provides Microsoft `cl.exe` and `link.exe`. |
| 3 | Your GPU vendor's current Windows driver | Provides the OpenCL runtime/ICD used at scan time. |
| 4 | OpenCL development files | Provides `CL/cl.h` and `OpenCL.lib` used at build time. |

Good OpenCL development-file choices are:

| Hardware / preference | Recommended dev-file source | Notes |
| --- | --- | --- |
| NVIDIA GPU | [CUDA Toolkit for Windows](https://docs.nvidia.com/cuda/cuda-installation-guide-microsoft-windows/) | Usually the simplest way to get `include\CL\cl.h` and `lib\x64\OpenCL.lib` on NVIDIA systems. |
| Vendor-neutral setup | [Khronos OpenCL SDK](https://github.com/KhronosGroup/OpenCL-SDK) | Brings together the Khronos headers, loader, samples, and docs. |
| Minimal/manual setup | [OpenCL-Headers](https://github.com/KhronosGroup/OpenCL-Headers) + [OpenCL-ICD-Loader](https://github.com/KhronosGroup/OpenCL-ICD-Loader) | Useful if you know exactly where you want headers and libraries installed. The ICD loader is not a GPU driver by itself. |

Khronos also publishes a [Windows OpenCL getting-started guide](https://github.com/KhronosGroup/OpenCL-Guide/blob/main/chapters/getting_started_windows.md) that is useful when you want to understand the SDK/header/loader split.

#### 1. Install and verify Java

Install a **JDK**, not only a JRE. The project build needs `javac` and `jar`.

Open **Command Prompt** or **PowerShell** and run:

```bat
java -version
javac -version
jar --help
```

Expected result:

- `java -version` prints the installed Java runtime.
- `javac -version` prints the compiler version.
- `jar --help` prints the jar tool help.

If `java` works but `javac` does not, Windows is probably finding a JRE or Microsoft Store Java alias instead of the JDK. Reinstall the JDK with the `PATH` option enabled, or add the JDK `bin` directory manually, for example:

```text
C:\Program Files\Eclipse Adoptium\jdk-XX.X.X.X-hotspot\bin
```

Then open a new terminal and verify again.

#### 2. Install MSVC build tools

The helper is a small native C program:

```text
gpu\opencl_coordinatecracker.c
```

The Windows build script compiles it with Microsoft `cl.exe`, so install either:

- [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/), or
- [Visual Studio Community](https://visualstudio.microsoft.com/downloads/).

In the Visual Studio Installer, select:

- **Desktop development with C++**.
- **MSVC v143 VS 2022 C++ x64/x86 build tools** or the current equivalent.
- **Windows 10 SDK** or **Windows 11 SDK**.

After installation, open this from the Start menu:

```text
x64 Native Tools Command Prompt for VS 2022
```

or:

```text
Developer PowerShell for VS 2022
```

Verify MSVC:

```bat
where cl
cl
where link
```

A normal `cmd.exe` or PowerShell window usually will **not** know where `cl.exe`, Windows SDK headers, or MSVC libraries are. The Visual Studio developer shell sets `PATH`, `INCLUDE`, and `LIB` for you. Microsoft documents this requirement in its MSVC command-line tooling docs.

#### 3. Install or update the GPU OpenCL runtime

Install the newest stable graphics driver for your actual GPU or integrated graphics. This is what makes an OpenCL GPU device visible at runtime.

| Hardware | Driver/runtime resource | Notes |
| --- | --- | --- |
| NVIDIA GeForce / RTX / Quadro | [NVIDIA driver downloads](https://www.nvidia.com/en-us/drivers/) and [NVIDIA OpenCL overview](https://developer.nvidia.com/opencl) | NVIDIA provides OpenCL through its display driver. NVIDIA documents OpenCL 3.0 support on R465 and later Windows/Linux drivers. |
| AMD Radeon / Ryzen graphics | [AMD Drivers and Support](https://www.amd.com/en/support/download/drivers.html) and [AMD Auto-Detect and Install Tool](https://www.amd.com/en/resources/support-articles/faqs/GPU-Driver-Autodetect.html) | Use the official AMD driver package for Windows 10/11. Laptop users may need the OEM driver if AMD's installer refuses the device. |
| Intel integrated / Arc graphics | [Intel OpenCL runtimes and driver notes](https://www.intel.com/content/www/us/en/developer/articles/tool/opencl-drivers.html) | Intel states that the Intel Graphics Compute Runtime for OpenCL is included with the Intel Graphics Driver package on Windows. |

This project's helper intentionally asks OpenCL for `CL_DEVICE_TYPE_GPU`. A CPU-only OpenCL runtime is not enough for acceleration here. It can be useful for diagnosing OpenCL in general, but `--probe` will still fail if no GPU OpenCL device is exposed.

#### 4. Install OpenCL headers and import library

The helper source includes:

```c
#include <CL/cl.h>
```

The Windows build links:

```bat
OpenCL.lib
```

So the compiler needs to find both:

```text
include\CL\cl.h
lib\x64\OpenCL.lib
```

Common install locations:

```text
C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\vXX.X\include\CL\cl.h
C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\vXX.X\lib\x64\OpenCL.lib
```

or, for a manually installed SDK:

```text
C:\OpenCL-SDK\include\CL\cl.h
C:\OpenCL-SDK\lib\OpenCL.lib
```

The exact SDK path depends on how you install it. The file names matter more than the directory names.

#### 5. Build the app and helper

From the repository root, inside **x64 Native Tools Command Prompt for VS 2022**:

```bat
build.bat
gpu\build-opencl-helper.bat
```

The helper build script runs the equivalent of:

```bat
cd gpu
cl /O2 /W3 opencl_coordinatecracker.c /Fe:coordinatecracker-opencl-helper.exe OpenCL.lib
```

Expected success output:

```text
Built gpu\coordinatecracker-opencl-helper.exe
```

After this, you should have:

```text
Promptts_Coordinate_Cracker.jar
gpu\coordinatecracker-opencl-helper.exe
```

#### 6. If `CL/cl.h` or `OpenCL.lib` is not found

If the SDK is installed but MSVC cannot find it, add the SDK paths to the current developer-shell session.

Example for CUDA Toolkit:

```bat
set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\vXX.X"
set "INCLUDE=%CUDA_PATH%\include;%INCLUDE%"
set "LIB=%CUDA_PATH%\lib\x64;%LIB%"
gpu\build-opencl-helper.bat
```

Replace `vXX.X` with the installed CUDA Toolkit version, such as `v12.5`.

Example for a custom SDK directory:

```bat
set "OPENCL_SDK=C:\OpenCL-SDK"
set "INCLUDE=%OPENCL_SDK%\include;%INCLUDE%"
set "LIB=%OPENCL_SDK%\lib;%LIB%"
gpu\build-opencl-helper.bat
```

If your SDK stores the library under `lib\x64`, use that instead:

```bat
set "LIB=%OPENCL_SDK%\lib\x64;%LIB%"
```

PowerShell equivalent:

```powershell
$env:OPENCL_SDK = "C:\OpenCL-SDK"
$env:INCLUDE = "$env:OPENCL_SDK\include;$env:INCLUDE"
$env:LIB = "$env:OPENCL_SDK\lib\x64;$env:LIB"
.\gpu\build-opencl-helper.bat
```

#### 7. Probe the helper before opening the GUI

Run the built helper directly:

```bat
gpu\coordinatecracker-opencl-helper.exe --probe
```

A working setup prints a GPU name, for example:

```text
OpenCL GPU device: NVIDIA GeForce RTX ...
```

or:

```text
OpenCL GPU device: AMD Radeon ...
```

or:

```text
OpenCL GPU device: Intel(R) Arc(TM) ...
```

If the helper prints:

```text
ERROR No OpenCL GPU device found.
```

then the helper executable launched correctly, but OpenCL did not expose a GPU device. Fix the GPU driver/runtime before changing Java settings. Useful checks:

```bat
where coordinatecracker-opencl-helper.exe
where clinfo
clinfo
```

`clinfo` is optional, but it is a good independent OpenCL diagnostic tool. On Windows, install it from a trusted package manager or from the [`clinfo` project](https://github.com/Oblomov/clinfo). Look for a GPU device under an NVIDIA, AMD, or Intel platform.

#### 8. Launch the app with the helper enabled

The Java app does not load OpenCL directly. You point it at the helper executable with this system property:

```text
-Dcoordinatecracker.gpuCommand=...
```

Command Prompt:

```bat
java "-Dcoordinatecracker.gpuCommand=%CD%\gpu\coordinatecracker-opencl-helper.exe" -jar Promptts_Coordinate_Cracker.jar
```

PowerShell:

```powershell
$helper = Join-Path (Get-Location) "gpu\coordinatecracker-opencl-helper.exe"
java "-Dcoordinatecracker.gpuCommand=$helper" -jar Promptts_Coordinate_Cracker.jar
```

If your project path contains spaces, keep the quotes exactly as shown.

In the GUI:

1. Load or enter a known-good pattern.
2. Select **GPU auto** first.
3. Run a small radius scan.
4. Switch to **GPU required** only when testing the GPU path. Required mode is useful because it reports GPU setup errors instead of silently falling back to CPU.

#### 9. Runtime troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| `cl is not recognized` | You are not in a Visual Studio developer shell. | Open **x64 Native Tools Command Prompt for VS 2022**. |
| `fatal error C1083: Cannot open include file: 'CL/cl.h'` | MSVC cannot see OpenCL headers. | Install an OpenCL SDK or add its `include` directory to `INCLUDE`. |
| `LINK : fatal error LNK1181: cannot open input file 'OpenCL.lib'` | MSVC cannot see the OpenCL import library. | Add the SDK `lib` or `lib\x64` directory to `LIB`. |
| `ERROR No OpenCL GPU device found.` | OpenCL is installed, but no GPU device is exposed. | Update/reinstall the vendor graphics driver; check Device Manager; verify with `clinfo`. |
| GUI falls back to CPU in **GPU auto** | The pattern or version is unsupported by the GPU backend, or the helper failed. | Try one small scan in **GPU required** to see the explicit error. |
| Helper works from terminal but not from Java | Bad path or missing quotes around `coordinatecracker.gpuCommand`. | Use an absolute quoted path to the `.exe`. |
| Laptop has both iGPU and dGPU but probe shows the wrong one | OpenCL returns the first GPU device found by the helper. | Update both drivers; use vendor control panels to prefer the discrete GPU for Java/terminal if needed. |
| Antivirus quarantines the helper | The helper is a locally compiled native executable. | Restore/allowlist only if you built it yourself from this repository. |

#### 10. Windows GPU limitations to keep in mind

- GPU mode currently targets optimized Minecraft `1.21.11` direct four-state and modulo-two side-profile paths.
- Unsupported versions, predictors, or observation shapes may intentionally fall back to CPU in **GPU auto**.
- The helper chooses a GPU OpenCL device, not a CPU OpenCL device.
- WSL is a different Linux userspace. Prefer the native Windows helper when running the native Windows GUI.
- Keep CPU mode available for correctness checks. GPU acceleration is a speed path, not a replacement for validating the pattern.

### GPU support matrix

Current GPU support intentionally targets the most useful hot path first:

| Feature | GPU support |
| --- | --- |
| Minecraft `1.21.11` | Supported for direct four-state and modulo-two hot paths. |
| Direct `0..3` variants | Supported. |
| Modulo-two visible mappings | Supported for deepslate/infested deepslate/stone/infested stone/sculk/bedrock-style side states. |
| `1.12.2` / `1.16.5` predictors | CPU fallback in auto mode. |
| One-state side profiles or non-four-state model counts | CPU fallback in auto mode; one-state observations do not add coordinate signal. |

`GPU auto` falls back to CPU when the helper is missing, unsupported for the selected pattern, times out, overflows its per-rectangle match buffer, or fails.

`GPU required` stops and reports the problem instead.

### GPU troubleshooting

| Symptom | Check |
| --- | --- |
| `CL/cl.h: No such file or directory` | Install OpenCL headers/development packages. |
| Linker error for `OpenCL` / `OpenCL.lib` | Install the ICD loader or SDK development package. |
| Helper builds but finds no device | Install/update GPU driver or CPU OpenCL runtime. |
| GUI falls back to CPU | Confirm version is `1.21.11` and observations fit the GPU support matrix. |
| Too many matches in one rectangle | Add observations, reduce bounds, or increase `coordinatecracker.gpuMaxMatches`. |

## Optional SMT solver backend

The exact bit-vector solver backend is experimental and opt-in. It can be useful for dense 1.21.11 patterns over very large rectangles.

Enable it with:

```bash
java -Dcoordinatecracker.smtSolverCommand="z3 -in" \
  -jar Promptts_Coordinate_Cracker.jar
```

Recommended first dense-pattern test:

```bash
java \
  -Dcoordinatecracker.smtSolverCommand="z3 -in" \
  -Dcoordinatecracker.smtMinObservations=12 \
  -jar Promptts_Coordinate_Cracker.jar
```

The solver backend:

- supports 1.21.11 observations;
- expects four-state raw pools with direct or modulo-two visible mappings;
- encodes Java overflow and 48-bit LCG behavior;
- enumerates solver models;
- verifies each model with the Java matcher;
- falls back to the scanner on `unknown`, failure, timeout, unsupported patterns, or model cap overflow.

It is not always faster. Use it when the pattern is strong and the brute-force scan area is large.

## Tuning properties

| Property | Default | Purpose |
| --- | ---: | --- |
| `coordinatecracker.assets` | unset | Explicit texture source path. |
| `coordinatecracker.maxMatches` | `1000000` | Global scan match cap. `0` disables it. |
| `coordinatecracker.scanBandSize` | `64` | Origin-outward scan band size for CPU/global scheduling. |
| `coordinatecracker.gpuCommand` | helper name on `PATH` | OpenCL helper command/path. |
| `coordinatecracker.gpuMaxMatches` | helper-defined/default backend value | Per-rectangle GPU match cap. |
| `coordinatecracker.gpuTimeoutSeconds` | `600` | GPU scan timeout. |
| `coordinatecracker.gpuProbeTimeoutSeconds` | `10` | GPU helper probe timeout. |
| `coordinatecracker.gpuScanBandSize` | `2048` | Larger default band size when GPU mode is available. |
| `coordinatecracker.sievePlaneBytes` | `4194304` | Target memory size used to choose CPU sieve chunk depth. |
| `coordinatecracker.smtSolverCommand` | unset | Enables the exact bit-vector solver backend. |
| `coordinatecracker.smtTimeoutSeconds` | `30` | Timeout per SMT query. |
| `coordinatecracker.smtMaxMatches` | `10000` | Solver model cap per rectangle. |
| `coordinatecracker.smtMinObservations` | `10` | Minimum observation count before trying SMT mode. |

Example:

```bash
java \
  -Dcoordinatecracker.maxMatches=50000 \
  -Dcoordinatecracker.scanBandSize=128 \
  -jar Promptts_Coordinate_Cracker.jar
```

## Detailed design

### Design map

This section is organized by subsystem so the table of contents is easier to navigate.

| Subsystem | Main files |
| --- | --- |
| Swing GUI and app entrypoint | `Main.java` |
| Block profiles and app enums | `EnumBlockType.java`, `EnumMCVersion.java`, `EnumRotation.java`, `EnumAccelerationMode.java` |
| Pattern file I/O | `PatternCodec.java`, `PatternData.java` |
| Active pattern model | `PatternRelative.java`, `Matrix3.java`, `Vector3.java` |
| Search orchestration | `CoordinateBruteforcer.java` |
| CPU matching and pattern compilation | `BruteforceThread.java` |
| GPU acceleration | `GpuAccelerationBackend.java`, `gpu/opencl_coordinatecracker.c` |
| Exact SMT backend | `ExactBitVectorSolverBackend.java` |
| Coordinate math | `MathHelper.java` |
| Texture previews | `TextureManager.java` |
| Regression tests | `CoordinateCrackerRegressionTest.java` |

### 1. Application architecture

The application is a plain Java/Swing desktop app. It builds with `javac` and `jar`; there is no Maven or Gradle requirement.

At runtime, `Main` owns the GUI state:

- editor grid;
- selected block type and rotation/variant;
- Y/radius/thread/match settings;
- acceleration mode;
- result file path;
- result viewer table;
- texture source state.

When a scan starts, `Main` constructs a `PatternRelative` from the current matrices and creates a `CoordinateBruteforcer`. The bruteforcer opens the result writer, initializes optional acceleration backends, builds scan regions, and launches `BruteforceThread` workers.

### 2. Runtime data model

The editor uses two aligned `7 × 7 × 7` matrices:

1. **Pattern matrix**
   - `0..3`: known GUI-visible state.
   - `4`: unknown/ignored.
2. **Block-type matrix**
   - Stores `EnumBlockType` ids.

The matrices share the same coordinate system and center. Unknown cells are skipped during matching even if they retain a block type internally. Retaining the block type makes editing smoother because an unknown tile can remember the current/default brush.

The matrix center is `(3, 3, 3)`, displayed as the center tile on depth layer 4.

### 3. Pattern serialization

`PatternCodec` reads and writes plain-text pattern files.

The loader:

- initializes a clean unknown pattern;
- accepts comments and blank lines;
- validates exact layer/row/token counts;
- parses `block_type:state` tokens;
- supports numeric legacy deepslate files;
- rejects unsupported block tokens;
- rejects removed static tokens with a clear error.

The writer emits the current matrix state as text with comments, seven layers, and dashed separators.

### 4. GUI coordinate semantics

For a visible wall-style observation:

```text
horizontal = patternX - centerX
vertical   = centerZ - patternZ
depth      = patternY - centerY
```

The vertical expression is inverted because GUI rows increase downward while world Y increases upward.

Layer 4 is the visible plane because the GUI spinner is 1-indexed for users while the backing matrix is 0-indexed.

### 5. View-direction transforms

`BruteforceThread.getObservationOffsetX/Y/Z` maps GUI offsets into world offsets. Wall modes preserve the original wall transform:

Facing north:

```text
(worldX, worldY, worldZ) = ( horizontal, vertical, -depth )
```

Facing east:

```text
(worldX, worldY, worldZ) = ( depth, vertical, horizontal )
```

Facing south:

```text
(worldX, worldY, worldZ) = ( -horizontal, vertical, depth )
```

Facing west:

```text
(worldX, worldY, worldZ) = ( -depth, vertical, -horizontal )
```

For floor/top and ceiling/bottom modes, columns and GUI-forward rows map into X/Z while the pattern layer maps into world Y. The compiler creates one compiled pattern per selected surface/facing combination. The UI now chooses one surface plane at a time and warns before scanning if face-specific block profiles conflict with that plane.

### 6. Block-profile model

`EnumBlockType` separates two concepts:

| Concept | Meaning |
| --- | --- |
| Model variant count | Raw count sampled from Minecraft's coordinate-derived model pool. |
| GUI state count | Number of visually meaningful values the user should enter. |

This is important because a block can have four raw model variants but only one, two, or four readable GUI states on the face being observed.

Two-state side profiles such as deepslate, infested deepslate, stone, infested stone, sculk, and bedrock use a modulo-two visible mapping:

```text
raw 0 or 2 -> GUI 0
raw 1 or 3 -> GUI 1
```

Four-state horizontal profiles use direct mapping:

```text
raw 0 -> GUI 0
raw 1 -> GUI 1
raw 2 -> GUI 2
raw 3 -> GUI 3
```

One-state side profiles map every raw state to GUI value `0`:

```text
raw 0, 1, 2, or 3 -> GUI 0
```

Static blocks are intentionally excluded because they do not reduce the coordinate search space. Netherrack is also excluded until a dedicated 16-entry mapping is implemented.

### 7. Minecraft predictors

`MathHelper` contains the version-specific coordinate state functions.

For 1.21.11, the modern rendering-seed path is:

```text
i = (x * 3129871) XOR (z * 116129781) XOR y
i = i * i * 42317861 + i * 11
seed = i >> 16
```

The seed initializes a Java-compatible 48-bit linear congruential generator. The selected block profile's model variant count is used as the `nextInt(bound)` value. The raw variant is then converted to the visible GUI state.

The implementation deliberately avoids depending on Minecraft runtime classes. Texture previews are optional GUI aids; the coordinate predictor is implemented directly and does not require Minecraft runtime classes or game assets.

Legacy predictors remain for `1.12.2` and `1.16.5` compatibility. They are useful for older challenge material but are not the main target of the mixed block-profile workflow.

### 8. Search scheduler

The scan range is:

```text
X: -radius through +radius
Y: yMin through yMax - 1
Z: -radius through +radius
```

`CoordinateBruteforcer` builds origin-outward square bands over X/Z. This preserves complete coverage while prioritizing coordinates near `(0, 0)`.

Region results are written in region order. If one worker finishes an outer band early, its output waits until earlier regions are complete, keeping result files stable and easier to read.

### 9. CPU matcher and sieve

`BruteforceThread` compiles the selected pattern before scanning. Each known observation becomes a primitive structure containing:

- `dx`, `dy`, `dz` offset from the reference block;
- wanted GUI state;
- model variant count;
- visible-state mapping;
- Minecraft version.

Observations are sorted by selectivity so stronger constraints are tested earlier.

The scalar matcher checks each candidate reference coordinate by applying every known observation. A candidate fails immediately on the first mismatch.

For compatible direct four-state, modulo-two, and one-state cases, the optimized CPU path can use a plane-cache sieve:

1. Build raw-state bitplanes for X/Z slices at a lookup Y.
2. Shift/intersect 64-bit row masks for each observation.
3. Emit only candidate bits that survive every constraint.

This reduces per-candidate overhead for dense patterns.

### 10. GPU backend

`GpuAccelerationBackend` talks to a persistent OpenCL helper process. The helper protocol sends scan rectangles, compiled observations, and facing ids, then receives match lines.

The helper mirrors the Java 1.21.11 four-variant logic, including Java-specific overflow and 48-bit random behavior. Java still controls scheduling, fallback behavior, result ordering, and UI updates.

GPU mode is optional. The default Java jar remains dependency-free.

### 11. Exact bit-vector solver backend

`ExactBitVectorSolverBackend` is opt-in through:

```bash
-Dcoordinatecracker.smtSolverCommand="z3 -in"
```

It renders compatible dense 1.21.11 constraints as QF_BV formulas. The encoding preserves:

- 32-bit overflow for `x * 3129871` before sign extension;
- 64-bit wrapped rendering-seed arithmetic;
- arithmetic right shift for `seed = i >> 16`;
- 48-bit LCG masking;
- modulo/direct visible mappings.

Solver models are not trusted blindly. Each `(x, y, z)` model is checked by the standard Java matcher before being emitted. Unsupported solver cases fall back to the normal scanner.

### 12. Result ordering, cancellation, and match limits

Matches are written as plain text:

```text
x y z facing direction
```

The app appends to the selected result file.

Multiple workers can find matches at the same time, so `CoordinateBruteforcer` buffers matches by region and writes completed regions in order.

The scan can be stopped from the GUI. Cancellation:

- sets a shared cancellation flag;
- interrupts worker threads;
- closes optional GPU helpers;
- flushes/closes the writer;
- returns the UI to the idle state.

The default match cap protects the GUI and ordered buffers from unbounded memory growth on weak patterns. Adjust it with the UI or `coordinatecracker.maxMatches`.

### 13. Texture loading

`TextureManager` isolates texture lookup from scan logic. By default, it builds block preview images from embedded vanilla ARGB source data rather than PNG resources. It can also load block preview images from:

- explicit JVM property path;
- environment variable path;
- a GUI-selected source for deliberate custom/version-specific overrides;
- resource-pack or extracted-assets layouts that contain block PNGs;
- explicit missing-texture placeholders if neither embedded nor external exact data is available.

When an external texture source is partial, loaded textures render pixel-for-pixel and missing entries show explicit placeholders. Missing exact textures do not affect scan correctness, but they do reduce editor usefulness for screenshot transcription.

### 14. Tests and validation

The regression test suite covers core behaviors including:

- strict block token handling;
- removed static token classification;
- block id validation;
- pattern codec round-trip and malformed-file rejection;
- wall-facing offset transforms;
- facing labels;
- matrix rotation/copy behavior;
- origin-outward scan region coverage.

Run it with:

```bash
./test.sh
```

### 15. Known limitations

- The tool does not classify screenshots automatically.
- Visual state classification must be verified by the user.
- Resource packs and shaders can invalidate vanilla visual assumptions.
- Static/non-randomized blocks should be left unknown. One-state side profiles are accurate labels but do not narrow a search.
- Broad bounds with weak patterns can generate huge result sets.
- The result file can contain duplicate coordinates with different facing labels when direction is unset.
- GPU mode targets the 1.21.11 four-state and modulo-two hot paths; one-state side profiles and unsupported profiles fall back to CPU or are skipped by the accelerator.
- SMT mode is experimental and not always faster.
- Network master/slave modes remain placeholders rather than the primary workflow.

## Practical scanning advice

- Start small: narrow Y range, small radius, CPU only.
- Save your pattern before long scans.
- Use more trusted observations instead of guessing.
- Lock view direction when it is known.
- Treat zero results as a data-quality signal first, not as proof the coordinate is outside the radius.
- Re-check the reference block, offsets, version, and wall facing before widening the search.
- Use GPU/SMT only after the pattern is known-good.
- Keep a note of the screenshot, reference block, version, search bounds, and texture pack used for each result file.

## License

This project is licensed under the GNU General Public License v3. See [`LICENSE`](../LICENSE).
