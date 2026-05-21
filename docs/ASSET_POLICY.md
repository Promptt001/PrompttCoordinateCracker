# Asset policy

Coordinate Cracker does not require texture files for coordinate solving. Textures are GUI previews that help a user transcribe visible block states from screenshots.

## Exact texture requirement

The GUI now ships self-contained exact vanilla preview pixels as Java source data. The source PNG files are not imported, committed, copied into the jar, or required at runtime. At startup, `TextureManager` builds `BufferedImage` previews from embedded ARGB arrays decoded from the previous vanilla texture bundle.

Those previews are drawn with nearest-neighbor scaling and without labels, borders, tinting, antialiasing, direction dots, or footer overlays. The selected state is represented by the rotated/mirrored texture itself and by the button tooltip, not by modifying the pixels.

## Exactness scope

The embedded data preserves the vanilla asset pixels used by the previous bundled build. This is the correct baseline for visual texture matching in the GUI.

A world screenshot may still differ from raw asset pixels because Minecraft rendering can add biome tinting, light level, ambient occlusion, fog, shader effects, mipmapping, perspective distortion, screenshot compression, and video/platform scaling. For strict screenshot work, compare against screenshots taken with matching game version, resource pack, lighting, biome, graphics settings, and capture pipeline.

## No source PNG dependency

Do not commit copied vanilla Minecraft texture PNGs under `src/assets/minecraft/textures/block/` or `src/assets/coordinatecracker/textures/block/`. The project stores the needed vanilla preview pixels in `EmbeddedVanillaTextures.java` instead.

Optional external PNG sources are still supported for deliberate overrides, such as testing a different version or a user-supplied resource pack. The texture loader accepts these shapes when selected explicitly:

- `assets/coordinatecracker/textures/block/*.png`
- `coordinatecracker/textures/block/*.png`
- `assets/minecraft/textures/block/*.png`
- `minecraft/textures/block/*.png`
- `textures/block/*.png`
- direct `*.png` files in the selected directory

## Startup behavior

Texture lookup order at startup is:

1. `-Dcoordinatecracker.assets=/path/to/source`;
2. `COORDINATECRACKER_ASSETS=/path/to/source`;
3. embedded vanilla ARGB texture data;
4. last GUI-selected source stored in Java preferences, only if embedded data is unavailable;
5. explicit missing-texture placeholders.

When a selected external source contains only some textures, loaded textures render exactly and missing entries show placeholders. This prevents false confidence from approximate generated lookalikes.
