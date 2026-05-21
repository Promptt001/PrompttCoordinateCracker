# SMT solver backend

Optional exact bit-vector solver backend configuration and behavior.

## Optional SMT solver backend

The exact bit-vector solver backend is experimental and opt-in. It can be useful for dense 1.21.11 patterns over very large rectangles.

Enable it with:

```bash
java -Dcoordinatecracker.smtSolverCommand="z3 -in" \
  -jar Promptts_Coordinate_Cracker.jar
```

Recommended first dense-pattern test:

```bash
java \
  -Dcoordinatecracker.smtSolverCommand="z3 -in" \
  -Dcoordinatecracker.smtMinObservations=12 \
  -jar Promptts_Coordinate_Cracker.jar
```

The solver backend:

- supports 1.21.11 observations;
- expects four-state raw pools with direct or modulo-two visible mappings;
- encodes Java overflow and 48-bit LCG behavior;
- enumerates solver models;
- verifies each model with the Java matcher;
- falls back to the scanner on `unknown`, failure, timeout, unsupported patterns, or model cap overflow.

It is not always faster. Use it when the pattern is strong and the brute-force scan area is large.

## When not to use it

Do not start with SMT mode when validating a new screenshot. It is easier to diagnose mistakes with a small CPU scan first.

SMT mode is a poor fit when:

- the pattern has only a few observations;
- the selected blocks include unsupported or one-state profiles;
- the target version is not `1.21.11`;
- you need predictable progress feedback more than solver-driven pruning.

Every solver match is still verified by the Java matcher before it is accepted, so SMT mode should be treated as an optional accelerator rather than a separate source of truth.
