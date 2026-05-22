# Getting started

Use this guide to install the prerequisites, build the jar, run tests, and launch the application. For the conceptual research model and end-to-end project breakdown, read [Project walkthrough and methodology](PROJECT_WALKTHROUGH.md).

## Requirements

Core application:

- Java JDK with `javac` and `jar` on your `PATH`.
- A desktop environment capable of running Swing.
- Bash for `test.sh` on Linux/macOS or Git Bash/WSL on Windows.

Optional GPU mode:

- C compiler.
- OpenCL headers.
- OpenCL ICD loader/import library.
- A GPU or CPU OpenCL runtime provided by your hardware vendor or OpenCL runtime package.

Optional SMT mode:

- A QF_BV-capable SMT solver command, such as `z3 -in`.
- Dense enough 1.21.11 observations to make solver mode worthwhile.

## Quick start

```bash
chmod +x test.sh build.sh
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

For a first scan:

1. Load `examples/TestPattern` or the denser `examples/20058x_82y_86z` example.
2. Leave **MC version** on `1.21.11`.
3. Leave **Acceleration** on `CPU only`.
4. Use a small radius and a tight Y range.
5. Click **Start scan**.

The screenshot and GUI reference images in `examples/` are intended as visual aids for understanding the mapping from a Minecraft image to a 7×7 pattern. They are not required to run the application.

## First-run validation checklist

Before widening a real research scan, confirm these basics:

- `./test.sh` reports that all regression tests passed.
- `./build.sh` or `build.bat` creates `Promptts_Coordinate_Cracker.jar`.
- The GUI opens from `java -jar Promptts_Coordinate_Cracker.jar`.
- A small CPU-only scan starts and stops cleanly.
- Pattern files can be loaded from `examples/` and saved to a new path.

This checklist separates environment problems from pattern-classification or search-bound problems.

## Build, test, and run

### Linux and macOS

```bash
chmod +x build.sh test.sh
./test.sh
./build.sh
java -jar Promptts_Coordinate_Cracker.jar
```

`test.sh` compiles the source and regression tests, then runs:

```text
io.github.promptt001.coordinatecracker.cracker.CoordinateCrackerRegressionTest
```

`build.sh` compiles the Java sources, copies non-Java resources, and creates:

```text
Promptts_Coordinate_Cracker.jar
```

### Windows

From Command Prompt or PowerShell with a JDK on `PATH`:

```bat
build.bat
java -jar Promptts_Coordinate_Cracker.jar
```

Regression tests are currently provided through the Bash script. Use Git Bash or WSL to run:

```bash
./test.sh
```

### Run the included jar

When the jar is already present:

```bash
java -jar Promptts_Coordinate_Cracker.jar
```
