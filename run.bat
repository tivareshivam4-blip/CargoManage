@echo off
title CargoManage – Enterprise Management System
echo =====================================================================
echo              CARGOMANAGE ENTERPRISE MANAGEMENT SYSTEM
echo       Enterprise Warehouse, Inventory & Business Management
echo                     Manage. Track. Control.
echo =====================================================================
echo.

where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Java JDK is not installed or not in your PATH.
  echo Please install JDK 17 or newer.
  pause
  exit /b
)

where javac >nul 2>nul
if errorlevel 1 (
  echo [ERROR] javac compiler is not installed or not in your PATH.
  echo Please install JDK 17 or newer.
  pause
  exit /b
)

echo [1/2] Compiling CargoManage enterprise modules...
if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin src\CargoManage.java src\CargoGrid.java src\cargomanage\CargoManageApp.java src\cargomanage\config\*.java src\cargomanage\model\*.java src\cargomanage\dao\*.java src\cargomanage\security\*.java src\cargomanage\util\*.java src\cargomanage\ui\*.java
if errorlevel 1 (
  echo.
  echo [ERROR] Compilation failed. Please check source files and libraries.
  pause
  exit /b
)

echo [2/2] Starting CargoManage Application...
echo.
java -cp "bin;lib/*" CargoManage
pause
