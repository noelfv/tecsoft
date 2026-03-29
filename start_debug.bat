@echo off
:: Re-lanzar en una nueva terminal si no fue iniciado con el parametro de control
if not "%1"=="new_window" (
    start cmd /k "%~f0" new_window
    exit /b
)

echo ==============================================
echo  Iniciando pgwp-orchestrator en modo DEBUG
echo ==============================================

cd /d %~dp0

echo Cargando variables de entorno de .env...
if exist .env (
    for /F "usebackq tokens=1* delims==" %%A in (".env") do (
        set "%%A=%%B"
    )
) else (
    echo [ERROR] No se encontro el archivo .env
    pause
    exit /b 1
)

echo Variables cargadas.
echo Limpiando puerto 8083 si esta en uso...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8083 ^| findstr LISTENING') do (
    echo Matando proceso con PID %%a en el puerto 8083...
    taskkill /F /PID %%a
)

echo Compilando y ejecutando microservicio en modo Debug...
echo El puerto de depuracion es 5005

mvn spring-boot:run -Dspring-boot.run.profiles=gw -DskipTests -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

pause
