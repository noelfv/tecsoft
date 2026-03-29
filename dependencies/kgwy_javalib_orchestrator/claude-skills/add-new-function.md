# Skill: add-new-function

## Cuándo usar este skill
Cuando se necesita agregar una **nueva función disponible** en el motor de reglas de
`kgwy_javalib_orchestrator` — una función que los consumidores puedan referenciar en el
campo `function` de su `application-local.yml`. Este skill **requiere modificar código Java**.

## Audiencia
**Equipo de `kgwy_javalib_orchestrator` exclusivamente.**
Los consumidores NO usan este skill — ellos solo usan las funciones ya registradas.

---

> ⚠️ **ADVERTENCIA: Este skill modifica el código Java de `kgwy_javalib_orchestrator`.**
>
> A diferencia de `add-network-rule.md` y `add-new-network.md`, este cambio afecta a
> **TODOS los consumidores de `kgwy_javalib_orchestrator`** — no solo al que lo solicita.
>
> Antes de ejecutar:
> - Coordinar con el equipo de kgwy
> - Verificar que el microservicio objetivo existe en arqGw (ver `dependencies/kgwy_javalib_gateway/CONTRACT.md`)
> - Planificar la actualización de documentación
> - Comunicar a todos los consumidores que la nueva función estará disponible

---

## Contexto que Claude necesita antes de ejecutar
- `CLAUDE.md §4` — patrón de cliente gRPC y patrón de registro en `OrchestrationsHandler`
- `docs/architecture.md §4.4` — tabla actual de funciones
- `src/main/java/com/bbva/orchlib/command/OrchestrationsHandler.java` — código de referencia
- `dependencies/kgwy_javalib_gateway/CONTRACT.md` — verificar que el servicio existe en arqGw

---

## El patrón del proyecto

Las funciones disponibles en el motor se registran en `OrchestrationsHandler` siguiendo este patrón:

```java
// 1. Constante de nombre (la clave del YML)
private static final String NUEVO_SERVICIO = "nuevoservicio";

// 2. Bean del cliente
private final GrpcNuevoServicioClient grpcNuevoServicioClient;

// 3. Lógica de invocación condicional
private ISO20022 processNuevoServicioRuleIfPresent(List<String> arrOrchList, ISO20022 iso20022) {
    if (RulesOrchestrator.findRule(arrOrchList, NUEVO_SERVICIO)) {
        return grpcNuevoServicioClient.callNuevoServicioService(
            grpcConnection.getNuevoServicio().getChannelServer(),
            grpcConnection.getNuevoServicio().getChannelPort(),
            iso20022);
    }
    return iso20022;
}

// 4. Invocación en processOrchestrationRules() en la posición deseada
iso20022map = processNuevoServicioRuleIfPresent(arrOrchList, iso20022map); // posición X
```

---

## Pasos

### Paso 1 — Crear el cliente gRPC en `grpcclient/`

Crear `GrpcNuevoServicioClient.java` siguiendo el patrón exacto de `CLAUDE.md §7`.
El patrón es obligatorio — no se aceptan variaciones:

```java
@Component
public class GrpcNuevoServicioClient {

    private static final String LOG_PREFIX = "[GrpcNuevoServicioClient]: ";
    private static final String LABEL_ERROR = "gw_error_callNuevoServicioService";

    private final AtomicReference<ManagedChannel> channel = new AtomicReference<>();
    private NuevoServicioGrpc.NuevoServicioBlockingStub blockingStub;

    public void createChannelAndStub(String server, int port) {
        ManagedChannel currentChannel = channel.get();
        ManagedChannel newChannel = ManagedChannelBuilder.forAddress(server, port)
                .keepAliveTime(1200, TimeUnit.SECONDS)
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .intercept(new GrpcClientRequestInterceptor())  // new, nunca inyectado
                .usePlaintext()
                .build();
        if (channel.compareAndSet(currentChannel, newChannel)) {
            this.blockingStub = NuevoServicioGrpc.newBlockingStub(newChannel);
        } else {
            newChannel.shutdown();
        }
    }

    public ISO20022 callNuevoServicioService(String server, int port, ISO20022 iso20022) {
        try {
            if (channel.get() == null || channel.get().isShutdown()) {
                createChannelAndStub(server, port);
            }
            Iso20022Request req = Convert.mapIso20022DTOtoRequestGRPC(iso20022);
            if (!Objects.equals(GrpcHeadersInfo.getSimulation(), "true")) {
                LogsTraces.writeInfo(LOG_PREFIX + "Start call to NuevoServicio");
                Iso20022Response res = blockingStub.nuevoRpc(req);
                LogsTraces.writeInfo(LOG_PREFIX + "End call to NuevoServicio");
                return Convert.mapResponseGRPCtoIso20022DTO(res);
            } else {
                LogsTraces.writeInfo(LOG_PREFIX + "MODE: Simulation. Skipping NuevoServicio call.");
                return iso20022;
            }
        } catch (Exception e) {
            return ClientUtils.handleInternalServerException(
                LABEL_ERROR, LOG_PREFIX, "NuevoServicio", e.getMessage(), iso20022);
        }
    }

    @PreDestroy                   // ← SIEMPRE @PreDestroy, nunca @PostConstruct
    public void shutdown() {
        ClientUtils.shutdownChannel(channel.get());
    }
}
```

### Paso 2 — Verificar que el servicio existe en arqGw

Antes de continuar, confirmar en `dependencies/kgwy_javalib_gateway/CONTRACT.md` que:
- El stub gRPC `NuevoServicioGrpc` existe en la versión de arqGw en uso (actualmente `2.13.0`)
- El RPC `nuevoRpc` existe y acepta `Iso20022Request` y retorna `Iso20022Response`

Si el servicio no está en `CONTRACT.md`, **detener** — no se puede registrar una función
que apunta a un microservicio no disponible en arqGw.

### Paso 3 — Registrar en `OrchestrationsHandler`

Abrir `src/main/java/com/bbva/orchlib/command/OrchestrationsHandler.java` y:

**3a. Agregar la constante de nombre** (junto al resto de constantes al inicio):
```java
private static final String NUEVO_SERVICIO = "nuevoservicio";
```

**3b. Agregar el campo del cliente** (en el constructor via `@RequiredArgsConstructor`):
```java
private final GrpcNuevoServicioClient grpcNuevoServicioClient;
```

**3c. Agregar el método de invocación** siguiendo el patrón:
```java
private ISO20022 processNuevoServicioRuleIfPresent(List<String> arrOrchList, ISO20022 iso20022) {
    if (RulesOrchestrator.findRule(arrOrchList, NUEVO_SERVICIO)) {
        LogsTraces.writeInfo("[OrchestrationsHandler]: Processing NuevoServicio rule");
        return grpcNuevoServicioClient.callNuevoServicioService(
            grpcConnection.getNuevoServicio().getChannelServer(),
            grpcConnection.getNuevoServicio().getChannelPort(),
            iso20022);
    }
    return iso20022;
}
```

**3d. Invocar desde `processOrchestrationRules()`** en la posición correcta:
```java
// Orden actual en processOrchestrationRules():
iso20022map = processCryptoRuleIfPresent(arrOrchList, iso20022map);       // pos 1
iso20022map = processInsertMonitorRuleIfPresent(arrOrchList, iso20022map); // pos 2
if (!checkCryptoIsPresent(iso20022map)) {
    iso20022map = processFraudRuleIfPresent(arrOrchList, iso20022map);     // pos 3
    // ...
    iso20022map = processNuevoServicioRuleIfPresent(arrOrchList, iso20022map); // ← nueva pos X
    // ...
}
```

**3e. Inicializar el canal** en `@PostConstruct initializeClientChannels()`:
```java
grpcNuevoServicioClient.createChannelAndStub(
    grpcConnection.getNuevoServicio().getChannelServer(),
    grpcConnection.getNuevoServicio().getChannelPort());
```

### Paso 4 — Agregar la propiedad en `GrpcConnectionPropertiesLoad`

```java
// GrpcConnectionPropertiesLoad.java
@ConfigurationProperties(prefix = "grpc.client.global.services")
@Component
public class GrpcConnectionPropertiesLoad {
    // ... propiedades existentes
    private ConnectionProperties nuevoServicio;   // ← nueva propiedad
}
```

El consumidor deberá agregar en su `application.yml`:
```yaml
grpc:
  client:
    global:
      services:
        nuevoServicio:
          channelServer: <host>
          channelPort: <puerto>
```

### Paso 5 — Actualizar la documentación

Actualizar los 3 documentos afectados:

1. **`CLAUDE.md §4`** — agregar fila en la tabla de servicios orquestados:
   ```
   | X | `nuevoservicio` | NuevoServicio | `nuevoRpc` | GrpcNuevoServicioClient.callNuevoServicioService | [propósito] |
   ```

2. **`docs/architecture.md §4.4`** — agregar fila en la tabla de funciones disponibles.

3. **`docs/orchestration-rules-contract.md §4`** — agregar fila en la tabla de funciones.

---

## Checklist de validación

- [ ] El stub gRPC del nuevo servicio está en `dependencies/kgwy_javalib_gateway/CONTRACT.md`
- [ ] El nombre de función (constante Java) es **único** — no duplica ninguno existente en `OrchestrationsHandler`
- [ ] `GrpcNuevoServicioClient` sigue el patrón exacto de `CLAUDE.md §7` (AtomicReference, GrpcClientRequestInterceptor con `new`, @PreDestroy)
- [ ] La función se invoca en `processOrchestrationRules()` en la posición correcta respecto a dependencias de datos
- [ ] El canal se inicializa en `@PostConstruct initializeClientChannels()`
- [ ] `GrpcConnectionPropertiesLoad` tiene la nueva propiedad `ConnectionProperties nuevoServicio`
- [ ] `CLAUDE.md §4` actualizado con la nueva función
- [ ] `docs/architecture.md §4.4` actualizado
- [ ] `docs/orchestration-rules-contract.md §4` actualizado
- [ ] Los consumidores han sido notificados de que la nueva función está disponible

---

## Restricciones del motor de reglas

- **El nombre de la constante Java debe ser idéntico al string que usará el consumidor en el YML.** `NUEVO_SERVICIO = "nuevoservicio"` → el consumidor escribe `function: nuevoservicio`.
- **La comparación es `equals()` (sensible a mayúsculas).** Si la constante es `"nuevoServicio"` (camelCase), el consumidor también debe escribirlo exactamente así.
- **La posición en `processOrchestrationRules()` define el orden de ejecución.** Verificar que el nuevo servicio no necesita datos que producen servicios que están después en el orden actual.
- **Si `crypto` falla (check_crypto=true), los pasos 3-14 se saltan.** Si la nueva función va entre las posiciones 3-14, debe tolerar que no se ejecute cuando crypto falla.

---

## ⚠️ Antipatrones — NO hacer

### ❌ Agregar la función en el YML antes de registrarla en el código
```yaml
# application-local.yml del consumidor
function: monitor,nuevoservicio,host
# Si "nuevoservicio" no está en OrchestrationsHandler.processXxx:
# RulesOrchestrator.findRule(arrOrchList, "nuevoservicio") → false
# El servicio se ignora silenciosamente — sin error, sin log
```

### ❌ Usar @PostConstruct en el método shutdown del cliente
```java
@PostConstruct   // ← ERROR: destruye el canal al arrancar
public void shutdown() {
    ClientUtils.shutdownChannel(channel.get());
}
```
✅ Correcto: siempre `@PreDestroy` en el método de cierre. (`GrpcEventsClient` tiene este bug conocido — no reproducirlo.)

### ❌ Registrar función que apunta a microservicio no disponible en arqGw
```java
// Si NuevoServicioGrpc no existe en arqGw 2.13.0
private NuevoServicioGrpc.NuevoServicioBlockingStub blockingStub;
// → ClassNotFoundException en startup o error al compilar
```

### ❌ Inyectar GrpcClientRequestInterceptor con @Autowired
```java
// INCORRECTO
@Autowired
private GrpcClientRequestInterceptor interceptor;

ManagedChannelBuilder.forAddress(server, port)
    .intercept(interceptor)  // ← el interceptor tiene estado de contexto — no es compartible
```
✅ Correcto: siempre `new GrpcClientRequestInterceptor()` por canal.

---

## Cómo invocar este skill con Claude Code

```
execute docs/claude-skills/add-new-function.md
```
