@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0"
set "SRC_DIR=%ROOT_DIR%src"
set "BUILD_DIR=%ROOT_DIR%build"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "SOURCES_FILE=%BUILD_DIR%\sources.txt"
set "JAR_FILE=%ROOT_DIR%Promptts_Coordinate_Cracker.jar"
set "MAIN_CLASS=io.github.promptt001.coordinatecracker.Main"

where javac >nul 2>nul
if errorlevel 1 (
    echo Error: javac was not found on PATH. Install a JDK and try again.
    exit /b 1
)

where jar >nul 2>nul
if errorlevel 1 (
    echo Error: jar was not found on PATH. Install a JDK and try again.
    exit /b 1
)

if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%JAR_FILE%" del /q "%JAR_FILE%"
mkdir "%CLASSES_DIR%" >nul 2>nul

for /r "%SRC_DIR%" %%F in (*.java) do echo %%F>>"%SOURCES_FILE%"

if not exist "%SOURCES_FILE%" (
    echo Error: no Java sources found under %SRC_DIR%.
    exit /b 1
)

echo Compiling Java sources...
javac -encoding UTF-8 -d "%CLASSES_DIR%" @"%SOURCES_FILE%"
if errorlevel 1 exit /b 1

echo Copying resources...
robocopy "%SRC_DIR%" "%CLASSES_DIR%" /E /XF *.java >nul
if %ERRORLEVEL% GEQ 8 (
    echo Error: resource copy failed.
    exit /b 1
)

echo Building %JAR_FILE%...
jar cfe "%JAR_FILE%" "%MAIN_CLASS%" -C "%CLASSES_DIR%" .
if errorlevel 1 exit /b 1

rmdir /s /q "%BUILD_DIR%"

echo Successfully built %JAR_FILE%
endlocal
