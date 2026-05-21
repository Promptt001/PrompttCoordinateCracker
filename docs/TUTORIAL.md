# Tutorial and workflow

A guided workflow for turning a screenshot into a constrained coordinate search.

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

## Practical scanning advice

- Start small: narrow Y range, small radius, CPU only.
- Save your pattern before long scans.
- Use more trusted observations instead of guessing.
- Lock view direction when it is known.
- Treat zero results as a data-quality signal first, not as proof the coordinate is outside the radius.
- Re-check the reference block, offsets, version, and wall facing before widening the search.
- Use GPU/SMT only after the pattern is known-good.
- Keep a note of the screenshot, reference block, version, search bounds, and texture pack used for each result file.
