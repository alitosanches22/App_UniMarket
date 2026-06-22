@echo off
cd /d "%~dp0"
echo Iniciando backend UniMarket...
"C:\Users\Aleja\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe" src/server.js
pause
