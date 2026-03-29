# Skill: add-new-network

## Cuándo usar este skill
Cuando el consumidor necesita dar soporte a una **red completamente nueva** en su
`application-local.yml` — no solo una regla adicional a una red existente, sino una
nueva entrada en `orchestrations` con su propio conjunto de reglas.

## Audiencia
Consumidores de `kgwy_javalib_orchestrator` — **no requiere tocar código Java**.

## Contexto que Claude necesita antes de ejecutar
- `docs/orchestration-rules-contract.md` — estructura del YML y comportamiento del motor
- `docs/architecture.md §4` — tabla de funciones disponibles y cómo kgwy identifica la red
- `docs/configuration-examples/application-local.yml` — referencia real de PEER01 y PEER02

---

## El patrón del proyecto

Una red en `application-local.yml` tiene esta estructura:

```yaml
local:
  orchestrations:
    - network: NOMBRE-RED       # String — sensible a case en el YAML pero comparado ignoreCase
      rules:
        - filter:
            - condition:
                - name: [alias]
                  operation: [operador]
                  value: "[valores]"
          function: [funciones]
        # ... más reglas para esta red
```

**Cómo kgwy identifica la red del mensaje entrante:**
La red NO se lee del ISO-8583 ni del ISO-20022 — se lee del header gRPC `network` que el
caller incluye en cada request:
```java
// OrchestratorService.processISO20022()
String networkName = GrpcHeadersInfo.getNetwork();  // header gRPC
```
El valor de `networkName` se compara con `orchestrations[n].network` usando `equalsIgnoreCase()`.

**Implicación crítica**: si el caller envía el header `network: peer03`, kgwy busca en
`orchestrations` una entrada con `network: PEER03` (o `peer03`, `Peer03` — insensible al case).
Si no la encuentra → `arrOrchList` vacío → sin orquestación → log `"No orchestration rules found"`.

---

## Pasos

**1. Verificar que la red no existe ya**

Buscar en `application-local.yml` si ya hay un bloque `- network: NUEVA-RED`. Si existe, usar
`add-network-rule.md` en su lugar.

**2. Identificar los tipos de mensajes que manejará la nueva red**

Consultar con el equipo qué códigos ISO 8583 puede recibir esta red. Referencia de patrones
comunes en PEER01/PEER02:

| Patrón de mensaje | Tipo | Cadena típica |
|---|---|---|
| Solicitudes iniciales al host emisor | `0100`, `0120`, `0400`, `0420` | `monitor,host` |
| Respuestas del host hacia el procesador | `0110`, `0130`, `0410`, `0430` | `updatemonitor,processor` |
| Mensajes de red — solicitud | `0800` | `processor` |
| Mensajes de red — respuesta | `0810` | `host` |
| Consulta de saldo — solicitud | `0302` | `processor` |
| Consulta de saldo — respuesta | `0312` | `host` |

**3. Determinar si la nueva red comparte `filterLabels` con las existentes**

Los `filterLabels` son compartidos por todas las redes en el mismo YML. Si la nueva red
necesita un alias adicional, hay que agregarlo al bloque `filterLabels` global del archivo.

**4. Escribir el nuevo bloque de red**

Agregar al final de la lista `orchestrations` (o en la posición que corresponda):

```yaml
- network: NUEVA-RED
  rules:
    - filter:
        - condition:
            - name: messageType
              operation: In
              value: "0100,0120,0400,0420"
      function: monitor,host
    # ... más reglas según necesidad
```

**5. Verificar el checklist de validación**

---

## Diferencias entre PEER01 y PEER02 como referencia

| Aspecto | PEER01 | PEER02 |
|---|---|---|
| Mensajes de compra/reversión (solicitud) | `0100,0120,0400,0420,0101,0401` | `0100,0120,0400,0420` |
| Mensajes de respuesta/confirmación | `0110,0130,0410,0430` | `0110,0130,0410,0430` |
| Consulta de saldo respuesta | `0312` | `0312,0190` |
| Consulta de saldo solicitud | `0302` | `0302` |
| Red de gestión solicitud | `0800` | `0800` |
| Red de gestión respuesta | `0810` | `0810` |
| Mensajes exclusivos | `0101`, `0401` ⚠️ | `0190` ⚠️ |
| Cadena para solicitudes | `monitor,host` | `monitor,host` |
| Cadena para respuestas | `updatemonitor,processor` | `updatemonitor,processor` |

> ⚠️ `0101`, `0401` (PEER01) y `0190` (PEER02) están pendientes de confirmación semántica con el equipo.

Las diferencias entre redes son **transparentes para el código Java** — el motor evalúa exactamente
el mismo código para PEER01 que para PEER02. La red solo filtra qué set de reglas aplica.

---

## Ejemplo completo

### Contexto: agregar soporte para PEER03 con flujo completo (cripto + fraude + flow)

```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value
    pan: Environment/Card/Pan
  orchestrations:
    - network: PEER01
      rules:
        # ... reglas PEER01 sin cambios
    - network: PEER02
      rules:
        # ... reglas PEER02 sin cambios
    # Nueva red — se agrega al final
    - network: PEER03
      rules:
        # Solicitudes de compra con flujo completo
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420"
          function: crypto,monitor,fraud,flowhandler,host
        # Respuestas del host hacia el procesador
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0110,0130,0410,0430"
          function: uncrypto,updatemonitor,processor
        # Mensajes de red
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
```

---

## Comportamiento cuando la red no está configurada

Si llega un mensaje cuyo header `network` no tiene entrada en `orchestrations`:

1. `filterRulesLocalsListByNetwork(network, ORCHESTRATIONS)` retorna lista vacía.
2. `getFunctionsRules()` retorna `arrOrchList = []`.
3. `OrchestratorService.processISO20022()` detecta lista vacía y loguea:
   ```
   ERROR [OrchestratorService] No orchestration rules found, no BRMS action taken
   ```
4. El ISO-20022 **no se modifica** — no se invoca ningún microservicio.
5. `IParser.convert20022to8583(iso20022)` genera la respuesta desde el ISO20022 sin procesar.
6. El consumidor recibe una respuesta ISO-8583, pero **sin los datos de los microservicios**.

> No se produce error gRPC. El consumidor debe interpretar `traceData` para detectar que
> no hubo orquestación.

---

## Checklist de validación

- [ ] El nombre de red es **único** en la lista `orchestrations` del YML
- [ ] El nombre de red coincide exactamente con el valor del header gRPC `network` que el caller enviará (insensible a mayúsculas)
- [ ] La nueva red tiene **al menos una regla** definida en `rules`
- [ ] Todas las funciones en `function` existen en la tabla de funciones disponibles (ver `docs/architecture.md §4.4`)
- [ ] Se definieron reglas para **todos los tipos de mensaje** que esta red puede recibir
- [ ] Se verificó que ninguna regla de la nueva red usa alias que no estén en `filterLabels`
- [ ] Se probó con los headers gRPC reales que el sistema enviará para esta red
- [ ] Si la nueva red necesita alias específicos, se agregaron a `filterLabels`

---

## Restricciones del motor de reglas

- **La red se identifica por el header gRPC**, no por el contenido del mensaje ISO-8583.
- **La comparación de red es `equalsIgnoreCase`**: `PEER03` == `peer03` == `Peer03`.
- **Las funciones `processor` y `host` requieren configuración de proxy**: el `channelServer` del proxy en `application.yml` debe contener el nombre de red y el puerto como subcadenas.
- **`filterLabels` es compartido entre todas las redes**: un alias definido es visible para todas.
- **Si `rules` es null o vacío** para una red configurada, el comportamiento es igual que si la red no existiera (sin orquestación, sin error).

---

## ⚠️ Antipatrones — NO hacer

### ❌ Duplicar el nombre de una red existente
```yaml
orchestrations:
  - network: PEER01
    rules: [...]
  - network: PEER01   # ← duplicado
    rules: [...]
# kgwy usa la primera entrada que coincide con equalsIgnoreCase
# La segunda nunca se evalúa
```
✅ Correcto: verificar que el nombre sea único antes de agregar.

### ❌ Crear una red sin ninguna regla
```yaml
orchestrations:
  - network: PEER03
    rules: []   # ← vacío
# Resultado: log "No orchestration rules found" para todo mensaje de PEER03
# El consumidor recibe respuestas sin orquestación, sin error explícito
```
✅ Correcto: definir al menos una regla con todas las funciones necesarias.

### ❌ Copiar reglas de otra red sin adaptar los tipos de mensaje
```yaml
# Copiar PEER01 completo para PEER03 sin revisar
- network: PEER03
  rules:
    - filter:
        - condition:
            - name: messageType
              operation: In
              value: "0100,0120,0400,0420,0101,0401"  # ← incluye 0101, 0401 de PEER01
      function: monitor,host
# ¿PEER03 maneja 0101 y 0401? Confirmar con el equipo antes de copiar
```

### ❌ Asumir que el proxy (host/processor) se selecciona por el nombre de la función
```yaml
function: monitor,host
# "host" NO significa "usa el canal llamado 'host'"
# El canal se selecciona por: channelName.contains(network) && channelName.contains(port)
# Si no hay canal configurado para PEER03, la llamada al proxy falla silenciosamente
```
✅ Correcto: asegurarse de que hay un `ChannelGrpc` configurado en `application.yml`
cuyo `channelServer` contiene el nombre de la nueva red y el puerto como subcadenas.

---

## Cómo invocar este skill con Claude Code

```
execute docs/claude-skills/add-new-network.md
```
