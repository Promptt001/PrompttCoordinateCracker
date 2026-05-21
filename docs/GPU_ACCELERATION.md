# GPU acceleration

OpenCL helper setup, support matrix, and troubleshooting. See also `GPU_ACCELERATION_MODE.md` for the existing focused GPU mode notes.

## Optional GPU acceleration

GPU mode uses an external OpenCL helper instead of bundling a native Java binding into the main jar. The Java app sends scan rectangles and compiled observations to the helper over stdin; the helper returns matches over stdout.

Helper source:

```text
gpu/opencl_coordinatecracker.c
```

### Linux GPU setup

Install JDK/build tools:

```bash
sudo apt update
sudo apt install default-jdk build-essential
```

Install OpenCL headers and ICD loader. On Debian/Ubuntu-style systems:

```bash
sudo apt install opencl-headers ocl-icd-opencl-dev
```

Install or update your GPU vendor driver/runtime so OpenCL devices are visible.

Build the Java app and helper:

```bash
chmod +x build.sh test.sh gpu/build-opencl-helper.sh
./test.sh
./build.sh
./gpu/build-opencl-helper.sh
```

Run with the helper path:

```bash
java -Dcoordinatecracker.gpuCommand="$PWD/gpu/coordinatecracker-opencl-helper" \
  -jar Promptts_Coordinate_Cracker.jar
```

In the GUI, start with **GPU auto**.

### Windows GPU setup

Windows GPU mode is optional. The normal Java scanner works without it, so get the app running on CPU first and then add the GPU helper. GPU mode is a three-part setup:

1. **Java/JDK** for the main app and `build.bat`.
2. **MSVC** for compiling the native OpenCL helper executable.
3. **OpenCL runtime + development files** so Windows can both *run* OpenCL on your GPU and *build* against `CL/cl.h` / `OpenCL.lib`.

The most common Windows failure is mixing those requirements up. A GPU driver may provide the OpenCL runtime but not the headers and import library needed for compilation. Conversely, an SDK may provide `CL/cl.h` and `OpenCL.lib` but still cannot make a GPU appear if the vendor driver is missing or broken.

#### Recommended Windows setup path

For most users, use this route:

| Step | Install | Why |
| --- | --- | --- |
| 1 | A current JDK, such as [Eclipse Temurin](https://adoptium.net/temurin/releases/) | Provides `java`, `javac`, and `jar`. |
| 2 | [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) or [Visual Studio Community](https://visualstudio.microsoft.com/downloads/) | Provides Microsoft `cl.exe` and `link.exe`. |
| 3 | Your GPU vendor's current Windows driver | Provides the OpenCL runtime/ICD used at scan time. |
| 4 | OpenCL development files | Provides `CL/cl.h` and `OpenCL.lib` used at build time. |

Good OpenCL development-file choices are:

| Hardware / preference | Recommended dev-file source | Notes |
| --- | --- | --- |
| NVIDIA GPU | [CUDA Toolkit for Windows](https://docs.nvidia.com/cuda/cuda-installation-guide-microsoft-windows/) | Usually the simplest way to get `include\CL\cl.h` and `lib\x64\OpenCL.lib` on NVIDIA systems. |
| Vendor-neutral setup | [Khronos OpenCL SDK](https://github.com/KhronosGroup/OpenCL-SDK) | Brings together the Khronos headers, loader, samples, and docs. |
| Minimal/manual setup | [OpenCL-Headers](https://github.com/KhronosGroup/OpenCL-Headers) + [OpenCL-ICD-Loader](https://github.com/KhronosGroup/OpenCL-ICD-Loader) | Useful if you know exactly where you want headers and libraries installed. The ICD loader is not a GPU driver by itself. |

Khronos also publishes a [Windows OpenCL getting-started guide](https://github.com/KhronosGroup/OpenCL-Guide/blob/main/chapters/getting_started_windows.md) that is useful when you want to understand the SDK/header/loader split.

#### 1. Install and verify Java

Install a **JDK**, not only a JRE. The project build needs `javac` and `jar`.

Open **Command Prompt** or **PowerShell** and run:

```bat
java -version
javac -version
jar --help
```

Expected result:

- `java -version` prints the installed Java runtime.
- `javac -version` prints the compiler version.
- `jar --help` prints the jar tool help.

If `java` works but `javac` does not, Windows is probably finding a JRE or Microsoft Store Java alias instead of the JDK. Reinstall the JDK with the `PATH` option enabled, or add the JDK `bin` directory manually, for example:

```text
C:\Program Files\Eclipse Adoptium\jdk-XX.X.X.X-hotspot\bin
```

Then open a new terminal and verify again.

#### 2. Install MSVC build tools

The helper is a small native C program:

```text
gpu\opencl_coordinatecracker.c
```

The Windows build script compiles it with Microsoft `cl.exe`, so install either:

- [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/), or
- [Visual Studio Community](https://visualstudio.microsoft.com/downloads/).

In the Visual Studio Installer, select:

- **Desktop development with C++**.
- **MSVC v143 VS 2022 C++ x64/x86 build tools** or the current equivalent.
- **Windows 10 SDK** or **Windows 11 SDK**.

After installation, open this from the Start menu:

```text
x64 Native Tools Command Prompt for VS 2022
```

or:

```text
Developer PowerShell for VS 2022
```

Verify MSVC:

```bat
where cl
cl
where link
```

A normal `cmd.exe` or PowerShell window usually will **not** know where `cl.exe`, Windows SDK headers, or MSVC libraries are. The Visual Studio developer shell sets `PATH`, `INCLUDE`, and `LIB` for you. Microsoft documents this requirement in its MSVC command-line tooling docs.

#### 3. Install or update the GPU OpenCL runtime

Install the newest stable graphics driver for your actual GPU or integrated graphics. This is what makes an OpenCL GPU device visible at runtime.

| Hardware | Driver/runtime resource | Notes |
| --- | --- | --- |
| NVIDIA GeForce / RTX / Quadro | [NVIDIA driver downloads](https://www.nvidia.com/en-us/drivers/) and [NVIDIA OpenCL overview](https://developer.nvidia.com/opencl) | NVIDIA provides OpenCL through its display driver. NVIDIA documents OpenCL 3.0 support on R465 and later Windows/Linux drivers. |
| AMD Radeon / Ryzen graphics | [AMD Drivers and Support](https://www.amd.com/en/support/download/drivers.html) and [AMD Auto-Detect and Install Tool](https://www.amd.com/en/resources/support-articles/faqs/GPU-Driver-Autodetect.html) | Use the official AMD driver package for Windows 10/11. Laptop users may need the OEM driver if AMD's installer refuses the device. |
| Intel integrated / Arc graphics | [Intel OpenCL runtimes and driver notes](https://www.intel.com/content/www/us/en/developer/articles/tool/opencl-drivers.html) | Intel states that the Intel Graphics Compute Runtime for OpenCL is included with the Intel Graphics Driver package on Windows. |

This project's helper intentionally asks OpenCL for `CL_DEVICE_TYPE_GPU`. A CPU-only OpenCL runtime is not enough for acceleration here. It can be useful for diagnosing OpenCL in general, but `--probe` will still fail if no GPU OpenCL device is exposed.

#### 4. Install OpenCL headers and import library

The helper source includes:

```c
#include <CL/cl.h>
```

The Windows build links:

```bat
OpenCL.lib
```

So the compiler needs to find both:

```text
include\CL\cl.h
lib\x64\OpenCL.lib
```

Common install locations:

```text
C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\vXX.X\include\CL\cl.h
C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\vXX.X\lib\x64\OpenCL.lib
```

or, for a manually installed SDK:

```text
C:\OpenCL-SDK\include\CL\cl.h
C:\OpenCL-SDK\lib\OpenCL.lib
```

The exact SDK path depends on how you install it. The file names matter more than the directory names.

#### 5. Build the app and helper

From the repository root, inside **x64 Native Tools Command Prompt for VS 2022**:

```bat
build.bat
gpu\build-opencl-helper.bat
```

The helper build script runs the equivalent of:

```bat
cd gpu
cl /O2 /W3 opencl_coordinatecracker.c /Fe:coordinatecracker-opencl-helper.exe OpenCL.lib
```

Expected success output:

```text
Built gpu\coordinatecracker-opencl-helper.exe
```

After this, you should have:

```text
Promptts_Coordinate_Cracker.jar
gpu\coordinatecracker-opencl-helper.exe
```

#### 6. If `CL/cl.h` or `OpenCL.lib` is not found

If the SDK is installed but MSVC cannot find it, add the SDK paths to the current developer-shell session.

Example for CUDA Toolkit:

```bat
set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\vXX.X"
set "INCLUDE=%CUDA_PATH%\include;%INCLUDE%"
set "LIB=%CUDA_PATH%\lib\x64;%LIB%"
gpu\build-opencl-helper.bat
```

Replace `vXX.X` with the installed CUDA Toolkit version, such as `v12.5`.

Example for a custom SDK directory:

```bat
set "OPENCL_SDK=C:\OpenCL-SDK"
set "INCLUDE=%OPENCL_SDK%\include;%INCLUDE%"
set "LIB=%OPENCL_SDK%\lib;%LIB%"
gpu\build-opencl-helper.bat
```

If your SDK stores the library under `lib\x64`, use that instead:

```bat
set "LIB=%OPENCL_SDK%\lib\x64;%LIB%"
```

PowerShell equivalent:

```powershell
$env:OPENCL_SDK = "C:\OpenCL-SDK"
$env:INCLUDE = "$env:OPENCL_SDK\include;$env:INCLUDE"
$env:LIB = "$env:OPENCL_SDK\lib\x64;$env:LIB"
.\gpu\build-opencl-helper.bat
```

#### 7. Probe the helper before opening the GUI

Run the built helper directly:

```bat
gpu\coordinatecracker-opencl-helper.exe --probe
```

A working setup prints a GPU name, for example:

```text
OpenCL GPU device: NVIDIA GeForce RTX ...
```

or:

```text
OpenCL GPU device: AMD Radeon ...
```

or:

```text
OpenCL GPU device: Intel(R) Arc(TM) ...
```

If the helper prints:

```text
ERROR No OpenCL GPU device found.
```

then the helper executable launched correctly, but OpenCL did not expose a GPU device. Fix the GPU driver/runtime before changing Java settings. Useful checks:

```bat
where coordinatecracker-opencl-helper.exe
where clinfo
clinfo
```

`clinfo` is optional, but it is a good independent OpenCL diagnostic tool. On Windows, install it from a trusted package manager or from the [`clinfo` project](https://github.com/Oblomov/clinfo). Look for a GPU device under an NVIDIA, AMD, or Intel platform.

#### 8. Launch the app with the helper enabled

The Java app does not load OpenCL directly. You point it at the helper executable with this system property:

```text
-Dcoordinatecracker.gpuCommand=...
```

Command Prompt:

```bat
java "-Dcoordinatecracker.gpuCommand=%CD%\gpu\coordinatecracker-opencl-helper.exe" -jar Promptts_Coordinate_Cracker.jar
```

PowerShell:

```powershell
$helper = Join-Path (Get-Location) "gpu\coordinatecracker-opencl-helper.exe"
java "-Dcoordinatecracker.gpuCommand=$helper" -jar Promptts_Coordinate_Cracker.jar
```

If your project path contains spaces, keep the quotes exactly as shown.

In the GUI:

1. Load or enter a known-good pattern.
2. Select **GPU auto** first.
3. Run a small radius scan.
4. Switch to **GPU required** only when testing the GPU path. Required mode is useful because it reports GPU setup errors instead of silently falling back to CPU.

#### 9. Runtime troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| `cl is not recognized` | You are not in a Visual Studio developer shell. | Open **x64 Native Tools Command Prompt for VS 2022**. |
| `fatal error C1083: Cannot open include file: 'CL/cl.h'` | MSVC cannot see OpenCL headers. | Install an OpenCL SDK or add its `include` directory to `INCLUDE`. |
| `LINK : fatal error LNK1181: cannot open input file 'OpenCL.lib'` | MSVC cannot see the OpenCL import library. | Add the SDK `lib` or `lib\x64` directory to `LIB`. |
| `ERROR No OpenCL GPU device found.` | OpenCL is installed, but no GPU device is exposed. | Update/reinstall the vendor graphics driver; check Device Manager; verify with `clinfo`. |
| GUI falls back to CPU in **GPU auto** | The pattern or version is unsupported by the GPU backend, or the helper failed. | Try one small scan in **GPU required** to see the explicit error. |
| Helper works from terminal but not from Java | Bad path or missing quotes around `coordinatecracker.gpuCommand`. | Use an absolute quoted path to the `.exe`. |
| Laptop has both iGPU and dGPU but probe shows the wrong one | OpenCL returns the first GPU device found by the helper. | Update both drivers; use vendor control panels to prefer the discrete GPU for Java/terminal if needed. |
| Antivirus quarantines the helper | The helper is a locally compiled native executable. | Restore/allowlist only if you built it yourself from this repository. |

#### 10. Windows GPU limitations to keep in mind

- GPU mode currently targets optimized Minecraft `1.21.11` direct four-state and modulo-two side-profile paths.
- Unsupported versions, predictors, or observation shapes may intentionally fall back to CPU in **GPU auto**.
- The helper chooses a GPU OpenCL device, not a CPU OpenCL device.
- WSL is a different Linux userspace. Prefer the native Windows helper when running the native Windows GUI.
- Keep CPU mode available for correctness checks. GPU acceleration is a speed path, not a replacement for validating the pattern.

### GPU support matrix

Current GPU support intentionally targets the most useful hot path first:

| Feature | GPU support |
| --- | --- |
| Minecraft `1.21.11` | Supported for direct four-state and modulo-two hot paths. |
| Direct `0..3` variants | Supported. |
| Modulo-two visible mappings | Supported for deepslate/infested deepslate/stone/infested stone/sculk/bedrock-style side states. |
| `1.12.2` / `1.16.5` predictors | CPU fallback in auto mode. |
| One-state side profiles or non-four-state model counts | CPU fallback in auto mode; one-state observations do not add coordinate signal. |

`GPU auto` falls back to CPU when the helper is missing, unsupported for the selected pattern, times out, overflows its per-rectangle match buffer, or fails.

`GPU required` stops and reports the problem instead.

### GPU troubleshooting

| Symptom | Check |
| --- | --- |
| `CL/cl.h: No such file or directory` | Install OpenCL headers/development packages. |
| Linker error for `OpenCL` / `OpenCL.lib` | Install the ICD loader or SDK development package. |
| Helper builds but finds no device | Install/update GPU driver or CPU OpenCL runtime. |
| GUI falls back to CPU | Confirm version is `1.21.11` and observations fit the GPU support matrix. |
| Too many matches in one rectangle | Add observations, reduce bounds, or increase `coordinatecracker.gpuMaxMatches`. |
