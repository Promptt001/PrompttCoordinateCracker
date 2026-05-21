# Maintainer review guide

A practical checklist for keeping this project polished, stable, and understandable without turning every review into a feature sprint.

## Review goals

1. Preserve the existing coordinate-cracking workflow.
2. Make the documentation easy to follow for non-specialists.
3. Keep optional acceleration paths isolated from the dependency-free Java baseline.
4. Prefer small stability and performance changes over new UI or scanning features.
5. Record assumptions and limitations clearly when a behavior is subtle.

## Documentation standards

- Start each guide with the user's immediate goal.
- Define terms before relying on them. Link to `GLOSSARY.md` for repeated concepts.
- Mark optional features as optional every time they introduce external dependencies.
- Use exact command snippets that can be copied without editing whenever possible.
- Explain failure modes in terms of user actions: wrong surface, weak pattern, broad bounds, missing OpenCL runtime, or unsupported block profile.
- Avoid implying that a match proves a screenshot's true location without qualification.

## Stability review checklist

Run these checks before shipping documentation or code polish:

```bash
./test.sh
./build.sh
mkdir -p build/lint
find src test -name '*.java' | sort > build/lint-sources.txt
javac -Xlint:all -encoding UTF-8 -d build/lint @build/lint-sources.txt
```

For optional GPU changes, also build and probe the helper on a machine with OpenCL headers and runtime:

```bash
./gpu/build-opencl-helper.sh
./gpu/coordinatecracker-opencl-helper --probe
```

For optional SMT changes, test one dense pattern with a known solver command:

```bash
java -Dcoordinatecracker.smtSolverCommand="z3 -in" \
  -Dcoordinatecracker.smtMinObservations=12 \
  -jar Promptts_Coordinate_Cracker.jar
```

## Code-change boundaries

Acceptable polish changes:

- clearer validation and error messages;
- fixes for duplicate or misleading console output;
- safer bounds, buffer, and cancellation behavior;
- small hot-loop optimizations covered by regression tests;
- extraction or naming changes that make existing behavior easier to reason about.

Avoid during polish-only reviews:

- new block-profile support unless it is fully documented and tested;
- new network/distributed behavior;
- broad UI redesigns;
- unverified changes to Minecraft rendering math;
- replacing fallback behavior with hard dependency requirements.

## Knowledge-base expansion areas

The current documentation is strongest around setup, pattern files, GPU mode, and architecture. Future knowledge-base improvements should focus on:

| Area | Why it helps |
| --- | --- |
| Screenshot annotation examples | Helps users understand reference-block placement and offsets. |
| Block-state visual atlas | Reduces wrong manual classification for two-state and four-state profiles. |
| Known-good tiny test patterns | Gives users a quick confidence check before large searches. |
| Troubleshooting decision tree | Turns broad symptoms such as zero matches into concrete next steps. |
| Backend comparison table | Helps users decide when CPU, GPU, or SMT mode is appropriate. |

Keep these as explanatory material unless a change is needed for correctness or stability.
