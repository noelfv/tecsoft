#!/bin/bash

# Configurar el script para capturar logs
echo "Iniciando aplicación..."

# Se usa este condicional para aber si estamos en stage de Jenkins o en un despliegue normal
if [ "$TEST_JENKINS_DEPLOY" = "true" ]; then
  cd "docker/" || exit 1
else
  cd /opt/swp2_gateway/ || exit 1
fi

# Ejecutar la aplicación
java -Xmx384m -Xms128m -jar pgwp-orchestrator*.jar &

# Capturar el PID del proceso de Java
JAVA_PID=$!

echo "Usando puerto TCP de comunicacion PORT=$PORT"
echo "Usando GRPC_SERVER_PORT=$GRPC_SERVER_PORT"

# Verificar el estado del servicio gRPC
echo "Verificando el estado del servicio gRPC..."
SERVICE_STATUS=""



while [ "$SERVICE_STATUS" != "SERVING" ]; do
    SERVICE_STATUS=$(grpcurl -H "traceId:1234" -H "spanId:1234" -H "network:PEER02" -H "port:$PORT" -H "simulation:true" -plaintext localhost:$GRPC_SERVER_PORT grpc.health.v1.Health.Check 2>/dev/null | jq -r .status)

    if [ "$SERVICE_STATUS" == "SERVING" ]; then
        echo "El servicio gRPC está activo y en estado SERVING."
        break
    else
        echo "El servicio gRPC aún no está activo. Reintentando en 1 segundos..."
        sleep 1
    fi
done

# Forzar la carga de métodos del proto
echo "Forzando la carga de métodos del proto..."

# Crear un estado inicial para el health check
ALL_METHODS_READY=true

# Lista de métodos a probar
METHODS=(
    "b.gateway.v1.OrchestratorService.PostProcessMessageAsync"
    # "b.gateway.v1.ConnMgrService.PostDataPacket"
)

JSON_PAYLOAD='{"originalMessage": "F0F1F0F0767F664128E1B20AF1F6F5F1F2F4F0F9F8F0F0F0F4F8F6F7F1F5F0F1F2F0F0F0F0F0F0F0F0F0F0F0F8F0F0F0F0F0F0F0F0F0F0F0F8F0F0F0F0F5F1F3F2F1F0F1F0F2F6F1F0F0F0F0F0F0F0F0F1F3F5F0F1F6F0F1F0F2F0F5F1F3F2F8F0F3F0F5F1F3F0F5F1F3F6F0F1F1F6F0F4F0F5F1F0F0F1F0F4F0F6F1F2F3F4F5F6F3F7F5F1F9F3F4F8F8F0F0F0F4F8F6F7F1F5C4F2F8F0F3F6F0F1F1F6F5F2F5F7F8F4F0F1F5F0F0F0F8F5F0F0F0F1F0F0F0F0F1F9F8F7F6F5F4F3F2F1F2F3F4F5F6F7F8F9F0F0F0F0F0F0D4859983888195A3409581948540404040404040404040D4859983888195A3408389A3A840E4E2C1F0F3F7E9F7F5F3F2F0F1F0F3F0F2F8F0F2F0F2F9F2F0F3F0F3F0F2F8F0F4F0F2F9F2F0F5F0F2F0F0F6F0F4F6F0F4B54E5CC2B73CFC06F1F1F15F2A02060482025C008407A0000000041010950500000400009A032505139C01019F02060000000080009F10120110250000044000DAC100000000000000009F1A0208409F2608124AA1ECE13A13E09F2701809F33036040209F34034201009F36028EA79F3704448325445F340101F0F2F5F0F0F0F0F0F1F0F0F0F0F5F0F0F6F0F4F9F0F2F1F0F1F2F3F4F0F0F9D4C3C3F0F0F0F3D6E5"}'

for i in {1..10}; do
    echo "Ejecutando iteración $i..."
    # Iterar sobre cada método e invocarlo
    for METHOD in "${METHODS[@]}"; do
        echo "Invocando $METHOD..."
        grpcurl -plaintext \
            -H "traceId:1234" -H "spanId:1234" -H "network:PEER02" -H "port:$PORT"  -H "simulation:true" \
            -d "$JSON_PAYLOAD" \
            -import-path proto \
            -proto orchestrator.proto \
            localhost:$GRPC_SERVER_PORT "$METHOD" 2>/dev/null

        # Verificar si la llamada tuvo éxito
        if [ $? -eq 0 ]; then
            echo "Método $METHOD cargado correctamente."
        else
            echo "Error al cargar el método $METHOD."
            ALL_METHODS_READY=false
        fi
    done
    echo "--------------------------------"
done
# Health check: marcar como ready si todos los métodos cargaron correctamente
if [ "$ALL_METHODS_READY" = true ]; then
    echo "Todos los métodos han sido cargados correctamente. Marcando el servicio como READY."
    # Crear un archivo que el readinessProbe verificará
    curl -X POST http://localhost:8083/mark-as-ready
    #touch /tmp/ready
else
    echo "No todos los métodos fueron cargados correctamente. El sistema no está listo."
    # Opcionalmente, podrías terminar el contenedor con un error
    curl -X POST http://localhost:8083/mark-as-not-ready
    exit 1
fi

# Supervisar el proceso de Java y mantener el contenedor vivo
echo "Supervisando el proceso principal (Java)."
wait $JAVA_PID
echo "El proceso de Java ha terminado. Deteniendo el contenedor."