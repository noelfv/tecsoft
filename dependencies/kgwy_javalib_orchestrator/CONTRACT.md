# CONTRACT.md
# Cómo pgwp_java_orchestrator usa kgwy_javalib_orchestrator

## Versión de kgwy_javalib_orchestrator usada

```xml
<!-- pom.xml de pgwp_java_orchestrator -->
<dependency>
    <groupId>com.bbva.orchlib</groupId>
    <artifactId>orchestratorlib</artifactId>
    <version>2.16.0</version>
</dependency>
```

arqGw subyacente: `2.13.0`
Spring Boot: `3.3.4` / Java 17
Versión del servicio: `pgwp-orchestrator 2.3.5`

## Documento fuente completo

```
dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md
```

## Característica principal de esta integración

pgwp_java_orchestrator **no tiene lógica Java propia de integración con kgwy**.
Su responsabilidad es:

1. **Configurar las reglas de orquestación** en `application-local.yml`
   — define qué microservicios se invocan para cada tipo de mensaje y red
2. **Configurar los datos de negocio** en `application-data.yml` y
   `application-datalocal.yml` — BINs, monedas, códigos de respuesta
3. **Implementar las interfaces requeridas** por kgwy — 5 puertos Java
4. **Mapear ISO-8583 ↔ ISO-20022** — es la lógica de negocio real de pgwp

La integración con kgwy es **declarativa — vía YML**.
El código Java de pgwp se ocupa del parsing/mapeo de mensajes.

---

## 2. Resumen de la integración

### Lo que pgwp_java_orchestrator delega completamente a kgwy_javalib_orchestrator

- Evaluación de las reglas del `application-local.yml` contra el ISO-20022
- Construcción de la cadena de microservicios a invocar (`arrOrchList`)
- Invocación gRPC secuencial de la cadena (Crypto, Monitor, Fraud, etc.)
- Propagación de trazabilidad (`traceid`, `spanid`) entre microservicios
- Serialización de errores de la cadena en `traceData` del ISO-20022
- Gestión de canales gRPC (reutilización, keep-alive, selección de proxy)
- Mecanismo de contingencia (`check_contingency=true` → re-ejecución)
- Cortocircuito post-Crypto (`check_crypto=true` → salta pasos 3-14)
- Evaluación de `global.validations` y `local.validations`

### Lo que pgwp_java_orchestrator provee a kgwy_javalib_orchestrator

| Qué provee | Dónde | Obligatorio |
|-----------|-------|-------------|
| Reglas de orquestación (`local.orchestrations`) | `application-local.yml` | Sí |
| Aliases de campos (`local.filterLabels`) | `application-local.yml` | Sí |
| Validaciones globales (`global.validations`) | `application-global.yml` | Sí (vacío en pgwp) |
| BINs y monedas válidas | `application-data.yml` | Sí |
| Códigos de respuesta y campos por MTI | `application-datalocal.yml` | Sí |
| Conexiones a microservicios | `application.yml` | Sí |
| `IParser` implementado | `OrchestratorFlowProcess.java` | Sí |
| `IGrpcControlDialogoClient` implementado | `GrpcControlDialogoClient.java` | Sí |
| `IGrpcDummyClient` implementado | `GrpcDummyClient.java` | Sí |
| `IValidationsLocal` implementado | `ValidationsLocal.java` | Sí |
| `IValidationsLocalErr` implementado | `ValidationsErrorLocal.java` | Sí |

### Lo que pgwp_java_orchestrator usa directamente de arqGw

Documentado en:
```
dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md
```

Resumen:
- `LogsTraces` — trazabilidad en 11 archivos Java de pgwp
- `GrpcHeadersInfo` — acceso a headers gRPC en 6 archivos Java de pgwp
- `ISO20022` + ~15 DTOs — modelo de datos que atraviesa toda la capa de mapeo

### Lo que kgwy_javalib_orchestrator ofrece pero pgwp_java_orchestrator NO usa

| Capacidad de kgwy | ¿La usa pgwp? | Razón |
|-------------------|--------------|-------|
| Función `crypto` (tokenización) | No | pgwp no tokeniza — trabaja en modo passthrough |
| Función `fraud`/`fraudasync` | No | No aplica para el flujo de pgwp |
| Función `dialog` | No | `GrpcControlDialogoClient` devuelve pass-through |
| Función `dialogcontrol` (arqGw) | No | Excluido del ComponentScan |
| Función `apiconnector` | No | No aplica para el flujo de pgwp |
| Función `dummyprocessor` | No | `GrpcDummyClient` no está en ninguna regla |
| Función `events` | No | No aplica para el flujo de pgwp |
| Función `flowhandler`/`flowhandlerasync` | No | No aplica para el flujo de pgwp |
| Función `feedbackfraud` | No | No aplica para el flujo de pgwp |
| Función `uncrypto` (destokenización) | No | Sin crypto, no hay destokenización |
| Operadores `Greater`, `Lower`, `Range` | No | pgwp solo evalúa tipos de mensaje (In/Equals) |
| Operadores `StartWith`, `EndWith`, `NotIn`, `NotEquals` | No | No necesarios para las reglas de pgwp |
| `local.validations` con reglas | No | pgwp tiene `validations:` vacío en el YML |
| `global.validations` con reglas | No | pgwp tiene `validations:` vacío en el YML |
| `ParserException` de orchlib | No | `OrchestratorFlowProcess` maneja errores con sus propias excepciones |

---

## 3. Integración via YML — el contrato principal

### FilterLabels configurados en pgwp

```yaml
# application-local.yml
local:
  filterLabels:
    pan: Environment/Card/Pan
    messageType: AddendumData/AdditionalData("UNSP")/Value
```

pgwp usa **solo 2 aliases** en sus reglas de orquestación:
- `messageType` → valor del campo UNSP en AddendumData (tipo de mensaje ISO-8583)
- `pan` → PAN de la tarjeta (disponible aunque no se usa en condiciones de reglas)

### Redes configuradas en pgwp

| Red | Marca | Propósito | Tipos de mensaje que maneja |
|-----|-------|-----------|---------------------------|
| `PEER01` | Visa | Transacciones Visa de BBVA Perú | 0100, 0101, 0120, 0110, 0130, 0302, 0312, 0400, 0401, 0420, 0410, 0430, 0800, 0810 |
| `PEER02` | Mastercard | Transacciones Mastercard de BBVA Perú | 0100, 0120, 0110, 0130, 0190, 0302, 0312, 0400, 0420, 0410, 0430, 0800, 0810 |

### Reglas configuradas por pgwp

**PEER01 (Visa) — 6 reglas en orden de evaluación:**

| Prioridad | Condición | Función | Microservicios invocados (orden fijo kgwy) |
|-----------|-----------|---------|------------------------------------------|
| 1 | messageType In `0100,0120,0400,0420,0101,0401` | `monitor,host` | MonitorService (pos.2) → ProxyService/host (pos.16) |
| 2 | messageType In `0110,0130,0410,0430` | `updatemonitor,processor` | MonitorService/update (pos.14) → ProxyService/processor (pos.15) |
| 3 | messageType In `0312` | `host` | ProxyService/host (pos.16) |
| 4 | messageType In `0302` | `processor` | ProxyService/processor (pos.15) |
| 5 | messageType Equals `0800` | `processor` | ProxyService/processor (pos.15) |
| 6 | messageType Equals `0810` | `host` | ProxyService/host (pos.16) |

**PEER02 (Mastercard) — 6 reglas en orden de evaluación:**

| Prioridad | Condición | Función | Microservicios invocados (orden fijo kgwy) |
|-----------|-----------|---------|------------------------------------------|
| 1 | messageType In `0100,0120,0400,0420` | `monitor,host` | MonitorService (pos.2) → ProxyService/host (pos.16) |
| 2 | messageType In `0110,0130,0410,0430` | `updatemonitor,processor` | MonitorService/update (pos.14) → ProxyService/processor (pos.15) |
| 3 | messageType In `0312,0190` | `host` | ProxyService/host (pos.16) |
| 4 | messageType In `0302` | `processor` | ProxyService/processor (pos.15) |
| 5 | messageType Equals `0800` | `processor` | ProxyService/processor (pos.15) |
| 6 | messageType Equals `0810` | `host` | ProxyService/host (pos.16) |

### Fragmento real del YML de pgwp

```yaml
# src/main/resources/application-local.yml (completo)
local:
  filterLabels:
    pan: Environment/Card/Pan
    messageType: AddendumData/AdditionalData("UNSP")/Value
  orchestrations:
    - network: PEER01
      rules:
        # Casos Pass-through - Compras
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420,0101,0401"
          function: monitor,host
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0110,0130,0410,0430"
          function: updatemonitor,processor
        # Casos Pass-through
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0312"
          function: host
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0302"
          function: processor
        # Casos Mensajes de Red
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0800"
          function: processor
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0810"
          function: host
    - network: PEER02
      rules:
        # Casos Pass-through - Compras
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420"
          function: monitor,host
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0110,0130,0410,0430"
          function: updatemonitor,processor
        # Casos Pass-through
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0312,0190"
          function: host
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0302"
          function: processor
        # Casos Mensajes de Red
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0800"
          function: processor
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0810"
          function: host
  validations:
```

### Diferencias con los ejemplos de kgwy_javalib_orchestrator

| Aspecto | Ejemplo de kgwy | pgwp_java_orchestrator |
|---------|----------------|------------------------|
| Regla de compras PEER01 | `0100,0120,0400,0420` | Añade `0101,0401` (mensajes de aviso/advice) |
| Mensajes de red PEER02 | `0810 → host` solamente | Añade regla `0312,0190 → host` |
| `local.validations` | Puede tener reglas | Vacío — pgwp delega toda validación a kgwy/arqGw |
| `global.validations` | Puede tener reglas | Vacío — sin validaciones globales adicionales |
| Redes con condiciones múltiples | Ejemplo con 2 condiciones AND | pgwp usa solo 1 condición por regla |

---

## 4. Integración Java — puertos implementados

### Los 5 puertos requeridos por kgwy_javalib_orchestrator

**⚠️ CRÍTICO**: pgwp implementa los 5 puertos que kgwy requiere. Sin estas
implementaciones el startup de Spring falla.

**`IParser` → `OrchestratorFlowProcess`**
```java
// src/main/java/com/bbva/orchestrator/core/OrchestratorFlowProcess.java
@Component
@RequiredArgsConstructor
public class OrchestratorFlowProcess implements IParser {

    @Override
    public ISO20022 convert8583to20022(String originalMessage) {
        // Selecciona parser por red (GrpcHeadersInfo.getNetwork())
        // Parsea ISO-8583 hex → campos → ISO-20022 vía estrategias de mapeo
        ISO8583DelegateParser delegateParser = parserFactory.getDelegateParser(GrpcHeadersInfo.getNetwork());
        // ...
    }

    @Override
    public String convert20022to8583(ISO20022 iso20022) {
        // Si isNextGen → paymentAuthorization (mapeo completo)
        // Si no → passthrough (devuelve ISO8583_HOST de addendumData)
        Boolean isNextGen = iso20022.getMonitoring().getIsNextGen();
        if(isNextGen){
            return flowPaymentAuthorization(iso20022);
        }
        return flowPassThrough(iso20022, "ISO8583_HOST");
    }
}
```

**`IGrpcControlDialogoClient` → `GrpcControlDialogoClient`** (pass-through vacío)
```java
// src/main/java/com/bbva/orchestrator/grpcclient/GrpcControlDialogoClient.java
@Component
public class GrpcControlDialogoClient implements IGrpcControlDialogoClient {
    @Override
    public ISO20022 callControlDialogoService(String serverGrpcClient, int portGrpcClient, ISO20022 iso20022) {
        return iso20022; // pass-through — pgwp no usa dialog
    }
}
```

**`IGrpcDummyClient` → `GrpcDummyClient`** (pass-through vacío)

**`IValidationsLocal` → `ValidationsLocal`**
```java
// src/main/java/com/bbva/orchestrator/validations/ValidationsLocal.java
@Component
public class ValidationsLocal implements IValidationsLocal {
    @Override
    public boolean validationsLocal(ISO20022 iso20022) {
        return checkValidations.checkValidationsLocal(iso20022, this);
    }
}
```

**`IValidationsLocalErr` → `ValidationsErrorLocal`**
```java
// src/main/java/com/bbva/orchestrator/validations/ValidationsErrorLocal.java
@Component
public class ValidationsErrorLocal implements IValidationsLocalErr {
    @Override
    public ISO20022 validationsLocalErr(ISO20022 iso20022, String validationName) {
        iso20022.getProcessingResult().getResultData().setResult("Error");
        return iso20022;
    }
}
```

### Nota sobre la integración Java

pgwp no tiene lógica Java propia de integración con kgwy más allá de los 5 puertos.
El código Java relacionado con la infraestructura de kgwy/arqGw es
exclusivamente uso transitivo de arqGw — documentado en:
```
dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md
```

**ComponentScan (mecanismo de activación de arqGw en pgwp):**
```java
// OrchestratorApplication.java
@ComponentScan(basePackages = {"com.bbva.orchlib", "com.bbva.orchestrator", "com.bbva.gateway"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {DialogControlHandler.class, IDialogControl.class, GrpcDialogControlService.class}
    ))
```
→ Documentación completa en `TRANSITIVE-CONTRACT.md` Sección 3.

**Uso de LogsTraces:**
```java
// DefaultDelegateMapper.java:53
LogsTraces.writeInfo("requestMessage %s".formatted(input.getPlainTextPCI()));
// MonitoringBuilder.java:85
LogsTraces.writeError("Error creating monitoring: " + e.getMessage());
```
→ Documentación completa en `TRANSITIVE-CONTRACT.md` Sección 2.

**Acceso a GrpcHeadersInfo:**
```java
// TraceDataMappingStrategy.java:36
.value(GrpcHeadersInfo.getTraceId())
// MapperFactory.java:48
String peerId = GrpcHeadersInfo.getNetwork();
```
→ Documentación completa en `TRANSITIVE-CONTRACT.md` Sección 3.

---

## 5. Configuración técnica de pgwp

### Microservicios configurados para kgwy (application.yml)

```yaml
grpc:
  server:
    address: ${GRPC_SERVER_ADDRESS}
    port: ${GRPC_SERVER_PORT}
    enable-keep-alive: true
    keep-alive-time: 1200s
    keep-alive-timeout: 60s
  client:
    GLOBAL:
      enable-keep-alive: true
      keep-alive-time: 1200s
      keep-alive-timeout: 60s
      services:
        flowhandler:
          channel-server: ${ORCHESTRATION_FLOWHANDLER_ADDRESS}
          channel-port: ${ORCHESTRATION_FLOWHANDLER_PORT}
        monitor:
          channel-server: ${ORCHESTRATION_MONITOR_ADDRESS}
          channel-port: ${ORCHESTRATION_MONITOR_PORT}
        fraud:
          channel-server: ${ORCHESTRATION_FRAUD_ADDRESS}
          channel-port: ${ORCHESTRATION_FRAUD_PORT}
        crypto:
          channel-server: ${ORCHESTRATION_CRYPTO_ADDRESS}
          channel-port: ${ORCHESTRATION_CRYPTO_PORT}
        apiconnector:
          channel-server: ${ORCHESTRATION_APICONNECTOR_ADDRESS}
          channel-port: ${ORCHESTRATION_APICONNECTOR_PORT}
        dialogcontrol:
          channel-server: ${ORCHESTRATION_DIALOG_ADDRESS}
          channel-port: ${ORCHESTRATION_DIALOG_PORT}
        events:
          channel-server: ${ORCHESTRATION_EVENTS_ADDRESS}
          channel-port: ${ORCHESTRATION_EVENTS_PORT}
        dummyprocessor:
          channel-server: ${ORCHESTRATION_DUMMYPROCESSOR_ADDRESS}
          channel-port: ${ORCHESTRATION_DUMMYPROCESSOR_PORT}
        processor:
          - channel-server: ${ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003}
            channel-port: ${ORCHESTRATION_PROXYPROCESSOR_PORT_7003}
          - channel-server: ${ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234}
            channel-port: ${ORCHESTRATION_PROXYPROCESSOR_PORT_1234}
        host:
          - channel-server: ${ORCHESTRATION_PROXYHOST_ADDRESS_7003}
            channel-port: ${ORCHESTRATION_PROXYHOST_PORT_7003}
          - channel-server: ${ORCHESTRATION_PROXYHOST_ADDRESS_1234}
            channel-port: ${ORCHESTRATION_PROXYHOST_PORT_1234}
```

> **⚠️ Regla de naming para proxies**: `channel-server` de cada proxy debe
> contener el nombre de la red (`PEER01`/`PEER02`) y el puerto (`7003`/`1234`)
> como subcadenas. El selector de kgwy usa
> `channelName.contains(network) && channelName.contains(port)`.

### Variables de entorno requeridas por pgwp

**Del servidor gRPC propio:**

| Variable | Descripción |
|----------|-------------|
| `GRPC_SERVER_ADDRESS` | Dirección de escucha del servidor gRPC de pgwp |
| `GRPC_SERVER_PORT` | Puerto de escucha del servidor gRPC de pgwp |

**De los microservicios de la cadena (requeridas por kgwy):**

| Variable | Microservicio |
|----------|--------------|
| `ORCHESTRATION_FLOWHANDLER_ADDRESS/PORT` | FlowHandlerService |
| `ORCHESTRATION_MONITOR_ADDRESS/PORT` | MonitorService |
| `ORCHESTRATION_FRAUD_ADDRESS/PORT` | FraudService |
| `ORCHESTRATION_CRYPTO_ADDRESS/PORT` | CryptoService |
| `ORCHESTRATION_APICONNECTOR_ADDRESS/PORT` | ApiConnectorService |
| `ORCHESTRATION_DIALOG_ADDRESS/PORT` | DialogControlService |
| `ORCHESTRATION_EVENTS_ADDRESS/PORT` | EventService |
| `ORCHESTRATION_DUMMYPROCESSOR_ADDRESS/PORT` | DummyProcessorService |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003/PORT_7003` | ProxyService/processor (PEER01) |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234/PORT_1234` | ProxyService/processor (PEER02) |
| `ORCHESTRATION_PROXYHOST_ADDRESS_7003/PORT_7003` | ProxyService/host (PEER01) |
| `ORCHESTRATION_PROXYHOST_ADDRESS_1234/PORT_1234` | ProxyService/host (PEER02) |

**Del keep-alive de proxies (requeridas por kgwy — sin fallback):**

| Variable | Tipo | Descripción |
|----------|------|-------------|
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIME` | Long (segundos) | Tiempo de keep-alive para canales proxy |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIMEOUT` | Long (segundos) | Timeout de keep-alive para canales proxy |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_WITHOUT_CALLS` | Boolean | Mantener keep-alive sin llamadas activas |

> ⚠️ Las 3 variables de keep-alive no tienen fallback en kgwy — su ausencia
> causa `NullPointerException` o `NumberFormatException` en startup.

### Timeout configurado en pgwp

```yaml
# application.yml — keep-alive del servidor gRPC de pgwp
grpc:
  server:
    keep-alive-time: 1200s        # tiempo sin actividad antes de enviar ping
    keep-alive-timeout: 60s       # tiempo esperando respuesta al ping
  client:
    GLOBAL:
      keep-alive-time: 1200s      # keep-alive de canales hacia microservicios
      keep-alive-timeout: 60s
```

pgwp **no configura un timeout de deadline** para el request `PostProcessMessage`.
El timeout efectivo es el keep-alive de 1200s del canal.

> **⚠️ PENDIENTE DE VALIDACIÓN**: Evaluar si se debe agregar un timeout de
> deadline gRPC explícito para el request `PostProcessMessage`, considerando
> que la cadena más larga de pgwp invoca 2 microservicios en secuencia
> (monitor + host/processor).

### Datos de negocio configurados por pgwp_java_orchestrator

**application-data.yml:**
- 120 monedas con código numérico y alfabético
- BINs PEER01 (Visa): 36+ BINs con descripción de producto
- BINs PEER02 (Mastercard): 8 BINs con descripción de producto
- Mapa P2P (`bank_p2p`): 20+ entidades bancarias con código y nombre
- Categorías de comercio (`merchant_type`): 130+ códigos MCC con descripción

**application-datalocal.yml:**
- Códigos de respuesta PEER01: mapeo label semántico → código ISO-8583
- Definición de campos mandatorios/opcionales por MTI (0110, 0410)

---

## 6. Lo que kgwy_javalib_orchestrator ofrece pero pgwp_java_orchestrator NO usa
— Para que Claude no busque su uso en el código —

| Capacidad de kgwy | ¿La usa pgwp? | Razón |
|-------------------|--------------|-------|
| Función `crypto` | No | Flujo passthrough — sin tokenización |
| Función `uncrypto` | No | Sin crypto, no hay uncrypto |
| Función `fraud` / `fraudasync` | No | No aplica para pgwp |
| Función `feedbackfraud` | No | No aplica para pgwp |
| Función `dialog` (IGrpcControlDialogoClient activo) | No | GrpcControlDialogoClient hace pass-through |
| Función `dialogcontrol` (arqGw) | No | DialogControlService excluido del ComponentScan |
| Función `apiconnector` | No | No aplica para pgwp |
| Función `dummyprocessor` | No | GrpcDummyClient no en ninguna regla |
| Función `events` | No | No aplica para pgwp |
| Función `flowhandler` / `flowhandlerasync` | No | No aplica para pgwp |
| Operador `Greater` / `Lower` / `Range` | No | Solo evalúa tipos de mensaje |
| Operador `StartWith` / `EndWith` | No | Solo evalúa tipos de mensaje |
| Operador `NotIn` / `NotEquals` | No | Reglas de pgwp son todas positivas |
| `local.validations` con reglas | No | Sección vacía en el YML |
| `global.validations` con reglas | No | Sección vacía en el YML |
| `ParserException` (orchlib) | No | pgwp no lanza ParserException |
| Contingencia (`check_contingency`) | No | Transparente — lo maneja kgwy |
| Cortocircuito (`check_crypto`) | No | Transparente — sin crypto configurado |
| `traceData` con errores de cadena | Sí (lectura) | pgwp puede leer `traceData` para detectar fallos |

---

## 7. Tabla de cobertura

| Capacidad de kgwy_javalib_orchestrator | ¿La usa pgwp? | Documentada en |
|----------------------------------------|--------------|----------------|
| Motor de reglas YML (`local.orchestrations`) | Sí | Sección 3 de este documento |
| FilterLabels (`local.filterLabels`) | Sí (2 aliases) | Sección 3 de este documento |
| Función `monitor` | Sí | Sección 3 — PEER01/PEER02 regla 1 |
| Función `updatemonitor` | Sí | Sección 3 — PEER01/PEER02 regla 2 |
| Función `host` (ProxyService) | Sí | Sección 3 — múltiples reglas |
| Función `processor` (ProxyService) | Sí | Sección 3 — múltiples reglas |
| IParser implementado | Sí | Sección 4 — OrchestratorFlowProcess |
| IGrpcControlDialogoClient | Sí (pass-through) | Sección 4 — GrpcControlDialogoClient |
| IGrpcDummyClient | Sí (pass-through) | Sección 4 — GrpcDummyClient |
| IValidationsLocal | Sí | Sección 4 — ValidationsLocal |
| IValidationsLocalErr | Sí | Sección 4 — ValidationsErrorLocal |
| `LogsTraces` (arqGw transitivo) | Sí | TRANSITIVE-CONTRACT.md Sección 2 |
| `GrpcHeadersInfo` (arqGw transitivo) | Sí | TRANSITIVE-CONTRACT.md Sección 3 |
| `ISO20022` + DTOs (arqGw transitivo) | Sí | TRANSITIVE-CONTRACT.md Sección 4 |
| `HeadersInterceptor` (via ComponentScan) | Sí (auto) | TRANSITIVE-CONTRACT.md Sección 3 |
| Errores en `traceData` | Sí (lectura) | orchestratorlib-contract.md §6 |
| Funciones crypto, fraud, events, etc. | No | Sección 6 de este documento |
| Operadores avanzados (Range, Greater, etc.) | No | Sección 6 de este documento |
| Validaciones locales/globales con reglas | No | Sección 6 de este documento |

---

## ⚠️ PENDIENTES DE VALIDACIÓN

1. **Timeout de deadline gRPC**: pgwp no configura un timeout de deadline
   explícito para el request `PostProcessMessage`. Evaluar si es necesario
   en producción considerando que la cadena más larga invoca 2 microservicios.

2. **Variables de proxy en `processor`/`host`**: Los `channel-server` de los
   proxies usan `_7003` y `_1234` como sufijos. Verificar con infraestructura
   que los hostnames reales contienen `PEER01`/`PEER02` y `7003`/`1234` como
   subcadenas (requisito de kgwy para la selección de proxy).

3. **Función `dialog` activa en YML**: La función `dialog` no aparece en ninguna
   regla del `application-local.yml` de pgwp, pero `GrpcControlDialogoClient`
   está registrado. Confirmar que no existe uso oculto o que el servicio de
   diálogo está correctamente desactivado.

---

## Siguiente paso

Con CONTRACT.md generado continúa con:
```
execute prompts/phase1-analyze.md
```
