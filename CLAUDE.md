# pgwp_java_orchestrator — CLAUDE.md

---

## 1. Identidad

- **Nombre**: pgwp_java_orchestrator
- **Artefacto Maven**: `com.bbva.orchestrator:pgwp-orchestrator:2.3.6`
- **Tipo**: Consumidor de kgwy_javalib_orchestrator con capa propia de conversión de protocolos

**Posición en el ecosistema:**
```
kgwy_javalib_gateway (arqGw 2.13.0)
    → kgwy_javalib_orchestrator (orchestratorlib 2.16.0) lo usa como base gRPC
        → pgwp_java_orchestrator consume orchestratorlib para orquestar sus microservicios
```

**Propósito:**
Convertir mensajes ISO-8583 de redes Visa (PEER01) y Mastercard (PEER02) a ISO-20022, registrarlos en MonitorService y reenviarlos al host/procesador emisor (ProxyService), implementando el flujo de autorización de tarjetas de BBVA Perú.

**Qué hace pgwp_java_orchestrator:**
- Parsea ISO-8583 hexadecimal a nivel de bitmap, campos y subcampos (TLV/fixed/variable) por red
- Mapea los campos ISO-8583 a ~15 secciones del modelo ISO-20022
- Define en su `application-local.yml` las reglas de orquestación que kgwy evalúa en runtime
- Construye los datos de monitoreo de la transacción (MonitoringBuilder)
- Implementa los 5 puertos requeridos por kgwy

**Qué NO hace pgwp_java_orchestrator:**
- No invoca gRPC directamente hacia los microservicios de la cadena
- No evalúa las reglas del YML (lo hace kgwy)
- No gestiona canales gRPC (lo hace kgwy/arqGw)
- No propaga trazabilidad entre microservicios (lo hace arqGw)
- No instancia clases de kgwy manualmente
- No expone API propia hacia otros componentes del ecosistema

---

## 2. Stack técnico

### Propio de pgwp_java_orchestrator
- **Java**: 17
- **Spring Boot**: 3.3.4
- **Build**: Maven (defaultGoal: `clean generate-sources compile install`)
- **Protobuf**: protoc-jar-maven-plugin 3.6.0.1 / grpc-java 1.62.2

### Dependencia directa
- **kgwy_javalib_orchestrator**: `orchestratorlib 2.16.0`
  - Contrato de uso: `dependencies/kgwy_javalib_orchestrator/CONTRACT.md`
  - Documentación completa: `dependencies/kgwy_javalib_orchestrator/`

### Dependencia transitiva visible
- **kgwy_javalib_gateway (arqGw)**: `2.13.0` (vía orchestratorlib)
  - pgwp usa directamente: `LogsTraces`, `GrpcHeadersInfo`, `ISO20022` y sus DTOs
  - Contrato transitivo: `dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md`
  - **IMPORTANTE**: arqGw **NO está en el pom.xml** de pgwp pero sus clases son visibles y usadas en el código

---

## 3. Reglas de orquestación de pgwp

### Tipo de integración con kgwy

La lógica de enrutamiento de pgwp vive **exclusivamente en el YML**.
No hay código Java de orquestación en pgwp — kgwy evalúa las reglas del
`application-local.yml` en runtime y construye la cadena de microservicios.

### Redes configuradas

| Red | Marca | Propósito |
|-----|-------|-----------|
| `PEER01` | Visa | Transacciones Visa — BBVA Perú |
| `PEER02` | Mastercard | Transacciones Mastercard — BBVA Perú |

La red se compara `equalsIgnoreCase` contra el header gRPC `network` del request entrante.

### Mensajes ISO-8583 que pgwp maneja

| Código MTI | Tipo | Red | Función kgwy invocada |
|------------|------|-----|-----------------------|
| 0100, 0101 | Solicitud de autorización / advice Visa | PEER01 | `monitor, host` |
| 0120, 0400, 0401, 0420 | Aviso de autorización / reversal | PEER01 | `monitor, host` |
| 0110, 0130, 0410, 0430 | Respuesta de autorización / reversal | PEER01 | `updatemonitor, processor` |
| 0312 | Pass-through Visa | PEER01 | `host` |
| 0302 | Pass-through Visa | PEER01 | `processor` |
| 0800 | Network Management Request | PEER01 | `processor` |
| 0810 | Network Management Response | PEER01 | `host` |
| 0100, 0120, 0400, 0420 | Solicitud / reversal Mastercard | PEER02 | `monitor, host` |
| 0110, 0130, 0410, 0430 | Respuesta Mastercard | PEER02 | `updatemonitor, processor` |
| 0312, 0190 | Pass-through / Reversal Advice Response | PEER02 | `host` |
| 0302 | Pass-through Mastercard | PEER02 | `processor` |
| 0800 | Network Management Request | PEER02 | `processor` |
| 0810 | Network Management Response | PEER02 | `host` |

> ⚠️ PEER01 incluye `0101, 0401` (mensajes advice) pero PEER02 no. Verificar
> si es intencional para Mastercard. PEER02 incluye `0190` que PEER01 no tiene.

### Funciones de kgwy que pgwp usa (de 16 disponibles)

| Función | En qué reglas | Microservicio invocado (pos. en cadena) |
|---------|--------------|----------------------------------------|
| `monitor` | Compras/reversals entrantes (0100, 0120, 0400...) | MonitorService.PostPatchInsertDocument (pos. 2) |
| `updatemonitor` | Respuestas (0110, 0130, 0410, 0430) | MonitorService.PostPatchUpdateDocument (pos. 14) |
| `host` | Compras entrantes, pass-through, mensajes de red | ProxyService.PostData ISO-8583 async (pos. 16) |
| `processor` | Respuestas, pass-through, mensajes de red | ProxyService.PostData ISO-8583 async (pos. 15) |

**Funciones de kgwy que pgwp NO usa**: `crypto`, `uncrypto`, `fraud`, `fraudasync`,
`feedbackfraud`, `dialog`, `dialogcontrol`, `apiconnector`, `dummyprocessor`,
`events`, `flowhandler`, `flowhandlerasync`

### Fragmento representativo del YML

```yaml
# application-local.yml — ejemplo de PEER01
local:
  filterLabels:
    pan: Environment/Card/Pan
    messageType: AddendumData/AdditionalData("UNSP")/Value
  orchestrations:
    - network: PEER01
      rules:
        # El orden importa — short-circuit: primera regla que aplica gana
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420,0101,0401"
          function: monitor,host        # orden en función ≠ orden de ejecución
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0110,0130,0410,0430"
          function: updatemonitor,processor
```

> **CRÍTICO**: `function: monitor,host` NO significa que `monitor` se ejecuta
> antes que `host`. El orden de ejecución está fijo en el código Java de kgwy
> (ver `orchestratorlib-contract.md §3`). `monitor` siempre es posición 2,
> `host` siempre es posición 16.

### Referencia a configuración completa

- `src/main/resources/application-local.yml` — reglas y filterLabels
- `src/main/resources/application-data.yml` — BINs, monedas, MCC
- `src/main/resources/application-datalocal.yml` — códigos de respuesta por red

---

## 3.5. Lógica Java propia de pgwp — Capa de conversión ISO-8583 ↔ ISO-20022

> **IMPORTANTE**: El phase1-analyze detectó que pgwp tiene lógica de negocio
> Java sustancial que no estaba documentada en el CONTRACT.md original.
> Esta sección la documenta. No es infraestructura de kgwy/arqGw — es código
> propio de pgwp.

### Dos flujos en IParser.convert20022to8583

```java
// OrchestratorFlowProcess.java — punto de entrada de ambos flujos
@Override
public String convert20022to8583(ISO20022 iso20022) {
    Boolean isNextGen = iso20022.getMonitoring().getIsNextGen();
    if (isNextGen) {
        return flowPaymentAuthorization(iso20022);  // flujo nextGen
    }
    return flowPassThrough(iso20022, "ISO8583_HOST");  // flujo passthrough
}
```

| Flujo | Cuándo activa | Qué hace |
|-------|--------------|----------|
| **nextGen** (`isNextGen=true`) | Respuesta procesada por un microservicio | Mapeo completo ISO-20022 → ISO-8583 vía estrategias |
| **passthrough** (`isNextGen=false`) | Respuesta del host sin modificar | Devuelve el string ISO-8583 original almacenado en `addendumData["ISO8583_HOST"]` |

> ⚠️ PENDIENTE DE VALIDACIÓN: ¿Qué microservicio pone `isNextGen=true`?
> La lógica que activa el flujo nextGen no está documentada.

### Capa de Parser ISO-8583 (convert8583to20022)

```
ParserFactory
  → selecciona parser por GrpcHeadersInfo.getNetwork()
  → VisaDelegateParser (PEER01) | MastercardDelegateParser (PEER02)
     → VisaProcessField / MastercardProcessField
        → lee bitmap hexadecimal bit a bit
        → delega cada campo a su FieldParserStrategy:
           AlphaNumericFieldParser, NumericFieldParser, HexadecimalFieldParser,
           LlvarLengthPrefixParser, BinaryStringFieldParser, PlainTextFieldParser
     → subcampos compuestos:
        CompositeFixedFieldParser  — longitud fija (campo 22, 60, 61, 63...)
        CompositeTlvFieldParser    — TLV (campo 48 Mastercard)
        CompositeVariableFieldParser — longitud variable (LLVAR)
  → FieldLogicFactory
     → VisaDelegateFieldLogic | MastercardDelegateFieldLogic
        → lógica de negocio por subcampo
        → VisaAxisOperator / MastercardAxisOperator:
           entryMode, ecommerceIndicator, ECI, channel, TPV indicator
```

### Capa de Mapper ISO-20022

```
MapperFactory
  → selecciona mapper por GrpcHeadersInfo.getNetwork()
  → DefaultDelegateMapper (lógica principal)
     → 10 estrategias SectionMappingStrategy:
        EnvironmentMappingStrategy   — terminal, tarjeta, adquirente
        TransactionMappingStrategy   — transacción, montos, moneda
        ContextMappingStrategy       — POS, ecommerce, canal
        TraceDataMappingStrategy     — trazabilidad (usa GrpcHeadersInfo.getTraceId())
        AddendumDataMappingStrategy  — trama ISO-8583 original preservada
        ProcessingResultMappingStrategy — resultado del procesamiento
        ProtectedDataMappingStrategy — PAN tokenizado
        SecurityTrailerMappingStrategy — MAC/SecurityTrailer
        SupplementaryDataMappingStrategy — datos suplementarios
        CustomDataLocalMappingStrategy — datos locales personalizados
     → MonitoringBuilder:
        binCode, binDescription, merchantName, merchantCategory, channel,
        operationFilter, transactionStatus, P2P data, timezone Peru
```

### Caches de datos de negocio

```
ApplicationDataCache (@PostConstruct)
  → lee BusinessDataLoad de orchlib (del application-data.yml)
  → indexa BINs por red (PEER01/PEER02), monedas, custom data (MCC, P2P banks)

ApplicationDataLocalCache (@PostConstruct)
  → lee BusinessDataLocalLoad de orchlib (del application-datalocal.yml)
  → indexa códigos de respuesta label→código por red
  → indexa campos mandatorios/opcionales por MTI
```

### Excepciones propias de pgwp

| Excepción | Código | Cuándo |
|-----------|--------|--------|
| `MapperFieldsException` | `PGWP-00121`, otros | Error en estrategia de mapeo ISO-20022 |
| `ParserFieldsException` | `PGWP-00140` | Error en subcampos del campo 48 |
| `LogicFieldsException` | — | Error en lógica de campos de red |
| `MandatoryFieldsException` | — | Campo obligatorio faltante |

---

## 4. Uso transitivo de arqGw

### Por qué pgwp usa clases de arqGw directamente

arqGw es dependencia interna de kgwy — no de pgwp.
Sin embargo tres elementos de arqGw atraviesan kgwy sin ser abstraídos
y pgwp los usa directamente en su código Java.

### Regla para Claude

Cuando veas `com.bbva.gateway.*` en el código de pgwp,
**NO lo trates como código propio de pgwp**.
Es uso transitivo de arqGw documentado en:
```
dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md
```

| Si ves en pgwp | Ir a |
|---------------|------|
| `LogsTraces` | TRANSITIVE-CONTRACT.md Sección 2 |
| `GrpcHeadersInfo` | TRANSITIVE-CONTRACT.md Sección 3 |
| `com.bbva.gateway.dto.iso20022.*` | TRANSITIVE-CONTRACT.md Sección 4 |
| `DialogControlHandler`, `GrpcDialogControlService`, `IDialogControl` | Excluidos del ComponentScan — no son uso funcional |
| Cualquier otra clase | ⚠️ USO NO DOCUMENTADO — notificar al equipo de kgwy |

### LogsTraces — trazabilidad

`com.bbva.gateway.utils.LogsTraces` — clase de métodos estáticos.
pgwp NO la inicializa — la usa directamente. arqGw propaga traceId automáticamente.

```java
// Uso en 11 archivos de pgwp
LogsTraces.writeInfo("requestMessage %s".formatted(input.getPlainTextPCI()));
LogsTraces.writeWarning("responseCode no encontrado: " + responseCode);
LogsTraces.writeError("Error creating monitoring: " + e.getMessage());
```

Documentación completa: `TRANSITIVE-CONTRACT.md Sección 2`

### GrpcHeadersInfo — acceso a headers gRPC

`com.bbva.gateway.interceptors.GrpcHeadersInfo` — accessor ThreadLocal poblado por `HeadersInterceptor` de arqGw.

```java
GrpcHeadersInfo.getNetwork()  // → "PEER01" o "PEER02" — selecciona parser/mapper
GrpcHeadersInfo.getPort()     // → puerto gRPC del request entrante
GrpcHeadersInfo.getTraceId()  // → traceid del header gRPC → PAYMENT_ID en traceData
```

Documentación completa: `TRANSITIVE-CONTRACT.md Sección 3`

### ISO20022 + DTOs — modelo de datos

`com.bbva.gateway.dto.iso20022.*` — el modelo de datos que IParser recibe y retorna.
Toda la capa de mapeo de pgwp trabaja con estos DTOs.

Documentación completa: `TRANSITIVE-CONTRACT.md Sección 4`

### Registro de beans de arqGw (ComponentScan)

pgwp no registra interceptores manualmente. El mecanismo es el `@ComponentScan`:

```java
// OrchestratorApplication.java
@ComponentScan(
    basePackages = {"com.bbva.orchlib", "com.bbva.orchestrator", "com.bbva.gateway"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {DialogControlHandler.class, IDialogControl.class, GrpcDialogControlService.class}
    ))
```

Al incluir `"com.bbva.gateway"`, Spring auto-descubre `HeadersInterceptor` y otros
beans de arqGw. pgwp excluye explícitamente el servicio de control de diálogo estándar.

### Excepciones de arqGw — pgwp NO las captura

pgwp no captura ninguna excepción `com.bbva.gateway.*`. Solo captura sus propias
excepciones (`MapperFieldsException`, etc.) y `RuntimeException`/`Exception` genéricos.

`StatusRuntimeException(INVALID_ARGUMENT)` de `HeadersInterceptor` llega al
**caller de pgwp** — pgwp nunca lo recibe porque el request es rechazado antes
de llegar al código de pgwp.

---

## 5. Dependencias de negocio

Lee en este orden antes de analizar cualquier código:

1. `dependencies/kgwy_javalib_orchestrator/CONTRACT.md`
   → **CRÍTICO**: cómo pgwp usa kgwy — leer siempre primero

2. `dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md`
   → **CRÍTICO**: clases de arqGw que pgwp usa directamente
   → leer antes de analizar cualquier código Java de pgwp con `com.bbva.gateway.*`

3. `dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md`
   → solo si necesitas profundidad sobre kgwy

### Reglas para Claude

**Sobre la lógica de orquestación (routing):**
El routing de pgwp vive en `application-local.yml` — no en código Java.
Para entender qué microservicios se invocan para un mensaje, leer el YML.

**Sobre la conversión de protocolos:**
La lógica de negocio real de pgwp es la capa ISO-8583 ↔ ISO-20022.
El código relevante está en `core/parser/`, `core/mapper/`, `core/network/`.
Ver Sección 3.5 de este documento antes de analizar esas clases.

**Sobre clases `com.bbva.gateway.*`:**
```
LogsTraces              → TRANSITIVE-CONTRACT.md Sección 2
GrpcHeadersInfo         → TRANSITIVE-CONTRACT.md Sección 3
dto.iso20022.*          → TRANSITIVE-CONTRACT.md Sección 4
Cualquier otra clase    → ⚠️ USO NO DOCUMENTADO
```

**Sobre clases `com.bbva.orchlib.*` en pgwp:**
```
IParser                 → implementado por OrchestratorFlowProcess
IGrpcControlDialogoClient → implementado por GrpcControlDialogoClient (pass-through)
IGrpcDummyClient        → implementado por GrpcDummyClient (pass-through)
IValidationsLocal       → implementado por ValidationsLocal
IValidationsLocalErr    → implementado por ValidationsErrorLocal
BusinessDataLoad        → usado por ApplicationDataCache (@PostConstruct)
BusinessDataLocalLoad   → usado por ApplicationDataLocalCache (@PostConstruct)
CheckValidations        → usado por ValidationsLocal
```

---

## 6. Configuración

### Perfil activo

```yaml
# application.yml
spring:
  profiles:
    active: @spring.profiles.active@      # inyectado en build Maven
    group:
      gw: global, local, data, datalocal, validations, sensitivedata
```

El perfil `gw` activa todos los YMLs de pgwp en un único arranque.

### Properties de kgwy configuradas en pgwp

```yaml
# application-local.yml — RulesLocalLoad de kgwy
local:
  filterLabels:
    pan: Environment/Card/Pan
    messageType: AddendumData/AdditionalData("UNSP")/Value
  orchestrations:
    - network: PEER01  # 6 reglas
    - network: PEER02  # 6 reglas
  validations:         # vacío — sin reglas de validación local

# application-global.yml — RulesGlobalLoad de kgwy
global:
  validations:         # vacío

# application-data.yml — BusinessDataLoad de kgwy
data:
  currency: [120 monedas]
  bins:
    - key: PEER01  [36+ BINs Visa con descripción]
    - key: PEER02  [8 BINs Mastercard con descripción]
  custom:
    bank_p2p: [20+ entidades bancarias]
    merchant_type: [130+ códigos MCC]

# application-datalocal.yml — BusinessDataLocalLoad de kgwy
datalocal:
  - network: "PEER01"
    custom:
      map_fields_response: [campos M/O por MTI: 0110, 0410]
      response_code: [mapeo label semántico → código ISO-8583]
# ⚠️ PEER02 no tiene entrada en datalocal
```

### Properties de arqGw configuradas en pgwp

```yaml
# application.yml — gRPC server (arqGw gestiona el servidor)
grpc:
  server:
    address: ${GRPC_SERVER_ADDRESS}
    port: ${GRPC_SERVER_PORT}
    enable-keep-alive: true
    keep-alive-time: 1200s
    keep-alive-timeout: 60s
    permit-keep-alive-time: 30s
  client:
    GLOBAL:
      enable-keep-alive: true
      keep-alive-time: 1200s
      keep-alive-timeout: 60s
      services:
        monitor:      channel-server: ${ORCHESTRATION_MONITOR_ADDRESS}
        flowhandler:  channel-server: ${ORCHESTRATION_FLOWHANDLER_ADDRESS}
        fraud:        channel-server: ${ORCHESTRATION_FRAUD_ADDRESS}
        crypto:       channel-server: ${ORCHESTRATION_CRYPTO_ADDRESS}
        apiconnector: channel-server: ${ORCHESTRATION_APICONNECTOR_ADDRESS}
        dialogcontrol: channel-server: ${ORCHESTRATION_DIALOG_ADDRESS}
        events:       channel-server: ${ORCHESTRATION_EVENTS_ADDRESS}
        dummyprocessor: channel-server: ${ORCHESTRATION_DUMMYPROCESSOR_ADDRESS}
        processor:    # lista de proxies — channelServer debe contener red y puerto
          - channel-server: ${ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003}
          - channel-server: ${ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234}
        host:
          - channel-server: ${ORCHESTRATION_PROXYHOST_ADDRESS_7003}
          - channel-server: ${ORCHESTRATION_PROXYHOST_ADDRESS_1234}
```

> ⚠️ Los `channelServer` de los proxies deben contener el nombre de la red
> (`PEER01`/`PEER02`) y el puerto (`7003`/`1234`) como subcadenas.
> El selector de kgwy usa `channelName.contains(network) && channelName.contains(port)`.

### Timeout

```yaml
grpc:
  server:
    keep-alive-time: 1200s    # keep-alive del canal del servidor gRPC
  client:
    GLOBAL:
      keep-alive-time: 1200s  # keep-alive de canales hacia microservicios
```

pgwp **no configura deadline de request** para `PostProcessMessage`.
El timeout efectivo es el keep-alive de canal (1200s).
kgwy tampoco configura deadline global sobre la cadena.

> ⚠️ PENDIENTE DE VALIDACIÓN: Evaluar deadline gRPC explícito. La cadena más
> larga de pgwp invoca 2 microservicios. Un canal sin deadline puede mantenerse
> hasta 1200s si un microservicio no responde.

### Variables de entorno requeridas en startup

| Variable | Propósito | Requerida por |
|----------|-----------|--------------|
| `GRPC_SERVER_ADDRESS` | Dirección del servidor gRPC de pgwp | arqGw |
| `GRPC_SERVER_PORT` | Puerto del servidor gRPC de pgwp | arqGw |
| `ORCHESTRATION_MONITOR_ADDRESS/PORT` | MonitorService | kgwy |
| `ORCHESTRATION_FLOWHANDLER_ADDRESS/PORT` | FlowHandlerService | kgwy |
| `ORCHESTRATION_FRAUD_ADDRESS/PORT` | FraudService | kgwy |
| `ORCHESTRATION_CRYPTO_ADDRESS/PORT` | CryptoService | kgwy |
| `ORCHESTRATION_APICONNECTOR_ADDRESS/PORT` | ApiConnectorService | kgwy |
| `ORCHESTRATION_DIALOG_ADDRESS/PORT` | DialogControlService | kgwy |
| `ORCHESTRATION_EVENTS_ADDRESS/PORT` | EventService | kgwy |
| `ORCHESTRATION_DUMMYPROCESSOR_ADDRESS/PORT` | DummyProcessorService | kgwy |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003/PORT_7003` | Proxy processor (contiene red y "7003") | kgwy |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234/PORT_1234` | Proxy processor (contiene red y "1234") | kgwy |
| `ORCHESTRATION_PROXYHOST_ADDRESS_7003/PORT_7003` | Proxy host (contiene red y "7003") | kgwy |
| `ORCHESTRATION_PROXYHOST_ADDRESS_1234/PORT_1234` | Proxy host (contiene red y "1234") | kgwy |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIME` | Keep-alive canales proxy (Long, segundos) | kgwy — sin fallback |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIMEOUT` | Timeout keep-alive proxy (Long, segundos) | kgwy — sin fallback |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_WITHOUT_CALLS` | Keep-alive sin llamadas (`"true"`/`"false"`) | kgwy — sin fallback |

> ⚠️ Las 3 variables de keep-alive de proxy no tienen fallback en kgwy.
> Su ausencia causa `NullPointerException` o `NumberFormatException` en startup.

---

## 7. Convenciones del proyecto

### Nomenclatura y organización del YML

- **Redes en mayúscula**: `PEER01`, `PEER02` — comparación `equalsIgnoreCase` con header gRPC
- **Comentarios por categoría**: las reglas se agrupan con comentarios
  (`# Casos Pass-through - Compras`, `# Casos Mensajes de Red`)
- **Reglas en orden de especificidad**: las más específicas primero
  (compras antes que mensajes de red) — el short-circuit hace que la primera que aplica gane
- **Una sola condición por regla**: pgwp no usa condiciones AND múltiples
- **Solo aliases usados**: `filterLabels` declara solo `pan` y `messageType` — los dos que aparecen en condiciones
- **Simetría entre redes**: PEER01 y PEER02 tienen la misma estructura de reglas con variaciones mínimas por marca

### Estructura del código Java

- **Patrón Strategy** para mapeo ISO-20022: interfaz `SectionMappingStrategy<T>` con 10 implementaciones por sección del mensaje
- **Patrón Factory** para selección por red: `ParserFactory` y `MapperFactory` mapean `peer01→visa`, `peer02→mastercard`, `default→default`
- **Inyección por constructor** con `@RequiredArgsConstructor` de Lombok
- **Códigos de error propios** `PGWP-00XXX` en excepciones para identificación en logs
- **Métodos estáticos** en utilitarios de campo: `FieldUtil`, `VisaAxisOperator`, `MastercardAxisOperator`
- **Fallback sin excepción** en `DefaultDelegateMapper`: errores capturados devuelven un ISO20022 mínimo (ver deuda técnica)

### Antipatrones — NO hacer

1. **No agregar lógica de orquestación en Java**: toda decisión de routing va en el YML. Si necesitas agregar un microservicio a la cadena, agrégalo en `application-local.yml` como nueva función, no en código Java.

2. **No registrar interceptores de arqGw manualmente**: el `@ComponentScan("com.bbva.gateway")` los auto-descubre. Agregar `addInterceptors()` redundante puede causar registro duplicado.

3. **No capturar `StatusRuntimeException` dentro de pgwp**: ese error ocurre antes de que el código de pgwp se ejecute (en `HeadersInterceptor`). El caller de pgwp debe manejarlo.

4. **No ignorar la regla de naming de proxies**: el `channelServer` de `host` y `processor` debe contener el nombre de red y el puerto como subcadenas. Si no, kgwy no selecciona el proxy y el mensaje no llega al destino sin error explícito.

5. **No poner lógica de negocio en `IValidationsLocalErr`**: `ValidationsErrorLocal` solo debe escribir el resultado de error en `processingResult`. La lógica de validación va en `IValidationsLocal` o en las reglas del YML.

---

## 8. Prompts de trabajo

| Fase | Prompt | Genera |
|------|--------|--------|
| Contrato transitivo | `prompts/generate-transitive-contract.md` | `dependencies/.../transitive/arqGw/TRANSITIVE-CONTRACT.md` |
| Contrato kgwy | `prompts/generate-consumer-contract.md` | `dependencies/.../CONTRACT.md` |
| Análisis | `prompts/phase1-analyze.md` | Reporte (sin archivo) |
| CLAUDE.md | `prompts/phase2-create-claude.md` | `CLAUDE.md` |
| Arquitectura | `prompts/phase3-architecture.md` | `docs/architecture.md` |
| Skills | `prompts/phase4-skills.md` | `docs/claude-skills/` |

### Skills heredados de kgwy

```
dependencies/kgwy_javalib_orchestrator/claude-skills/
├── add-network-rule.md         → agregar regla a red existente en el YML
├── add-new-network.md          → agregar red nueva al YML
├── configure-filter-labels.md  → agregar/modificar aliases en filterLabels
└── troubleshoot-rules.md       → diagnosticar por qué un mensaje no se enruta
```

### Regla de actualización

Cuando hagas cambios al YML o al código Java, consulta:
```
execute updates/triggers.md
```

---

## 9. Gaps conocidos

### ⚠️ PENDIENTE DE VALIDACIÓN

1. **Flujo nextGen — condición de activación desconocida**: `OrchestratorFlowProcess.convert20022to8583` ejecuta un mapeo completo cuando `isNextGen=true` en `MonitoringDTO`. No está documentado qué microservicio de la cadena pone este flag. ¿Es MonitorService? ¿FlowHandler?

2. **Cobertura de `application-datalocal.yml` solo para PEER01**: el mapeo de códigos de respuesta (`response_code`) y campos por MTI (`map_fields_response`) existe solo para PEER01. Llamadas a `convertResponseCodeToLabelData` para PEER02 devolverán `"NO_FOUND_"+responseCode` con un `LogsTraces.writeWarning`. Confirmar si es intencional.

3. **Microservicios configurados sin uso**: `fraud`, `crypto`, `apiconnector`, `events`, `dummyprocessor`, `flowhandler`, `dialogcontrol` tienen conexiones en `application.yml` pero ninguna regla del YML los invoca. Sus variables de entorno son obligatorias en todos los ambientes. ¿Se pueden eliminar las conexiones no usadas?

### ⚠️ CONTRACT.MD DESACTUALIZADO

El `CONTRACT.md` (`dependencies/kgwy_javalib_orchestrator/CONTRACT.md`) tiene como premisa *"pgwp no tiene lógica Java propia de integración"*. Esto es incorrecto: pgwp tiene una capa completa de parsing ISO-8583 y mapping ISO-20022 (Sección 3.5 de este CLAUDE.md). El CONTRACT.md necesita actualizarse para incluir esta capa de lógica de negocio.

### ⚠️ TRANSITIVE-CONTRACT DESACTUALIZADO

Sin desactualizaciones detectadas. El TRANSITIVE-CONTRACT.md está sincronizado con el código.

### Deuda técnica (no bloqueante)

| Ítem | Archivo | Descripción |
|------|---------|-------------|
| TODOs en código de producción | `OrchestratorFlowProcess.java:48` | Traza opcional marcada para eliminar en producción |
| Fallback silencioso | `DefaultDelegateMapper.java:104-112` | Errores de mapeo devuelven ISO20022 mínimo en vez de propagar excepción |
| Valores P2P hardcodeados | `MonitoringBuilder.java:62-64` | `"Visa direct"`, `"BILLETERA"`, `"P2PP"` — marcados como TODO |
| `isSimulation` siempre `false` | `DefaultDelegateMapper.java:72` | Campo hardcodeado — marcado como TODO |
| `customDataLocal` en sensitiveData | `DefaultDelegateMapper.java:80` | Datos locales llegan a FlowHandler cuando no deberían — TODO |
| Sin deadline de request | `application.yml` | No hay deadline gRPC para `PostProcessMessage` — solo keep-alive 1200s |
