# Tuning properties

Runtime system properties for performance tuning, guardrails, and diagnostics.

## Tuning properties

| Property | Default | Purpose |
| --- | ---: | --- |
| `coordinatecracker.assets` | unset | Explicit texture source path. |
| `coordinatecracker.maxMatches` | `1000000` | Global scan match cap. `0` disables it. |
| `coordinatecracker.scanBandSize` | `64` | Origin-outward scan band size for CPU/global scheduling. |
| `coordinatecracker.gpuCommand` | helper name on `PATH` | OpenCL helper command/path. |
| `coordinatecracker.gpuMaxMatches` | helper-defined/default backend value | Per-rectangle GPU match cap. |
| `coordinatecracker.gpuTimeoutSeconds` | `600` | GPU scan timeout. |
| `coordinatecracker.gpuProbeTimeoutSeconds` | `10` | GPU helper probe timeout. |
| `coordinatecracker.gpuScanBandSize` | `2048` | Larger default band size when GPU mode is available. |
| `coordinatecracker.sievePlaneBytes` | `4194304` | Target memory size used to choose CPU sieve chunk depth. |
| `coordinatecracker.smtSolverCommand` | unset | Enables the exact bit-vector solver backend. |
| `coordinatecracker.smtTimeoutSeconds` | `30` | Timeout per SMT query. |
| `coordinatecracker.smtMaxMatches` | `10000` | Solver model cap per rectangle. |
| `coordinatecracker.smtMinObservations` | `10` | Minimum observation count before trying SMT mode. |

Example:

```bash
java \
  -Dcoordinatecracker.maxMatches=50000 \
  -Dcoordinatecracker.scanBandSize=128 \
  -jar Promptts_Coordinate_Cracker.jar
```

## Practical tuning sequence

1. Start with a narrow Y range, a small radius, and `CPU only`.
2. Add or correct observations until the expected scan produces a manageable number of matches.
3. Increase radius only after the small scan behaves as expected.
4. Raise `Max matches` only when you intentionally want a broad exploratory result set.
5. Try GPU or SMT mode only after the CPU configuration is known to be valid.

When a scan is unexpectedly slow, the best fix is usually better constraints, not more threads. Extra threads help most when the pattern is already selective enough to keep result buffering small.

## Guardrail guidance

Use a finite `Max matches` for normal work. Unlimited mode is available for research, but it can create very large result files and region buffers when the pattern is weak.

The buffer-related properties are emergency controls for advanced users. Prefer to improve the pattern, reduce the bounds, or use a finite match cap before raising them.
