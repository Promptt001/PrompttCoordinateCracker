# Optional external texture bundle placeholder

The default app no longer needs PNG texture files. Exact vanilla preview pixels are embedded as Java ARGB source data in `EmbeddedVanillaTextures.java`.

This directory may still be used for a project-owned, separately licensed, or private external texture bundle when deliberately building or testing with PNG inputs. Expected filenames are the values in `EnumBlockType#getTextureFileName()`, for example:

```text
stone.png
deepslate.png
deepslate_top.png
grass_block_side.png
grass_block_top.png
```

Loaded PNGs are rendered without labels, tinting, borders, direction dots, or footer overlays, using nearest-neighbor scaling.

Do not commit copied Minecraft vanilla texture PNGs here; the vanilla preview data is already represented in Java source form.
