# Skill: setup-new-environment.md
# Configurar pgwp_java_orchestrator en un nuevo ambiente

## Por qué existe este skill

Los skills heredados de kgwy_javalib_orchestrator cubren agregar reglas o redes individuales.
Este skill cubre la **configuración completa desde cero** de un nuevo ambiente de
pgwp_java_orchestrator — incluyendo todas las properties técnicas de gRPC, la estructura
del grupo de perfiles Spring y los datos de negocio.

No hay un skill equivalente en kgwy_javalib_orchestrator porque kgwy no configura ambientes
de despliegue — pgwp sí.

---

## Cuándo usar este skill

- Al desplegar pgwp_java_orchestrator en un nuevo ambiente (nuevo data center, nueva región)
- Al crear un nuevo perfil Spring para un ambiente específico de BBVA Perú
- Al incorporar un nuevo integrante al equipo que necesita levantar el servicio localmente
- Al revisar que la configuración de un ambiente existente está completa

---

## Estructura de la configuración de pgwp

pgwp usa un **grupo de perfiles Spring** que carga todos los YMLs en un solo arranque:

```yaml
# application.yml
spring:
  profiles:
    active: @spring.profiles.active@     # inyectado por Maven en el build
    group:
      gw: global, local, data, datalocal, validations, sensitivedata
```

El perfil `gw` activa los siguientes YMLs simultáneamente:

| Archivo | Qué configura | Bean de kgwy que lo lee |
|---------|--------------|------------------------|
| `application-global.yml` | `global.validations` | `RulesGlobalLoad` |
| `application-local.yml` | `local.orchestrations`, `local.filterLabels`, `local.validations` | `RulesLocalLoad` |
| `application-data.yml` | `data.currency`, `data.bins`, `data.custom` | `BusinessDataLoad` |
| `application-datalocal.yml` | `datalocal[].response_code`, `datalocal[].map_fields_response` | `BusinessDataLocalLoad` |
| `application-validations.yml` | validaciones adicionales | — |
| `application-sensitivedata.yml` | datos sensibles | — |

---

## Template completo por archivo

### 1. application.yml — configuración técnica base (ya existe, no duplicar)

```yaml
spring:
  profiles:
    active: @spring.profiles.active@
    group:
      gw: global, local, data, datalocal, validations, sensitivedata

grpc:
  server:
    address: ${GRPC_SERVER_ADDRESS}
    port: ${GRPC_SERVER_PORT}
    enable-keep-alive: true
    permit-keep-alive-without-calls: true
    keep-alive-time: 1200s
    keep-alive-timeout: 60s
    permit-keep-alive-time: 30s
  client:
    GLOBAL:
      enable-keep-alive: true
      permit-keep-alive-without-calls: true
      keep-alive-time: 1200s
      keep-alive-timeout: 60s
      permit-keep-alive-time: 30s
      services:
        # ---- Microservicios USADOS en las reglas YML de pgwp ----
        monitor:
          channel-server: ${ORCHESTRATION_MONITOR_ADDRESS}
          channel-port: ${ORCHESTRATION_MONITOR_PORT}
        # ---- Proxies — channelServer DEBE contener red y puerto como subcadenas ----
        processor:
          - channel-server: ${ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003}   # debe contener "PEER01" y "7003"
            channel-port: ${ORCHESTRATION_PROXYPROCESSOR_PORT_7003}
          - channel-server: ${ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234}   # debe contener "PEER02" y "1234"
            channel-port: ${ORCHESTRATION_PROXYPROCESSOR_PORT_1234}
        host:
          - channel-server: ${ORCHESTRATION_PROXYHOST_ADDRESS_7003}        # debe contener "PEER01" y "7003"
            channel-port: ${ORCHESTRATION_PROXYHOST_PORT_7003}
          - channel-server: ${ORCHESTRATION_PROXYHOST_ADDRESS_1234}        # debe contener "PEER02" y "1234"
            channel-port: ${ORCHESTRATION_PROXYHOST_PORT_1234}
        # ---- Microservicios configurados pero sin uso en reglas YML actuales ----
        flowhandler:
          channel-server: ${ORCHESTRATION_FLOWHANDLER_ADDRESS}
          channel-port: ${ORCHESTRATION_FLOWHANDLER_PORT}
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

server:
  port: 8083
```

### 2. application-local.yml — reglas de orquestación (redes REALES de pgwp)

```yaml
# Reglas de orquestación de pgwp_java_orchestrator
# IMPORTANTE: estas son las redes reales — no copiar ejemplos de kgwy
local:
  filterLabels:
    # Los dos aliases que pgwp usa en sus condiciones de reglas
    pan: Environment/Card/Pan
    messageType: AddendumData/AdditionalData("UNSP")/Value

  orchestrations:
    # ---- PEER01: Red Visa ----
    - network: PEER01
      rules:
        # Compras y reversals entrantes → MonitorService + ProxyService/host
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420,0101,0401"
          function: monitor,host
        # Respuestas de compras y reversals → MonitorService/update + ProxyService/processor
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0110,0130,0410,0430"
          function: updatemonitor,processor
        # Pass-through Visa → solo host
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0312"
          function: host
        # Pass-through Visa → solo processor
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0302"
          function: processor
        # Mensajes de red → processor
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0800"
          function: processor
        # Mensajes de red → host
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0810"
          function: host

    # ---- PEER02: Red Mastercard ----
    - network: PEER02
      rules:
        # Compras y reversals entrantes (sin 0101/0401 — no aplican a Mastercard)
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
        # Pass-through Mastercard (incluye 0190 que PEER01 no tiene)
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
    # pgwp_java_orchestrator delega toda validación a kgwy/arqGw — dejar vacío
```

### 3. application-global.yml

```yaml
global:
  validations:
    # pgwp_java_orchestrator no tiene validaciones globales propias — dejar vacío
```

### 4. application-data.yml — datos de negocio (solo referencia de estructura)

```yaml
data:
  currency:
    - numericCode: "840"
      alphabeticCode: USD
    # [120 monedas totales — copiar desde el application-data.yml existente]

  bins:
    - key: PEER01
      value:
        - bin: "[6 dígitos]"
          description: "[descripción del producto Visa]"
        # [36+ BINs de Visa]
    - key: PEER02
      value:
        - bin: "[6 dígitos]"
          description: "[descripción del producto Mastercard]"
        # [8 BINs de Mastercard]

  custom:
    bank_p2p:
      - code: "[código banco]"
        name: "[nombre banco]"
      # [20+ entidades bancarias]
    merchant_type:
      - code: "[MCC]"
        description: "[descripción]"
      # [130+ códigos MCC]
```

### 5. application-datalocal.yml — códigos de respuesta por red

```yaml
datalocal:
  - network: "PEER01"
    custom:
      map_fields_response:
        # Campos mandatorios (M) y opcionales (O) por MTI de respuesta
        0110:
          - field: "[número de campo ISO-8583]"
            mandatory: true/false
        0410:
          - field: "[número de campo ISO-8583]"
            mandatory: true/false
      response_code:
        # Mapeo label semántico → código ISO-8583 de respuesta
        "[LABEL_SEMANTICO]": "[código numérico]"
  # ⚠️ PEER02 actualmente no tiene entrada — códigos Mastercard devuelven "NO_FOUND_+código"
  # Agregar si se requiere soporte completo de respuestas Mastercard:
  # - network: "PEER02"
  #   custom:
  #     response_code: ...
```

---

## Variables de entorno requeridas en startup

Todas son obligatorias — la ausencia de cualquiera causa error en startup.

### Variables del servidor gRPC propio de pgwp

| Variable | Ejemplo | Descripción |
|----------|---------|-------------|
| `GRPC_SERVER_ADDRESS` | `0.0.0.0` | Dirección de escucha |
| `GRPC_SERVER_PORT` | `9090` | Puerto de escucha gRPC |

### Variables de los microservicios de la cadena

| Variable | Microservicio | Usado en reglas YML |
|----------|--------------|---------------------|
| `ORCHESTRATION_MONITOR_ADDRESS` | MonitorService | Sí — `monitor`, `updatemonitor` |
| `ORCHESTRATION_MONITOR_PORT` | MonitorService | Sí |
| `ORCHESTRATION_PROXYHOST_ADDRESS_7003` | ProxyService/host PEER01 | Sí — `host` |
| `ORCHESTRATION_PROXYHOST_PORT_7003` | ProxyService/host PEER01 | Sí |
| `ORCHESTRATION_PROXYHOST_ADDRESS_1234` | ProxyService/host PEER02 | Sí — `host` |
| `ORCHESTRATION_PROXYHOST_PORT_1234` | ProxyService/host PEER02 | Sí |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_7003` | ProxyService/processor PEER01 | Sí — `processor` |
| `ORCHESTRATION_PROXYPROCESSOR_PORT_7003` | ProxyService/processor PEER01 | Sí |
| `ORCHESTRATION_PROXYPROCESSOR_ADDRESS_1234` | ProxyService/processor PEER02 | Sí — `processor` |
| `ORCHESTRATION_PROXYPROCESSOR_PORT_1234` | ProxyService/processor PEER02 | Sí |
| `ORCHESTRATION_FLOWHANDLER_ADDRESS` | FlowHandlerService | No — sin uso en reglas |
| `ORCHESTRATION_FLOWHANDLER_PORT` | FlowHandlerService | No |
| `ORCHESTRATION_FRAUD_ADDRESS` | FraudService | No |
| `ORCHESTRATION_FRAUD_PORT` | FraudService | No |
| `ORCHESTRATION_CRYPTO_ADDRESS` | CryptoService | No |
| `ORCHESTRATION_CRYPTO_PORT` | CryptoService | No |
| `ORCHESTRATION_APICONNECTOR_ADDRESS` | ApiConnectorService | No |
| `ORCHESTRATION_APICONNECTOR_PORT` | ApiConnectorService | No |
| `ORCHESTRATION_DIALOG_ADDRESS` | DialogControlService | No |
| `ORCHESTRATION_DIALOG_PORT` | DialogControlService | No |
| `ORCHESTRATION_EVENTS_ADDRESS` | EventService | No |
| `ORCHESTRATION_EVENTS_PORT` | EventService | No |
| `ORCHESTRATION_DUMMYPROCESSOR_ADDRESS` | DummyProcessorService | No |
| `ORCHESTRATION_DUMMYPROCESSOR_PORT` | DummyProcessorService | No |

### Variables de keep-alive de proxies (requeridas por kgwy — sin fallback)

| Variable | Tipo | Descripción |
|----------|------|-------------|
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIME` | Long (segundos) | Keep-alive de canales proxy |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_TIMEOUT` | Long (segundos) | Timeout de keep-alive proxy |
| `ORCHESTRATION_PROXY_GRPC_CLIENT_KEEP_ALIVE_WITHOUT_CALLS` | `"true"` / `"false"` | Keep-alive sin llamadas |

> ⚠️ Las 3 variables de keep-alive de proxy **no tienen fallback en kgwy**.
> Su ausencia causa `NullPointerException` o `NumberFormatException` en startup.

---

## Regla crítica para los valores de channelServer de los proxies

kgwy selecciona el proxy por red evaluando:

```
channelServer.contains(networkName) && channelServer.contains(port)
```

Los valores de `ORCHESTRATION_PROXYHOST_ADDRESS_7003` y similares **deben contener
el nombre de la red (`PEER01`/`PEER02`) y el puerto (`7003`/`1234`) como subcadenas**.

Ejemplo correcto:
```
ORCHESTRATION_PROXYHOST_ADDRESS_7003=proxy-PEER01-7003.bbva.com
ORCHESTRATION_PROXYHOST_ADDRESS_1234=proxy-PEER02-1234.bbva.com
```

---

## Checklist de validación del nuevo ambiente

### Antes de iniciar

- [ ] Todas las variables de entorno del servidor gRPC de pgwp definidas
- [ ] Todas las variables de los microservicios de la cadena definidas
- [ ] Las 3 variables de keep-alive de proxy definidas (NullPointerException en startup si faltan)
- [ ] Los valores de `channelServer` de proxies contienen red y puerto como subcadenas

### Verificar la configuración YML

- [ ] `filterLabels` tiene `pan` y `messageType` con las rutas correctas:
  ```yaml
  pan: Environment/Card/Pan
  messageType: AddendumData/AdditionalData("UNSP")/Value
  ```
- [ ] `orchestrations` tiene entradas para `PEER01` y `PEER02`
- [ ] Cada red tiene exactamente 6 reglas en el orden correcto
- [ ] Las funciones usadas (`monitor`, `host`, `processor`, `updatemonitor`) están disponibles en kgwy
  → verificar en `dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md`
- [ ] `validations:` está vacío (pgwp no usa validaciones locales propias)
- [ ] `data.bins` tiene entradas para `PEER01` y `PEER02`

### Al arrancar el servicio

- [ ] La aplicación inicia sin errores de binding de properties
- [ ] Health endpoint responde: `GET http://localhost:8083/actuator/health`
- [ ] gRPC server escucha en el puerto configurado

### Al recibir el primer mensaje

- [ ] Un mensaje `0100 PEER01` llega a MonitorService (función `monitor,host`)
- [ ] Un mensaje `0110 PEER01` llega a ProxyService/processor (función `updatemonitor,processor`)
- [ ] Si los logs muestran `"NO_FOUND_"` en códigos de respuesta PEER02, confirmar
  si `application-datalocal.yml` necesita entrada para Mastercard

---

## Antipatrones

- **Copiar application-local.yml de kgwy_javalib_orchestrator**: los ejemplos de kgwy
  usan redes genéricas (`PEER01_EXAMPLE`). Las redes reales de pgwp son `PEER01` y `PEER02`.

- **Usar funciones no disponibles en kgwy**: pgwp solo usa `monitor`, `updatemonitor`,
  `host`, `processor`. Agregar funciones como `crypto` o `fraud` sin validar en
  `orchestratorlib-contract.md` causa error en startup.

- **No definir las 3 variables de keep-alive de proxy**: a diferencia de otras variables
  que tienen fallback, estas tres causan excepción en startup de kgwy.

- **Valores de channelServer de proxy sin el nombre de red y puerto**: kgwy selecciona
  el proxy por `contains(network) && contains(port)`. Si el hostname no cumple esto,
  el mensaje llega a todos los proxies o a ninguno sin error explícito.

- **Omitir application-global.yml**: aunque esté vacío, kgwy necesita que
  `global.validations` esté definido. Sin este archivo puede fallar el binding.

---

## Referencias

- `dependencies/kgwy_javalib_orchestrator/CONTRACT.md` — §5 para variables de entorno
- `dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md` — lista de funciones disponibles
- `docs/configuration-examples/application-local.yml` — reglas reales de pgwp como referencia
- `CLAUDE.md §6` — configuración completa con todas las variables de entorno
