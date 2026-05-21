# Block profiles and textures

Reference for supported block profiles, visible-state terms, and texture source behavior.

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

The dropdown now shows only profiles with more than one useful GUI state. Names use plain black list text instead of state-count coloring so the dropdown stays readable across platforms and themes.

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
