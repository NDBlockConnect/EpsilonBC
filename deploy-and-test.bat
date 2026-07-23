@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
echo ====================================
echo EpsilonBC deploy and test
echo ====================================

set "MOD_DIR=C:\Users\Sails\AppData\Roaming\mdl\instances\epsilon-fabric-26\mods"
set "LIBS_DIR=C:\Users\Sails\Documents\Workspace\NormalWorkspace\Epsilon-Workspace\EpsilonBC\fabric\build\libs"

echo.
echo [1/4] Selecting newest build artifact...
set "JAR_NAME="
for /f "delims=" %%i in ('dir /b /o-d "%LIBS_DIR%\epsilon-fabric-*.jar" ^| findstr /v /i "sources"') do (
    if not defined JAR_NAME set "JAR_NAME=%%i"
)
if not defined JAR_NAME (
    echo   [X] No artifact found. Run gradlew :fabric:jar first.
    exit /b 1
)
echo     Selected: !JAR_NAME!

echo [2/4] Cleaning old mod...
del /Q "%MOD_DIR%\epsilon-fabric-*.jar" 2>nul

echo [3/4] Copying new mod...
copy /Y "%LIBS_DIR%\!JAR_NAME!" "%MOD_DIR%\"

echo [4/4] Verifying deployment...
if exist "%MOD_DIR%\!JAR_NAME!" (
    echo.
    echo   [OK] Deployed: !JAR_NAME!
    echo.
    echo   Run:  mdl --no-color launch epsilon-fabric-26 --detach
) else (
    echo.
    echo   [X] Deploy failed: jar not found.
    exit /b 1
)
endlocal
