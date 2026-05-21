# Optional GPU acceleration mode

This branch adds a real, optional GPU execution path while keeping the normal Java jar dependency-free.

## User-facing modes

The search settings panel now includes an **Acceleration** dropdown:

- **CPU only**: default; uses the existing Java CPU scan path.
- **GPU auto**: tries the OpenCL helper first, then falls back to CPU when the helper is missing, unsupported, overflows its per-rectangle match buffer, or fails.
- **GPU required**: requires the OpenCL helper. If GPU support is unavailable or the selected pattern is unsupported, the scan stops with an error instead of silently using CPU.

## Why the GPU helper is external

The project currently builds with plain `javac` and `jar`, without Maven/Gradle or native library packaging. Hard-linking a CUDA/OpenCL Java binding into the main application would make the default build fragile.

Instead, GPU mode uses a small external helper process. The Java application sends a scan rectangle, compiled observations, and each compiled pattern's surface/facing id to the helper over stdin; the helper scans on OpenCL and returns matching coordinates with facing metadata over stdout.

The reference helper lives at:

```text
gpu/opencl_coordinatecracker.c
```

## Building the helper

Linux:

```bash
sudo apt install ocl-icd-opencl-dev   # package name varies by distro
./gpu/build-opencl-helper.sh
```

macOS:

```bash
./gpu/build-opencl-helper.sh
```

Windows Developer Command Prompt:

```bat
gpu\build-opencl-helper.bat
```

Then start the Java app with the helper path:

```bash
java -Dcoordinatecracker.gpuCommand=/absolute/path/to/gpu/coordinatecracker-opencl-helper -jar Promptts_Coordinate_Cracker.jar
```

On Windows:

```bat
java -Dcoordinatecracker.gpuCommand=C:\path\to\gpu\coordinatecracker-opencl-helper.exe -jar Promptts_Coordinate_Cracker.jar
```

If the helper is on `PATH`, the property may be omitted. The default helper names are `coordinatecracker-opencl-helper` and `coordinatecracker-opencl-helper.exe`.

## Current GPU support matrix

The first GPU implementation intentionally targets the most useful hot path first:

- Minecraft version: **1.21.11**
- Variant count: **4-state block variants**
- Supported visible mappings:
  - direct 0..3 variants
  - modulo-two visible variants for blocks such as stone/deepslate/sculk
- Unsupported in GPU mode:
  - 1.12.2 and 1.16.5 predictors
  - non-four-state model variant counts

Wall, floor, and ceiling scan directions are all preserved in GPU results for supported four-state observations.

`GPU auto` falls back to CPU for unsupported cases. `GPU required` stops and reports the unsupported condition.

## Tuning properties

```bash
-Dcoordinatecracker.gpuCommand=/path/to/helper
-Dcoordinatecracker.gpuMaxMatches=1048576
-Dcoordinatecracker.gpuTimeoutSeconds=600
-Dcoordinatecracker.gpuProbeTimeoutSeconds=10
-Dcoordinatecracker.scanBandSize=64
-Dcoordinatecracker.maxBufferedMatchesPerRegion=250000
-Dcoordinatecracker.maxPendingRegionMatchChars=67108864
```

`gpuMaxMatches` caps the number of matches returned from one rectangle. Java also passes the remaining UI/global match budget into each request, so the helper does not allocate or transfer far more matches than the scan can still accept. If a rectangle exceeds its effective cap, `GPU auto` reruns that rectangle on CPU to preserve complete output; `GPU required` stops with an error.

`scanBandSize` controls origin-outward result ordering. Use `1` for exact Chebyshev-ring scheduling. The default `64` is a lower-overhead compromise; unlimited-match CPU scans default to smaller bands to reduce per-region buffering. The `maxBufferedMatchesPerRegion` and `maxPendingRegionMatchChars` guardrails stop unlimited or underconstrained scans before result buffering exhausts memory.

## Correctness notes

The helper mirrors Java's 1.21.11 four-variant calculation, including the Java-specific 32-bit overflow before the `(long)` cast in:

```java
long i = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
```

It also preserves output semantics: if multiple pattern rotations/surfaces match the same coordinate, the helper returns one match per matching compiled pattern, including the matching surface/facing id, just like the CPU path.

The helper protocol is `PCCGPU3`. `MATCH` lines have this wire format:

```text
MATCH x y z facing_id
```

where `facing_id` is:

- `0..3`: wall north/east/south/west
- `4..7`: floor north/east/south/west
- `8..11`: ceiling north/east/south/west

Java converts that id into the result-file text form such as `facing wall north`, `facing floor east`, or `facing ceiling west`.

## Implementation files

- `src/io/github/promptt001/coordinatecracker/data/EnumAccelerationMode.java`
- `src/io/github/promptt001/coordinatecracker/cracker/GpuAccelerationBackend.java`
- `src/io/github/promptt001/coordinatecracker/cracker/ObservationCompiler.java`
- `src/io/github/promptt001/coordinatecracker/cracker/ScanLaunchRequest.java`
- `src/io/github/promptt001/coordinatecracker/cracker/BruteforceThread.java`
- `src/io/github/promptt001/coordinatecracker/cracker/CoordinateBruteforcer.java`
- `src/io/github/promptt001/coordinatecracker/Main.java`
- `gpu/opencl_coordinatecracker.c`
- `gpu/build-opencl-helper.sh`
- `gpu/build-opencl-helper.bat`
