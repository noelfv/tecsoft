# Skill: add-network-rule

## Cuándo usar este skill
Cuando necesitas agregar una nueva regla de orquestación a una red existente (PEER01, PEER02 u otra)
en el `application-local.yml` del consumidor, sin modificar código Java de `kgwy_javalib_orchestrator`.

## Audiencia
Consumidores de `kgwy_javalib_orchestrator` — **no requiere tocar código Java**.

## Contexto que Claude necesita antes de ejecutar
- `docs/orchestration-rules-contract.md` — estructura completa del YML y semántica del motor
- `docs/architecture.md §4` — tabla de funciones disponibles
- `docs/configuration-examples/application-local.yml` — ejemplo real de referencia

---

## El patrón del proyecto

Una regla en `application-local.yml` tiene esta estructura fija:

```yaml
- filter:
    - condition:
        - name: [alias-definido-en-filterLabels]
          operation: [operador]
          value: "[valor1,valor2]"
  function: [funcion1,funcion2]
```

**Cómo la evalúa kgwy en runtime:**
1. Lee el alias `name` y lo busca en `filterLabels` para obtener la ruta real en el ISO20022.
2. Extrae el valor de ese campo por reflexión (`extractValueByMethodSequence`).
3. Aplica el `operation` comparando el valor extraído contra `value`.
4. Si **todas** las condiciones son `true` (AND), activa las funciones del campo `function`.
5. **Short-circuit**: primera regla que aplica gana — las siguientes no se evalúan.

---

## Pasos

**1. Identificar la red donde agregar la regla**

Buscar el bloque `- network: PEER01` (o la red que corresponda) en `application-local.yml`:

```yaml
local:
  orchestrations:
    - network: PEER01   # ← aquí
      rules:
        # ... reglas existentes
```

**2. Determinar la posición correcta**

Las reglas se evalúan en orden con short-circuit. Reglas más específicas deben ir **antes** que más generales.

Ejemplo: si existe una regla `In: "0100,0120,0400,0420"`, una regla para `Equals: "0100"` debe ir **antes**, o la regla general la interceptará.

**3. Escribir la nueva regla**

Agregar el nuevo bloque en la lista `rules` de la red correspondiente:

```yaml
- filter:
    - condition:
        - name: messageType
          operation: In
          value: "NUEVO1,NUEVO2"
  function: funcion1,funcion2
```

**4. Verificar el checklist de validación** (ver sección al final)

---

## Tabla de operadores disponibles

| Operador (en YML) | Cuándo usarlo | Ejemplo de `value` |
|---|---|---|
| `Equals` | Un solo valor exacto | `"0800"` |
| `NotEquals` | Todo excepto un valor | `"0800"` |
| `In` | Lista de valores posibles | `"0100,0120,0400"` |
| `NotIn` | Todo excepto esa lista | `"0110,0130"` |
| `StartWith` | El valor empieza con prefijo | `"01"` |
| `EndWith` | El valor termina con sufijo | `"00"` |
| `Greater` | Comparación numérica mayor | `"100"` |
| `Lower` | Comparación numérica menor | `"200"` |
| `Range` | Rango numérico inclusivo | `"100,200"` |

> Los YMLs de ejemplo solo usan `In` y `Equals`, pero los 9 operadores están implementados en el código.

---

## Tabla de funciones disponibles

| Función en `function` | Microservicio que invoca | Propósito resumido |
|---|---|---|
| `crypto` | CryptoService.Tokenize | Tokeniza datos de tarjeta — ejecuta siempre en posición 1 |
| `monitor` | MonitorService.PostPatchInsertDocument | Registra estado inicial — posición 2 |
| `fraud` | FraudService.GetFraudInfo | Consulta antifraude síncrona |
| `fraudasync` | FraudService.GetFraudInfoAsync | Consulta antifraude (async en FraudService, bloqueante en kgwy) |
| `dialog` | IGrpcControlDialogoClient (consumidor) | Diálogo específico del consumidor |
| `dialogcontrol` | DialogControlService.Process (arqGw) | Control de diálogo estándar |
| `apiconnector` | ApiConnectorService.SendAPIConnector | Llamada a API externa |
| `dummyprocessor` | IGrpcDummyClient (consumidor) | Procesador de prueba del consumidor |
| `events` | EventService.PostEvent | Publica evento de negocio |
| `flowhandler` | FlowHandlerService.SendResolverConnector | Manejador de flujo (síncrono) |
| `flowhandlerasync` | FlowHandlerService.SendResolverConnectorAsync | Manejador de flujo (asíncrono) |
| `feedbackfraud` | FraudService.PostFeedBackFraud | Retroalimentación al antifraude |
| `uncrypto` | CryptoService.Untokenize | Destokeniza datos de tarjeta |
| `updatemonitor` | MonitorService.PostPatchUpdateDocument | Actualiza estado final |
| `processor` | ProxyService.PostData (async fire-and-forget) | Envía ISO-8583 al procesador legacy |
| `host` | ProxyService.PostData (async fire-and-forget) | Envía ISO-8583 al host emisor |

> ⚠️ CRÍTICO: El orden del string `function` no define el orden de ejecución.
> `function: host,monitor` ejecuta igualmente `monitor` antes que `host` — el orden está fijo en el código Java.

---

## Ejemplo completo

### Contexto: agregar soporte para un nuevo tipo de mensaje en PEER01

**Antes** — PEER01 no maneja `0201`:
```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value
    pan: Environment/Card/Pan
  orchestrations:
    - network: PEER01
      rules:
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

**Después** — añadir `0201` (Authorization Advice alternativo con flujo completo):
```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value
    pan: Environment/Card/Pan
  orchestrations:
    - network: PEER01
      rules:
        # Nueva regla con flujo completo — va ANTES de la regla general de compras
        - filter:
            - condition:
                - name: messageType
                  operation: Equals
                  value: "0201"
          function: crypto,monitor,fraud,flowhandler,processor
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420,0101,0401"
          function: monitor,host
        # ... resto de reglas sin cambios
```

---

## Checklist de validación

- [ ] El alias en `name` existe en `local.filterLabels` de este mismo YML
- [ ] La operación usada es uno de los 9 operadores válidos (tabla arriba)
- [ ] Cada función en `function` existe en la tabla de funciones disponibles
- [ ] La nueva regla está en la posición correcta respecto al short-circuit:
  - Las reglas más específicas van antes que las más generales
  - Una regla `Equals: "0100"` debe ir antes de `In: "0100,0120,0400"`
- [ ] La nueva regla no intercepta mensajes que ya maneja una regla anterior
- [ ] Se probó con los tipos de mensaje que deben activar esta regla
- [ ] Se probó que los tipos de mensaje que NO deben activarla siguen cayendo en sus reglas correctas

---

## Restricciones del motor de reglas

- **Short-circuit**: primera regla que aplica gana. El orden en el YAML **es** la prioridad.
- **AND entre condiciones**: si una regla tiene múltiples `condition`, todas deben ser verdaderas.
- **Sensibilidad a mayúsculas en funciones**: `Monitor` != `monitor`. Usar siempre minúsculas.
- **El orden en `function` no define ejecución**: `function: host,monitor` ejecuta `monitor` antes que `host`.
- **Alias no definido falla silenciosamente**: si `name` no está en `filterLabels`, la condición retorna `false` sin error.
- **Función desconocida también falla silenciosamente**: si una función no existe en el mapeador, simplemente no se ejecuta sin log de advertencia.

---

## ⚠️ Antipatrones — NO hacer

### ❌ Referenciar un alias no definido en filterLabels
```yaml
# filterLabels solo tiene: messageType, pan
- condition:
    - name: transactionType   # ← NO existe en filterLabels
      operation: Equals
      value: "00"
# Resultado: condición falla silenciosamente, la regla nunca aplica
```
✅ Correcto: definir el alias antes de usarlo:
```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value
    transactionType: Transaction/TransactionType   # ← agregar aquí primero
```

### ❌ Usar una función que no existe en el mapeador
```yaml
function: monitor,miServicioCustom,host
# "miServicioCustom" no está en el mapeador — se ignora silenciosamente
# No hay error, no hay log: simplemente no se ejecuta
```
✅ Correcto: usar solo funciones de la tabla de funciones disponibles.

### ❌ Agregar la regla al final siendo más específica que una regla anterior
```yaml
rules:
  - filter:
      - condition:
          - name: messageType
            operation: In
            value: "0100,0120,0400,0420"   # ← regla general
    function: monitor,host
  - filter:
      - condition:
          - name: messageType
            operation: Equals
            value: "0100"                  # ← regla específica — NUNCA alcanzada
    function: crypto,monitor,fraud,host
```
✅ Correcto: la regla más específica va primero:
```yaml
rules:
  - filter:
      - condition:
          - name: messageType
            operation: Equals
            value: "0100"                  # ← más específica, primero
    function: crypto,monitor,fraud,host
  - filter:
      - condition:
          - name: messageType
            operation: In
            value: "0100,0120,0400,0420"   # ← más general, después
    function: monitor,host
```

### ❌ Asumir que el orden en `function` define la ejecución
```yaml
function: host,monitor
# Incorrecto pensar: "host se ejecuta antes que monitor"
# Real: monitor SIEMPRE se ejecuta antes que host (orden fijo en Java)
```

---

## Cómo invocar este skill con Claude Code

```
execute docs/claude-skills/add-network-rule.md
```
