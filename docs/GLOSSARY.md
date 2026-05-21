# Glossary

Plain-language definitions for terms used across the application and documentation.

## Coordinate and pattern terms

| Term | Meaning |
| --- | --- |
| Reference block | The block represented by the center tile on layer 4. Every result coordinate points to this block. |
| Observation | One known cell in the 7×7×7 editor: block profile plus visible state. Unknown cells are ignored. |
| Pattern | The full set of known and unknown cells entered in the editor or loaded from a pattern file. |
| Layer 4 | The visible reference plane in the editor. Other layers describe depth or height offsets relative to the selected surface mode. |
| Offset | A block's position relative to the reference block, not its absolute world coordinate. |
| Radius | The X/Z search distance from world origin. Radius `100` scans X and Z from `-100` through `100`. |
| Y range | The vertical scan interval. The app treats the maximum as exclusive: `-64` to `320` scans `-64..319`. |
| Match | A candidate reference coordinate where all known observations agree with the selected Minecraft rendering model. |

## Minecraft rendering terms

| Term | Meaning |
| --- | --- |
| Coordinate-derived state | A model or texture choice selected from block coordinates rather than true randomness. |
| Raw pool | The number of model entries Minecraft can choose before the tool maps them into user-visible states. |
| Visible state | The value a user enters in the GUI after interpreting the block face in the screenshot. |
| Direct mapping | Raw state `0..3` maps directly to visible state `0..3`. |
| Modulo-two mapping | Raw states collapse into two visible classes: `0/2 -> 0` and `1/3 -> 1`. |
| One-state profile | A block face where every raw state looks equivalent for this workflow. These profiles do not reduce the search space. |
| Surface mode | Whether the screenshot is treated as a wall/side, floor/top, or ceiling/bottom observation. |
| Facing | The compass orientation assigned to the visible surface. Use a locked facing only when it is known. |

## Backend terms

| Term | Meaning |
| --- | --- |
| CPU scanner | The dependency-free Java scanner used for normal searches. |
| Bitmask sieve | The optimized CPU path that applies observations as packed 64-bit row masks before checking candidates individually. |
| OpenCL helper | Optional native helper process used by GPU mode. The Java jar does not link OpenCL directly. |
| GPU auto | Tries the OpenCL helper and falls back to CPU when unsupported or unavailable. |
| GPU required | Stops the scan if the OpenCL helper cannot run the selected pattern. |
| SMT solver | Optional exact bit-vector solver backend, usually configured with a command such as `z3 -in`. |
| Fallback | Returning from an optional backend to the normal scanner so the scan remains complete. |
| Guardrail | A limit that prevents weak patterns from producing unbounded memory use or huge result files. |

## Result-file terms

| Term | Meaning |
| --- | --- |
| `x y z` | The candidate coordinate of the reference block. |
| `facing wall north` | The view orientation that matched the entered pattern. |
| Ordered regions | Origin-outward scan regions buffered so result files stay stable even with multiple worker threads. |
| Duplicate coordinate | The same coordinate can appear with different facing labels if the direction was not locked. |
