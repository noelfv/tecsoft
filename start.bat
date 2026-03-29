@echo off
echo ================================
echo  Iniciando pgwp-orchestrator
echo ================================

cd /d %~dp0

echo Cargando variables de entorno...
for /F "tokens=1,2 delims==" %%A in (.env) do set %%A=%%B

echo Variables cargadas.
echo Limpiando puerto 8083 si esta en uso...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8083 ^| findstr LISTENING') do (
    echo Matando proceso con PID %%a en el puerto 8083...
    taskkill /F /PID %%a
)

echo Compilando y ejecutando microservicio...

mvn spring-boot:run -Dspring-boot.run.profiles=gw -DskipTests

pause
