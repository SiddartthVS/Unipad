@echo off
REM =========================================================================
REM UNi Universal Clipboard - CLEAN BUILD (All Required Libraries)
REM Includes SLF4J libraries required by Java-WebSocket
REM =========================================================================

echo.
echo ========================================
echo  UNi - CLEAN BUILD
echo ========================================
echo.
echo Required Libraries:
echo   - Java-WebSocket-1.5.3.jar (WebSocket server)
echo   - jna-5.13.0.jar (Windows API)
echo   - jna-platform-5.13.0.jar (Windows platform)
echo   - slf4j-api-2.0.7.jar (Logging API for WebSocket)
echo   - slf4j-simple-2.0.7.jar (Logging implementation)
echo.

REM Set variables
set BUILD_DIR=build
set DIST_DIR=dist
set LIB_DIR=lib
set JAR_NAME=UNi.jar

REM Step 1: Clean everything
echo [1/8] Cleaning old build files...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%BUILD_DIR%"
mkdir "%DIST_DIR%"
mkdir "%DIST_DIR%\lib"
echo      Done!
echo.

REM Step 2: Verify Java installation
echo [2/8] Verifying Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo      ERROR: Java is NOT installed or NOT in PATH
    echo      Download from: https://adoptium.net/
    pause
    exit /b 1
)
javac -version >nul 2>&1
if errorlevel 1 (
    echo      ERROR: Java compiler javac NOT found
    pause
    exit /b 1
)
echo      Java is installed
echo.

REM Step 3: Check required libraries
echo [3/8] Checking required libraries...
set LIB_ERROR=0
if not exist "%LIB_DIR%\Java-WebSocket-1.5.3.jar" (
    echo      ERROR: Java-WebSocket-1.5.3.jar is missing
    set LIB_ERROR=1
)
if not exist "%LIB_DIR%\jna-5.13.0.jar" (
    echo      ERROR: jna-5.13.0.jar is missing
    set LIB_ERROR=1
)
if not exist "%LIB_DIR%\jna-platform-5.13.0.jar" (
    echo      ERROR: jna-platform-5.13.0.jar is missing
    set LIB_ERROR=1
)
if not exist "%LIB_DIR%\slf4j-api-2.0.7.jar" (
    echo      ERROR: slf4j-api-2.0.7.jar is missing
    echo      Run: download-slf4j.bat to download it
    set LIB_ERROR=1
)
if not exist "%LIB_DIR%\slf4j-simple-2.0.7.jar" (
    echo      ERROR: slf4j-simple-2.0.7.jar is missing
    echo      Run: download-slf4j.bat to download it
    set LIB_ERROR=1
)

if %LIB_ERROR% EQU 1 (
    echo.
    echo      Some required libraries are missing!
    echo      Run: download-slf4j.bat to get SLF4J libraries
    pause
    exit /b 1
)
echo      All required libraries found
echo.

REM Step 4: Compile Java files
echo      Compiling with classpath: %LIB_DIR%\*
javac -d "%BUILD_DIR%" -cp "%LIB_DIR%\*" src\*.java 2>build_errors.txt

if errorlevel 1 (
    echo.
    echo      ERROR: Compilation failed!
    echo      Check build_errors.txt for details
    type build_errors.txt
    pause
    exit /b 1
)
if exist build_errors.txt del build_errors.txt
echo      Compilation successful!
echo.

REM Step 5: Handle icon
echo [5/8] Handling application icon...
if exist "icon.png" (
    copy "icon.png" "%BUILD_DIR%\icon.png" >nul
    echo      Icon copied to build directory
) else (
    echo      No icon found - app will create default icon at runtime
)
echo.

REM Step 6: Copy libraries to dist
echo [6/8] Copying required libraries to dist folder...
copy "%LIB_DIR%\Java-WebSocket-1.5.3.jar" "%DIST_DIR%\lib\" >nul
copy "%LIB_DIR%\jna-5.13.0.jar" "%DIST_DIR%\lib\" >nul
copy "%LIB_DIR%\jna-platform-5.13.0.jar" "%DIST_DIR%\lib\" >nul
copy "%LIB_DIR%\slf4j-api-2.0.7.jar" "%DIST_DIR%\lib\" >nul
copy "%LIB_DIR%\slf4j-simple-2.0.7.jar" "%DIST_DIR%\lib\" >nul
echo      5 libraries copied
echo.

REM Step 7: Create JAR file
echo [7/8] Creating JAR file...
cd "%BUILD_DIR%"
if exist "icon.png" (
    jar cvfm "..\%DIST_DIR%\%JAR_NAME%" "..\MANIFEST_CLEAN.MF" *.class icon.png >nul 2>&1
) else (
    jar cvfm "..\%DIST_DIR%\%JAR_NAME%" "..\MANIFEST_CLEAN.MF" *.class >nul 2>&1
)
cd ..

if errorlevel 1 (
    echo.
    echo      ERROR: JAR creation failed!
    pause
    exit /b 1
)
echo      JAR file created successfully!
echo.

REM Step 8: Verify lib folder in dist
echo [8/8] Verifying distribution...
if exist "%DIST_DIR%\lib\Java-WebSocket-1.5.3.jar" (
    if exist "%DIST_DIR%\lib\slf4j-api-2.0.7.jar" (
        echo      All libraries properly copied to dist\lib
    )
)
echo.

REM Success message
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.
echo Created:
echo   JAR:  %DIST_DIR%\%JAR_NAME%
echo   Libs: %DIST_DIR%\lib\ (5 files)
echo.
echo Libraries included:
echo   - Java-WebSocket-1.5.3.jar
echo   - jna-5.13.0.jar
echo   - jna-platform-5.13.0.jar
echo   - slf4j-api-2.0.7.jar
echo   - slf4j-simple-2.0.7.jar
echo.

