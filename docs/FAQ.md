# FAQ

Common questions for first-time users and maintainers.

## What does the tool actually prove?

A match proves that the entered observations are consistent with a candidate coordinate under the selected Minecraft version and surface/facing assumptions. It does not prove the screenshot came from that coordinate by itself. Confidence improves when the pattern has many reliable observations and few matches.

## Why did my scan return no results?

The most common causes are:

- one or more observed states were guessed incorrectly;
- the reference block was not placed on the center tile of layer 4;
- the selected surface mode does not match the screenshot;
- a top/bottom profile was mixed with a wall profile, or the reverse;
- the Y range or radius excludes the real coordinate;
- the screenshot uses a resource pack, shader, compression, or lighting that makes the visual state hard to classify.

Reduce the pattern to the most certain observations, scan a small area, then add observations back gradually.

## Why did my scan return too many results?

The pattern is underconstrained. Add more reliable observations, prefer profiles with two or four readable states, tighten the Y range, or reduce the radius. Avoid using one-state profiles for side observations because they do not add coordinate signal.

## Which block should I start with?

Start with blocks whose face and state you can identify confidently. For wall-style scans, deepslate, stone, sculk, and bedrock-style side profiles are usually more useful than sand or dirt side faces. For floor or ceiling scans, top/bottom profiles can be useful, but they are often harder to read from screenshots.

## Should I use CPU, GPU, or SMT mode first?

Use `CPU only` first. It is dependency-free and easiest to debug. Try `GPU auto` after a small CPU scan confirms that the pattern and bounds are sensible. Try SMT mode only for dense 1.21.11 patterns where a brute-force scan is large enough to justify solver overhead.

## Why does layer 4 matter?

Layer 4 is the visible reference plane. The center tile on that layer is offset `(0, 0, 0)`. The app reports candidate coordinates for that tile, so placing the reference block anywhere else shifts every result.

## Are result coordinates block positions or camera positions?

They are block positions for the reference block. The tool does not infer the player's camera position, yaw, pitch, seed, or dimension.

## Why can the same coordinate appear more than once?

If facing is not locked, the same coordinate can satisfy the pattern under more than one wall/floor/ceiling orientation. Lock the facing when the screenshot orientation is known.

## Can I use resource-pack textures?

You can use them for visual classification if you understand how the pack changes the face states. The predictor is based on Minecraft's coordinate-derived model choice, not the image files. A resource pack can still make state labels visually ambiguous or change the mapping you expect.

## Is this appropriate for every server or screenshot?

No. Dense coordinate fingerprints can reveal location information. Use the tool only for screenshots, events, worlds, and servers where coordinate inference is allowed.
