# Getting started

Use this guide to install the prerequisites, build the jar, run tests, and launch the application.

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

1. Load `examples/TestPattern`.
2. Leave **MC version** on `1.21.11`.
3. Leave **Acceleration** on `CPU only`.
4. Use a small radius and a tight Y range.
5. Click **Start scan**.

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
