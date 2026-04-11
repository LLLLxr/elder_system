@echo off
echo Starting Auth Service...
cd /d D:\Project\elder_system\auth-service
echo Current directory: %CD%

REM Check if Maven is installed
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Maven is not installed or not in PATH
    echo Please install Maven or run the application from VS Code
    pause
    exit /b 1
)

REM Run the application
mvn spring-boot:run
pause