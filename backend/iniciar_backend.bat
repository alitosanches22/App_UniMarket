@echo off
cd /d "%~dp0"

echo ========================================
echo Iniciando backend UniMarket
echo ========================================

where node >nul 2>nul
if errorlevel 1 (
    echo.
    echo ERROR: Node.js no esta instalado o no esta en el PATH.
    echo Instala Node.js LTS desde https://nodejs.org y vuelve a abrir esta ventana.
    echo.
    pause
    exit /b 1
)

where npm >nul 2>nul
if errorlevel 1 (
    echo.
    echo ERROR: npm no esta instalado o no esta en el PATH.
    echo Instala Node.js LTS desde https://nodejs.org y vuelve a abrir esta ventana.
    echo.
    pause
    exit /b 1
)

if not exist ".env" (
    echo.
    echo No existe el archivo .env.
    echo Se creara desde .env.example.
    copy ".env.example" ".env" >nul
    echo.
    echo IMPORTANTE: abre backend\.env y cambia TU_PASSWORD por la contrasena de PostgreSQL.
    echo Despues vuelve a ejecutar este archivo.
    echo.
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo.
    echo Instalando dependencias del backend...
    npm install
    if errorlevel 1 (
        echo.
        echo ERROR: No se pudieron instalar las dependencias.
        pause
        exit /b 1
    )
)

echo.
echo Backend listo en http://localhost:3000
echo Deja esta ventana abierta mientras usas la app.
echo.
node src/server.js
pause
