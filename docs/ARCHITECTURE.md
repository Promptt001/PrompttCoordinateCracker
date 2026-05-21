# Architecture and detailed design

Implementation notes for the GUI, search scheduler, CPU/GPU backends, serialization, and validation strategy.

## Detailed design

### Design map

This section is organized by subsystem so the table of contents is easier to navigate.

| Subsystem | Main files |
| --- | --- |
| Swing GUI and app entrypoint | `Main.java`, `gui/TileIconRenderer.java`, `gui/SquareGridPanel.java`, `gui/BlockTypeListCellRenderer.java` |
| Block profiles and app enums | `EnumBlockType.java`, `EnumMCVersion.java`, `EnumRotation.java`, `EnumAccelerationMode.java` |
| Pattern file I/O | `PatternCodec.java`, `PatternData.java` |
| Active pattern model | `PatternRelative.java`, `Matrix3.java`, `Vector3.java` |
| Search orchestration | `CoordinateBruteforcer.java` |
| CPU matching and pattern compilation | `BruteforceThread.java`, `ObservationCompiler.java`, `CompiledPattern.java`, `CompiledObservation.java`, `CandidateMask.java`, `StateMaskPlane.java`, `PlaneCache.java`, `MatchCollector.java` |
| GPU acceleration | `GpuAccelerationBackend.java`, `gpu/opencl_coordinatecracker.c` |
| Exact SMT backend | `ExactBitVectorSolverBackend.java` |
| Coordinate math | `MathHelper.java` |
| Texture previews | `TextureManager.java` |
| Regression tests | `CoordinateCrackerRegressionTest.java` |

### 1. Application architecture

The application is a plain Java/Swing desktop app. It builds with `javac` and `jar`; there is no Maven or Gradle requirement.

At runtime, `Main` owns the application flow and high-level GUI state:

- editor grid;
- selected block type and rotation/variant;
- Y/radius/thread/match settings;
- acceleration mode;
- result file path;
- result viewer table;
- texture source state.

Rendering-specific Swing helpers are isolated from the frame controller: `TileIconRenderer` builds texture-backed tile icons, `SquareGridPanel` keeps the editor grid square, and `BlockTypeListCellRenderer` formats block-profile choices.

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

`ObservationCompiler` compiles the selected pattern before scanning. Each known observation becomes a `CompiledObservation`, and each selected view direction becomes a `CompiledPattern` containing:

- `dx`, `dy`, `dz` offset from the reference block;
- wanted GUI state;
- model variant count;
- visible-state mapping;
- Minecraft version.

Observations are sorted by selectivity so stronger constraints are tested earlier.

`BruteforceThread` owns worker scheduling and cancellation checks. The scalar matcher checks each candidate reference coordinate by applying every known observation. A candidate fails immediately on the first mismatch.

For compatible direct four-state, modulo-two, and one-state cases, the optimized CPU path can use a plane-cache sieve:

1. Build raw-state bitplanes for X/Z slices at a lookup Y with `StateMaskPlane`.
2. Cache those planes per worker chunk with `PlaneCache`.
3. Shift/intersect 64-bit row masks into a `CandidateMask` for each observation.
4. Emit only candidate bits that survive every constraint.

`MatchCollector` buffers region-local matches and serializes them in stable distance order before `CoordinateBruteforcer` writes the ordered region output.

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
