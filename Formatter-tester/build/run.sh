#!/bin/bash
#autor: ofernandez 2011-08-29

#Indicamos la ruta relativa de los jars a cargar, separados por un espacio
DIR_JARS="lib/*.jar"

#Indicamos el nombre del main-class
MAIN_CLASS=com.novatronic.formatter.gui.Tester

#Iniciamos las variables para concatenar el Classpath
CP_TEMP=""
i=0

#Cargamos los jars de la carpeta indicadas en DIRS_JARS
for nombre in $DIR_JARS
do
  if [ ${i} -eq 0 ]
  then
    CP_TEMP=${nombre}
  else
    CP_TEMP=${CP_TEMP}:${nombre}
  fi
  i=$((${i} + 1))
done

#Mensaje en color azul, en fondo negro
echo -e "\033[40m\033[34mIniciando Monitor ETL\033[0m"

#Invocamos a la JVM para ejecutar la aplicacion
javaw -cp  $CP_TEMP $MAIN_CLASS
