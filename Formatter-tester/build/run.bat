@echo off

REM autor: ofernandez 2011-08-29

setLocal EnableDelayedExpansion

REM Indicamos el nombre del main-class
set MAIN-CLASS=com.novatronic.formatter.gui.Tester

REM Carpeta de configuracion
set DIR_CONFIG=config

REM Directorios que contiened el Classpath
set DIRS_JARS=lib lib.fmt

REM Ubicacion de la imagen splash
set SPLASH_PATH=config/splash.png

REM Iniciamos las variables para concatenar el Classpath
set CLSPATH="

set CDIR=%DIRS_JARS%
FOR %%D IN (%DIRS_JARS%) DO (
	FOR %%F IN (%%D\*.jar) DO (
 		SET CLSPATH=!CLSPATH!;%%F
	)
)

set CLSPATH=!CLSPATH!;%DIR_CONFIG%"

REM Invocamos a la JVM para ejecutar la aplicacion
START /B javaw -cp !CLSPATH! -splash:%SPLASH_PATH%  %MAIN-CLASS%