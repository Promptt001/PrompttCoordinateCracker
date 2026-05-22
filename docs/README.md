# Documentation hub

This directory is the long-form knowledge base for Promptt Coordinate Cracker. The root README gives a concise overview; these documents explain the research model, setup process, screenshot workflow, file formats, acceleration modes, and implementation details.

## Recommended reading paths

| Reader goal | Suggested path |
| --- | --- |
| First-time Minecraft researcher | [Project walkthrough and methodology](PROJECT_WALKTHROUGH.md) → [Getting started](GETTING_STARTED.md) → [Tutorial and workflow](TUTORIAL.md) |
| User preparing a real screenshot scan | [Tutorial and workflow](TUTORIAL.md) → [Block profiles and textures](BLOCK_PROFILES_AND_TEXTURES.md) → [FAQ](FAQ.md) |
| User debugging zero or too many matches | [FAQ](FAQ.md) → [Pattern file format](PATTERN_FILES.md) → [Tuning properties](TUNING.md) |
| Developer or maintainer | [Architecture and detailed design](ARCHITECTURE.md) → [Maintainer review guide](MAINTAINER_REVIEW.md) → [Full reference](FULL_REFERENCE.md) |
| User enabling optional acceleration | [GPU acceleration](GPU_ACCELERATION.md) or [SMT solver backend](SMT_SOLVER_BACKEND.md) |

## Conceptual overview

- [Project walkthrough and methodology](PROJECT_WALKTHROUGH.md) — plain-language but detailed explanation of the project, research assumptions, data flow, setup, scan methodology, output interpretation, and limitations.
- [Glossary](GLOSSARY.md) — definitions for common scanner, block-profile, and Minecraft-rendering terms.
- [FAQ](FAQ.md) — common setup, workflow, result-interpretation, and responsible-use questions.

## User guides

- [Getting started](GETTING_STARTED.md) — requirements, quick start, build, test, and run commands.
- [Tutorial and workflow](TUTORIAL.md) — end-to-end screenshot-to-scan procedure and practical advice.
- [Pattern file format](PATTERN_FILES.md) — save/load syntax, layer structure, tokens, and examples.
- [Block profiles and textures](BLOCK_PROFILES_AND_TEXTURES.md) — supported profiles, state terms, placement caveats, and texture source behavior.

## Advanced configuration

- [GPU acceleration](GPU_ACCELERATION.md) — OpenCL helper setup, support matrix, tuning, and troubleshooting.
- [GPU acceleration mode notes](GPU_ACCELERATION_MODE.md) — focused implementation notes for GPU mode.
- [SMT solver backend](SMT_SOLVER_BACKEND.md) — optional exact solver setup and guardrails.
- [Tuning properties](TUNING.md) — runtime flags and safety limits.

## Maintainer reference

- [Architecture and detailed design](ARCHITECTURE.md) — implementation map, data model, predictors, matcher, GPU backend, SMT backend, and known limitations.
- [Maintainer review guide](MAINTAINER_REVIEW.md) — documentation standards, stability review checklist, and future knowledge-base expansion areas.
- [Full reference](FULL_REFERENCE.md) — original README preserved verbatim so no information is lost during the documentation split.
