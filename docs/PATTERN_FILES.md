# Pattern file format

Reference for the plain-text 7×7×7 pattern file format used by the Swing editor.

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

## Reading a saved pattern

A saved file is intentionally human-readable. Each visible token represents one editor cell:

| Token | Meaning |
| --- | --- |
| `?`, `.`, `unknown` | Ignore this cell during matching. |
| `deepslate:1` | Known deepslate side observation with visible state `1`. |
| `sand_top:2` | Known sand top/bottom observation with visible state `2`. |
| `0`, `1`, `2`, `3`, `4` | Legacy numeric deepslate-side format; `4` means unknown. |

Use unknown tokens whenever a block is hidden, ambiguous, affected by a resource pack, or not part of the supported profile list. A small reliable pattern is better than a larger pattern with guessed states.

## Minimal annotated example

```text
# Layer 1 of 7
? ? ? ? ? ? ?
? ? ? ? ? ? ?
? ? ? ? ? ? ?
? ? ? ? ? ? ?
? ? ? ? ? ? ?
? ? ? ? ? ? ?
? ? ? ? ? ? ?
---------------------------
# Layers 2 and 3 omitted here for brevity in this example.
# Real saved files contain all seven complete layers.

# Layer 4 is the visible reference plane in the editor.
? ? ? ? ? ? ?
? ? ? ? ? ? ?
? ? deepslate:1 stone:0 sculk:1 ? ?
? ? ? deepslate:0 ? ? ?
? ? sand_top:2 ? ? ? ?
? ? ? ? ? ? ?
? ? ? ? ? ? ?
```

The center tile of layer 4 is the reference block. Output coordinates refer to that block, not to every observed block in the pattern.

Backward compatibility:

```text
0 1 4 4 0 1 0
```

Numeric-only legacy files load as deepslate side observations:

- `0`, `1`, `2`, `3` become deepslate side values.
- `4` becomes unknown.

The loader is intentionally strict. It rejects malformed dimensions, unsupported block tokens, invalid states, and removed static block tokens so bad data does not silently become a misleading scan.
