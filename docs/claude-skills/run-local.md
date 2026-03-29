# Skill: run-local.md
# Ejecutar pgwp_java_orchestrator en local con terminal de logs

## Por qué existe este skill

Levantar el servicio localmente requiere:
- Buildear el fat JAR con Maven (perfil `gw`, proto generation)
- Inyectar las 29 variables de entorno requeridas en startup
- Abrir una terminal separada para seguir los logs en tiempo real

Sin este skill hay que recordar todas las variables y el proceso de build cada vez.

---

## Cuándo usar este skill

- Al desarrollar o depurar localmente sin los microservicios dependientes
- Para verificar que el servicio arranca correctamente después de un cambio
- Para ver los logs de arranque y detectar errores de configuración
- Como primer paso antes de enviar un mensaje gRPC de prueba

---

## Precondiciones

Verificar antes de ejecutar:

- [ ] Java 17 instalado: `java -version`
- [ ] Maven instalado: `mvn -version`
- [ ] Directorio del proyecto: `D:/home/bbva/gateway/fuentes/local/release/2.3/pgwp_java_orchestrator`
- [ ] `orchestratorlib 2.16.0` en el repo local: `~/.m2/repository-bbva/com/bbva/orchlib/orchestratorlib/2.16.0/`

---

## Paso 1 — Build del proyecto

```bash
cd D:/home/bbva/gateway/fuentes/local/release/2.3/pgwp_java_orchestrator
mvn clean package -DskipTests
```

**Qué hace:**
- `clean` — elimina el `target/` anterior
- `generate-sources` — compila los `.proto` (controlDialogo, dummy, orchestrator)
- `compile` — compila las 113 clases Java del proyecto
- `package` — genera `target/pgwp-orchestrator-2.3.5.jar` (fat JAR con Spring Boot)
- `-DskipTests` — omite tests para velocidad (~14 segundos)

**Señal de éxito:**
```
[INFO] BUILD SUCCESS
[INFO] Building jar: target/pgwp-orchestrator-2.3.5.jar
```

**Si falla:**
- Error de `orchestratorlib not found` → verificar `~/.m2/repository-bbva/com/bbva/orchlib/`
- Error de compilación proto → verificar archivos en `src/main/resources/proto/`

---

## Paso 2 — Detener instancia previa (si existe) y preparar logs

```bash
# Verificar si ya hay una instancia corriendo
jps -l | grep pgwp-orchestrator

# Si hay PID activo, detenerlo con PowerShell (único método confiable en Windows)
powershell -command "Stop-Process -Id <PID> -Force"

# Crear directorio de logs si no existe
mkdir -p D:/home/bbva/gateway/fuentes/local/release/2.3/pgwp_java_orchestrator/logs

# Limpiar log anterior
> D:/home/bbva/gateway/fuentes/local/release/2.3/pgwp_java_orchestrator/logs/pgwp-local.log
```

> En Windows, `kill <PID>` y `taskkill /PID <PID> /F` fallan desde bash.
> Usar siempre `powershell -command "Stop-Process -Id <PID> -Force"`.
>
> Verificar que los puertos quedaron libres antes de continuar:
> ```bash
> netstat -ano | grep -E ":9097|:8083"
> # Si no devuelve nada, los puertos están libres
> ```

---

## Paso 3 — Abrir terminal de logs ANTES de iniciar el servicio

Abrir una nueva ventana de consola Windows que seguirá el archivo de log en tiempo real:

```bash
start "pgwp-orchestrator logs" cmd /k "powershell -noexit -command Get-Content -Path 'D:\home\bbva\gateway\fuentes\local\release\2.3\pgwp_java_orchestrator\logs\pgwp-local.log' -Wait"
```

> La ventana aparecerá vacía hasta que el servicio empiece a escribir logs en el Paso 4.

---

## Paso 4 — Iniciar el microservicio en background

Ejecutar en background, redirigiendo stdout y stderr al archivo de log:

```bash
cd D:/home/bbva/gateway/fuentes/local/release/2.3/pgwp_java_orchestrator && \
GRPC_SERVER_ADDRESS=0.0.0.0 \
GRPC_SERVER_PORT=9097 \
ORCHESTRATION_FLOWHANDLER_ADDRESS=localhost \
ORCHESTRATION_FLOWHANDLER_PORT=19092 \
ORCHESTRATION_MONITOR_ADDRESS=localhost \
ORCHESTRATION_MONITOR_PORT=19096 \
ORCHESTRATION_FRAUD_ADDRESS=localhost \
ORCHESTRATION_FRAUD_PORT=19095 \
ORCHESTRATION_CRYPTO_ADDRESS=localhost \
ORCHESTRATION_CRYPTO_PORT=19094 \
ORCHESTRATION_APICONNECTOR_ADDRESS=localhost \
ORCHESTRATION_APICONNECTOR_PORT=19093 \
ORCHESTRATION_DIALOG_ADDRESS=localhost \
ORCHESTRATION_DIALOG_PORT=19098 \
ORCHESTRATION_EVENTS_ADDRESS=localhost \
ORCHESTRATION_EVENTS_PORT=19091 \
ORCHESTRATION_DUMMYPROCESSOR_ADDRESS=localhost \
ORCHESTRATION_DUMMYPROCESSOR_PORT=19099 \
ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003=pgwp-proxyprocessor-peer02-7003 \
ORCHESTRATION_PROXYPROCESSOR_PORT_7003=19240 \
ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234=pgwp-proxyprocessor-peer01-1234 \
ORCHESTRATION_PROXYPROCESSOR_PORT_1234=19200 \
ORCHESTRATION_PROXYHOST_ADDRESS_7003=pgwp-proxyhost-peer02-7003 \
ORCHESTRATION_PROXYHOST_PORT_7003=19260 \
ORCHESTRATION_PROXYHOST_ADDRESS_1234=pgwp-proxyhost-peer01-1234 \
ORCHESTRATION_PROXYHOST_PORT_1234=19220 \
ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIME=1200 \
ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIMEOUT=60 \
ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_WITHOUT_CALLS=true \
java -jar target/pgwp-orchestrator-2.3.5.jar >> logs/pgwp-local.log 2>&1
```

> Ejecutar con `run_in_background: true` para que no bloquee la conversación.

---

## Paso 5 — Verificar startup exitoso

Esperar ~10 segundos y verificar en el archivo de log (o en la terminal abierta en Paso 3):

```bash
grep -E "Started OrchestratorApplication|gRPC Server started|ERROR" \
  D:/home/bbva/gateway/fuentes/local/release/2.3/pgwp_java_orchestrator/logs/pgwp-local.log
```

**Señales de startup correcto:**
```
gRPC Server started, listening on address: 0.0.0.0, port: 9097
Started OrchestratorApplication in 7.5 seconds
```

**Health check HTTP:**
```bash
curl http://localhost:8083/actuator/health
```

---

## Paso 6 — Detener el servicio

Para detener el proceso cuando ya no se necesite:

```bash
# Encontrar el PID
jps -l | grep pgwp-orchestrator

# Terminar el proceso
kill <PID>
```

---

## Puertos que expone el servicio (local)

| Puerto | Protocolo | Propósito |
|--------|-----------|-----------|
| `9097` | gRPC | Entrada principal — recibe mensajes ISO-8583 |
| `8083` | HTTP | Actuator: `/actuator/health`, `/actuator/info` |

---

## Variables de entorno — explicación

### Por qué tienen valores placeholder
Los microservicios dependientes (Monitor, Proxy, Fraud, etc.) no corren localmente.
Los canales gRPC hacia ellos son **lazy** — no conectan hasta la primera llamada real.
El servicio arranca correctamente con `localhost:1909X` aunque esos puertos no existan.

### Regla crítica de los proxies
Los valores de `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_*` y `ORCHESTRATION_PROXYHOST_ADDRESS_*`
**deben contener el nombre de la red y el puerto como subcadenas**, porque kgwy selecciona
el proxy evaluando `channelServer.contains(network) && channelServer.contains(port)`.

| Variable | Valor local | Subcadenas requeridas |
|----------|-------------|----------------------|
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003` | `pgwp-proxyprocessor-peer02-7003` | `peer02` (PEER02) + `7003` |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234` | `pgwp-proxyprocessor-peer01-1234` | `peer01` (PEER01) + `1234` |
| `ORCHESTRATION_PROXYHOST_ADDRESS_7003` | `pgwp-proxyhost-peer02-7003` | `peer02` + `7003` |
| `ORCHESTRATION_PROXYHOST_ADDRESS_1234` | `pgwp-proxyhost-peer01-1234` | `peer01` + `1234` |

### Las 3 variables de keep-alive de proxy son obligatorias
`ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIME`, `_TIMEOUT` y `_WITHOUT_CALLS`
no tienen fallback en kgwy — su ausencia causa `NullPointerException` en startup.

---

## Perfiles Spring activos

Al iniciar, el perfil `gw` carga los 7 perfiles simultáneamente:

```
gw → global, local, data, datalocal, validations, sensitivedata
```

Se ven en los logs como:
```
The following 7 profiles are active: "gw","global","local","data","datalocal","validations","sensitivedata"
```

---

## Antipatrones

- **No usar `mvn spring-boot:run`**: el `@spring.profiles.active@` es un placeholder Maven
  que solo se resuelve durante la fase `process-resources`. Con `spring-boot:run` directo
  el perfil puede no resolverse correctamente. Usar siempre `mvn package` + `java -jar`.

- **No omitir las 3 variables de keep-alive de proxy**: a diferencia de otras variables,
  estas no tienen valor por defecto en kgwy y causan excepción en startup.

- **No cambiar los valores de los proxies a solo `localhost`**: sin las subcadenas de red
  y puerto, kgwy no puede seleccionar el canal correcto al enrutar mensajes.

---

## Referencias

- `CLAUDE.md §6` — lista completa de variables de entorno requeridas
- `docs/claude-skills/setup-new-environment.md` — configuración de ambiente real (no local)
- `src/main/resources/application.yml` — fuente de verdad de las variables requeridas
- `src/main/resources/application-local.yml` — reglas de orquestación PEER01/PEER02
