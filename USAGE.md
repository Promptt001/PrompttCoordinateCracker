# Usage guide

This page is a quick reference. For guided explanations, start with `README.md`, `docs/GETTING_STARTED.md`, and `docs/TUTORIAL.md`.

## Start

```bash
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

Windows:

```bat
build.bat
java -jar Promptts_Coordinate_Cracker.jar
```

## Basic workflow

1. Pick a reference block in the screenshot.
2. Put that reference block on the center tile of pattern layer 4.
3. Enter nearby known observations as block type plus rotation/variant.
4. Leave uncertain cells unknown.
5. Set version to `1.21.11` for Pattern03-style scans.
6. Use the **Surface** dropdown to choose exactly one plane: wall/side, floor/top, or ceiling/bottom; lock facing when known, or try all facings for that selected plane.
7. Use the smallest plausible Y range and radius.
8. Start the scan and review the results file.

## Pattern controls

- Left-click: select/cycle a tile.
- Right-click: clear a tile.
- Block type dropdown: set the selected tile's block profile.
- Rotation / variant dropdown: set the selected tile's visible state.
- Toggle known/unknown: switch the selected tile between ignored and value 0.
- Clear current layer: remove observations from the visible layer.
- Clear all layers: reset the whole 7×7×7 pattern.

## Supported block tokens

```text
deepslate, deepslate_top, infested_deepslate, infested_deepslate_top,
stone, stone_top, infested_stone, infested_stone_top,
sculk, sculk_top, bedrock, bedrock_top,
dirt_top, sand_top, red_sand_top, rooted_dirt_top,
grass_block_top, grass_block_bottom, podzol_top, podzol_bottom,
mycelium_top, mycelium_bottom, dirt_path_top, dirt_path_bottom,
<color>_concrete_powder_top
```

Static/non-randomized blocks are intentionally unsupported; leave those cells unknown. One-state profiles are omitted from the block-type dropdown because they do not add useful coordinate signal. Deepslate side, stone, and sculk observations use GUI values `0` and `1`; deepslate top/bottom and the four-state terrain top/bottom profiles use values `0..3`. The UI warns in bold red and disables scanning when face-specific profiles conflict with the selected surface, such as side and top tiles mixed together.

## Pattern file tokens

```text
block_type:rotation
```

Example:

```text
deepslate:1 stone:0 sand:2 ? ? sculk:1 dirt:3
```

Unknown cells can be `?`, `.`, or `unknown`. Old numeric-only files still load as deepslate side observations. Old static tokens such as `tuff`, `gravel`, `granite`, `diorite`, `andesite`, and `blackstone` are rejected; replace them with `?`.

## Output

Matches are appended as:

```text
x y z facing direction
```

Example:

```text
12 64 -8 facing north
```

Each coordinate is the candidate position of the reference block represented by the center tile on pattern layer 4, and the facing value is the surface orientation that matched.

## Optional GPU acceleration

The **Acceleration** dropdown offers `CPU only`, `GPU auto`, and `GPU required`.

GPU mode uses the optional OpenCL helper in `gpu/opencl_coordinatecracker.c`. Build it separately:

```bash
./gpu/build-opencl-helper.sh
```

Then run the app with:

```bash
java -Dcoordinatecracker.gpuCommand=/absolute/path/to/gpu/coordinatecracker-opencl-helper -jar Promptts_Coordinate_Cracker.jar
```

`GPU auto` falls back to CPU when the helper is missing or the selected pattern is unsupported. `GPU required` stops with an error instead.

Current GPU support is intentionally limited to the 1.21.11 four-state random-variant hot path. See `docs/GPU_ACCELERATION_MODE.md` for details and tuning flags.
