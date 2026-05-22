# Project walkthrough and methodology

This guide explains Promptt Coordinate Cracker as a complete research tool: what problem it studies, what data it accepts, how the scan works, how to set it up, and how to interpret results responsibly.

## 1. What the project is for

Promptt Coordinate Cracker studies a deterministic property of Minecraft Java rendering: some block models and texture orientations are selected from a coordinate-derived pseudo-random process. To a player, those rotations and variants look decorative. To a researcher, a sufficiently detailed patch of visible block states can behave like a coordinate fingerprint.

The tool lets a researcher manually translate a screenshot into a local pattern of trusted observations. It then asks whether each candidate world coordinate would produce the same visible block states under the selected Minecraft version and surface orientation.

The project is most useful for:

- controlled Minecraft screenshot-research experiments;
- validating whether a visible block pattern is coordinate-informative;
- comparing CPU, GPU, and exact-solver search approaches;
- documenting reproducible coordinate-fingerprinting workflows.

It is not designed to infer a world seed, player camera position, biome, dimension, server identity, or screenshot authenticity by itself.

## 2. Responsible-use boundary

Dense coordinate fingerprints can reveal location information. Use the tool only for screenshots, events, worlds, and servers where coordinate inference is allowed. For research writeups, describe the authorization context and avoid publishing sensitive coordinates from private servers or unpublished challenge material.

A result should be reported as a candidate coordinate under stated assumptions, not as absolute proof of origin.

## 3. Core research question

The scanner evaluates this question repeatedly:

```text
If the reference block were at candidate coordinate (x, y, z), would every known nearby observation match the visible state entered in the pattern editor?
```

The reference block is the center tile of layer 4 in the 7×7×7 editor. Every other known cell is stored as an offset from that center tile.

A match means the candidate coordinate is consistent with:

- the selected Minecraft version;
- the selected surface mode: wall/side, floor/top, or ceiling/bottom;
- the selected facing mode;
- the selected Y range and X/Z radius;
- the manually entered block profiles and visible states.

A match does not prove that the screenshot came from that coordinate unless the observations are strong, the bounds are complete, and alternative explanations have been considered.

## 4. Data flow from screenshot to result

The project can be understood as a pipeline:

```text
Screenshot
  ↓ manual classification
7×7×7 pattern matrix + block-profile matrix
  ↓ validation and serialization
Pattern file / GUI state
  ↓ observation compilation
Offsets, wanted visible states, profile mappings, version metadata
  ↓ candidate search
CPU matcher, optional GPU helper, or optional SMT solver
  ↓ verification and output
Candidate reference-block coordinates with matching surface/facing metadata
```

### Screenshot

The screenshot provides the visual evidence. The tool does not automatically classify image pixels. A human researcher decides which blocks are reliable enough to enter.

### Pattern matrix

The pattern editor stores two aligned 7×7×7 matrices:

- a state matrix, where each cell is unknown or a visible state value;
- a block-profile matrix, where each known cell has a supported block profile such as `deepslate`, `stone`, `sculk`, or a top/bottom profile.

Unknown cells are ignored during matching. Unsupported or uncertain blocks should remain unknown.

### Compiled observations

Before scanning, the GUI pattern is converted into compact observations. Each observation contains a relative offset, desired visible state, block-profile mapping, and Minecraft version. Sorting and compiling these observations lets the hot loop avoid expensive GUI-level logic.

### Candidate search

The scanner tests candidate reference coordinates within the chosen bounds. The CPU path is dependency-free and should be used first. Optional acceleration paths can reduce runtime for compatible dense patterns, but they should not replace a correctness pass on small bounds.

### Output

Each output coordinate is the candidate world position of the reference block. If facing is not locked, the same coordinate may appear under multiple compatible orientations.

## 5. Setup overview

### Minimum setup

Install a JDK that provides `javac`, `jar`, and `java`, then run:

```bash
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

On Windows:

```bat
build.bat
java -jar Promptts_Coordinate_Cracker.jar
```

Use Git Bash or WSL for `test.sh` on Windows.

### Optional GPU setup

GPU mode uses an external OpenCL helper rather than a bundled native Java binding. This keeps the main jar dependency-free. Use GPU mode only after a CPU scan verifies that the pattern and bounds are sensible.

Start with [GPU acceleration](GPU_ACCELERATION.md) when you need OpenCL setup details, support limits, and troubleshooting.

### Optional SMT setup

The SMT backend is for dense Minecraft `1.21.11` patterns where exact bit-vector reasoning may be useful. It requires an external QF_BV-capable solver, such as a `z3 -in` command. Start with [SMT solver backend](SMT_SOLVER_BACKEND.md).

## 6. How to prepare a research-quality scan

### 6.1 Record the screenshot context

For reproducibility, document:

- Minecraft version or the closest version hypothesis;
- whether the screenshot is from vanilla or a resource pack;
- lighting, shaders, scaling, compression, or video capture artifacts if known;
- the assumed surface plane: wall/side, floor/top, or ceiling/bottom;
- why the chosen reference block is identifiable;
- the search radius and Y range.

### 6.2 Select high-confidence observations

Only enter observations that can be classified confidently. A wrong known observation can eliminate the real coordinate. It is better to start with fewer trusted observations, verify that the scan returns plausible candidates, and then add observations gradually.

Prefer blocks whose visible states are meaningful for the selected surface. The block-profile dropdown is the practical source of truth. For detailed support information, read [Block profiles and textures](BLOCK_PROFILES_AND_TEXTURES.md).

### 6.3 Keep surface modes separate

Do not mix wall-side observations with top or bottom observations in the same scan. The GUI validates this because each surface mode uses a different mapping between editor offsets and world offsets.

### 6.4 Use small bounds first

A useful scan strategy is:

1. Use a tight Y range and small radius.
2. Confirm the pattern loads and scans correctly.
3. Lock facing if the screenshot orientation is known.
4. Increase radius or Y range only after the pattern is trusted.
5. Add observations gradually and rerun.

This keeps mistakes visible. If a broad scan returns zero matches, it is harder to know whether the problem is bounds, surface mode, facing, block classification, or a wrong observation.

## 7. How the matching logic works

The matcher implements Minecraft-version-specific predictors directly in Java. It does not load Minecraft runtime classes. For Minecraft `1.21.11`, the coordinate-derived rendering seed is computed from block coordinates and then used to select a raw model variant. The selected block profile maps that raw model variant into the visible state a user can enter in the GUI.

The scanner then checks every compiled observation against each candidate coordinate. A candidate fails immediately when the first observation disagrees. For compatible patterns, the CPU path can use a bitmask sieve that intersects packed rows of possible matches before emitting individual candidates.

For a deeper implementation map, read [Architecture and detailed design](ARCHITECTURE.md).

## 8. CPU, GPU, and SMT modes

| Mode | Best use | Notes |
| --- | --- | --- |
| CPU only | First pass, debugging, baseline scans | Dependency-free and easiest to reason about. |
| GPU auto | Large compatible Minecraft `1.21.11` scans after CPU validation | Falls back to CPU when helper support is unavailable or the pattern is unsupported. |
| GPU required | Reproducible GPU-only experiments | Stops instead of falling back, which is useful when testing the helper itself. |
| SMT backend | Dense exact-solver experiments | Optional external solver; final candidates are still verified by the normal Java matcher. |

The default recommendation is CPU first, acceleration second.

## 9. Interpreting results

### No matches

Common causes:

- one or more observations were misclassified;
- the reference block was not placed on the center tile of layer 4;
- the wrong surface mode or facing mode was selected;
- the real coordinate is outside the selected radius or Y range;
- a resource pack, shader, lighting condition, or compression artifact changed visual interpretation.

Reduce the pattern to the most trusted observations, scan small bounds, and add observations back gradually.

### Many matches

The pattern is underconstrained. Add reliable observations, use profiles with useful visible states, lock the facing if known, or reduce the search bounds.

### A small number of matches

A small candidate set is useful, but it is still conditional on the assumptions above. For research reporting, include the pattern file, settings, version, bounds, match count, and any manual classification caveats.

## 10. Suggested reproducibility checklist

When sharing results with another researcher, include:

- repository commit or release version;
- Java version and operating system;
- pattern file used for the scan;
- screenshot or annotated screenshot, if allowed;
- Minecraft version setting;
- surface mode and facing mode;
- Y min, Y max, radius, thread count, acceleration mode, and max match limit;
- texture source or resource pack details;
- output file and total number of matches;
- notes about uncertain observations that were left unknown.

This checklist makes it easier to distinguish an implementation issue from a data-entry or assumption issue.

## 11. Limitations and threats to validity

The main limitations are manual classification and scope. The application cannot know whether a screenshot has been edited, scaled, compressed, recolored, shader-processed, or captured with a resource pack. It also cannot prove that an entered pattern is complete or unbiased.

Technical limitations include:

- only supported block profiles contribute coordinate signal;
- surface modes must be modeled separately;
- unsupported Minecraft versions may need predictor work before they are scientifically useful;
- optional GPU and SMT modes have narrower support than the CPU baseline;
- weak patterns can produce many matches over large bounds.

Treat every output as a candidate result under explicit assumptions.

## 12. Where to go next

- Use [Getting started](GETTING_STARTED.md) to build and launch the application.
- Use [Tutorial and workflow](TUTORIAL.md) to perform a first scan.
- Use [Pattern file format](PATTERN_FILES.md) to inspect or edit saved patterns.
- Use [Block profiles and textures](BLOCK_PROFILES_AND_TEXTURES.md) to choose valid observations.
- Use [FAQ](FAQ.md) when a scan returns no matches, too many matches, or confusing orientation results.
- Use [Architecture and detailed design](ARCHITECTURE.md) when modifying the implementation.
