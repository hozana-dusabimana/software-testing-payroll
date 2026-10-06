@echo off
rem Compiles all Java sources and starts the web demo, then opens it in the browser.
cd /d "%~dp0"

if not exist out mkdir out
javac -d out src\main\*.java src\test\*.java src\web\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Fix the errors above and run again.
    pause
    exit /b 1
)

echo Compiled OK. Starting the server at http://localhost:8080/  (close this window or press Ctrl+C to stop)
start "" /b powershell -NoProfile -Command "Start-Sleep -Seconds 2; Start-Process 'http://localhost:8080/'"
java -cp out WebServer 8080
pause
