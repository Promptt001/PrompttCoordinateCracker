@echo off
setlocal
cd /d "%~dp0"
cl /O2 /W3 opencl_coordinatecracker.c /Fe:coordinatecracker-opencl-helper.exe OpenCL.lib
if errorlevel 1 exit /b 1
echo Built gpu\coordinatecracker-opencl-helper.exe
endlocal
