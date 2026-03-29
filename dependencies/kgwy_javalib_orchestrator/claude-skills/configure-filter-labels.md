# Skill: configure-filter-labels

## Cuándo usar este skill
Cuando el consumidor necesita agregar, modificar o entender los **alias de campos** en
`filterLabels` de su `application-local.yml` para usarlos en las condiciones de las reglas
de orquestación.

## Audiencia
Consumidores de `kgwy_javalib_orchestrator` — **no requiere tocar código Java**.

## Contexto que Claude necesita antes de ejecutar
- `docs/orchestration-rules-contract.md §2` — semántica completa de filterLabels
- `docs/configuration-examples/application-local.yml` — aliases actuales en uso
- Conocimiento del modelo ISO-20022 — las rutas son paths de getters del objeto `ISO20022`

---

## El patrón del proyecto

### Por qué existe filterLabels

Sin `filterLabels`, el consumidor tendría que escribir la ruta completa del campo ISO-20022
directamente en cada condición:

```yaml
# Sin filterLabels — verboso y propenso a errores:
- condition:
    - name: AddendumData/AdditionalData("UNSP")/Value
      operation: In
      value: "0100,0110"
```

Con `filterLabels`, el consumidor define un alias corto una sola vez y lo reutiliza:

```yaml
# Con filterLabels — legible y reutilizable:
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value   # ← definición

# ... en las condiciones:
- condition:
    - name: messageType   # ← uso del alias
      operation: In
      value: "0100,0110"
```

### Cómo kgwy resuelve el alias en runtime

```java
// RulesCommon.java (orchlib) — getResultForCondition()
String path = filterLabels.get(conditionName);   // conditionName = "messageType"
// path = "AddendumData/AdditionalData(\"UNSP\")/Value"

if (path != null) {
    String[] parts = path.split("/");
    // parts = ["AddendumData", "AdditionalData(\"UNSP\")", "Value"]
    Object resultObject = extractValueByMethodSequence(iso20022, parts, conditionName);
    // Navega el ISO20022 por reflexión:
    //   iso20022.getAddendumData()
    //     .getAdditionalData("UNSP")
    //     .getValue()
    if (resultObject != null) {
        result = resultObject.toString();
    }
}
```

`extractValueByMethodSequence` (static import de `com.bbva.gateway.rules.RulesCommon` de arqGw)
convierte cada segmento del path separado por `/` en una llamada de método por reflexión.
Los parámetros entre paréntesis (como `"UNSP"`) se pasan como argumentos String al método.

---

## Aliases actuales en los YMLs de ejemplo

| Alias | Ruta en filterLabels | Llamada Java equivalente | Usado en reglas |
|---|---|---|---|
| `messageType` | `AddendumData/AdditionalData("UNSP")/Value` | `iso20022.getAddendumData().getAdditionalData("UNSP").getValue()` | Todas las condiciones de PEER01 y PEER02 |
| `pan` | `Environment/Card/Pan` | `iso20022.getEnvironment().getCard().getPan()` | Declarado pero sin uso en condiciones de los ejemplos ⚠️ |

> ⚠️ El alias `pan` está declarado en `filterLabels` pero no aparece en ninguna `condition`
> de los YMLs de ejemplo. Puede estar previsto para reglas de validación locales o para
> uso futuro. No eliminar sin confirmar con el equipo.

---

## Pasos para agregar un nuevo alias

**1. Identificar la ruta en el modelo ISO-20022**

La ruta sigue el patrón de getters del objeto `ISO20022` de arqGw, separados por `/`:
- `Environment/Card/Pan` → `iso20022.getEnvironment().getCard().getPan()`
- `Transaction/TransactionType` → `iso20022.getTransaction().getTransactionType()`
- `AddendumData/AdditionalData("CLAVE")/Value` → `iso20022.getAddendumData().getAdditionalData("CLAVE").getValue()`

Para métodos con parámetros String: escribir el argumento entre paréntesis en el path.

**2. Definir el alias en `filterLabels`**

```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value   # existente
    pan: Environment/Card/Pan                                 # existente
    nuevoAlias: Ruta/Al/Campo/En/ISO20022                    # ← nuevo
```

El alias puede ser cualquier String alfanumérico sin espacios. Por convención, usar camelCase.

**3. Usar el alias en las condiciones**

```yaml
- filter:
    - condition:
        - name: nuevoAlias   # ← usar el alias definido
          operation: Equals
          value: "valorEsperado"
  function: monitor,host
```

**4. Verificar el checklist de validación**

---

## Ejemplo completo

### Contexto: agregar discriminación por tipo de transacción además del tipo de mensaje

**Antes** — solo se discrimina por `messageType`:
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
```

**Después** — agregar `transactionType` para diferenciar compras de retiros:
```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value
    pan: Environment/Card/Pan
    transactionType: Transaction/TransactionType            # ← nuevo alias
  orchestrations:
    - network: PEER01
      rules:
        # Compras (transactionType=00) con flujo de fraude
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420"
                - name: transactionType   # ← usando el nuevo alias
                  operation: Equals
                  value: "00"
          function: crypto,monitor,fraud,host
        # Retiros y adelantos en efectivo (otros transactionTypes)
        - filter:
            - condition:
                - name: messageType
                  operation: In
                  value: "0100,0120,0400,0420"
          function: monitor,host
        # ... resto de reglas sin cambios
```

> Nota: la regla con `transactionType=00` va **antes** que la regla general de `0100,0120,0400,0420`
> por el short-circuit — de lo contrario la regla general la interceptaría primero.

---

## Qué pasa si se usa un alias no definido en filterLabels

**Comportamiento certero (del código):**

```java
String path = filterLabels.get("aliasNoDefinido");   // → null
if (path != null) { ... }                             // bloque no ejecutado
// conditionResults almacena null para "aliasNoDefinido"
// evaluateCondition recibe null como filterLabelProcess
// Operations.in(null, "valor") → false
```

**Resultado**: la condición falla silenciosamente → la regla completa falla → se evalúa la
siguiente regla. **No se lanza excepción. No hay log de advertencia.** El mensaje puede caer
en una regla incorrecta o en ninguna.

---

## Formato válido de las rutas en filterLabels

| Patrón | Ejemplo | Nota |
|---|---|---|
| Path simple | `Transaction/TransactionType` | Getters sin parámetros |
| Path con clave String | `AddendumData/AdditionalData("UNSP")/Value` | El argumento va entre comillas dobles dentro del paréntesis |
| Path con índice numérico | ⚠️ PENDIENTE DE VALIDACIÓN | No confirmado si `extractValueByMethodSequence` soporta índices numéricos |
| Path de un solo nivel | `NetworkName` | Solo un getter desde la raíz del ISO20022 |

---

## Checklist de validación

- [ ] El alias es **único** en `filterLabels` — no duplica ningún alias existente
- [ ] La ruta en el valor del alias corresponde a getters reales del modelo `ISO20022` de arqGw
- [ ] Para métodos con parámetros: el argumento está entre paréntesis y comillas dobles en el path
- [ ] El alias se usa correctamente en `condition.name` en alguna regla
- [ ] Las reglas que usan el nuevo alias están en la posición correcta considerando el short-circuit
- [ ] Se verificó que el campo no retorna `null` para los mensajes que deben usar esta regla
- [ ] No se eliminaron aliases existentes sin verificar que ninguna regla los referencia

---

## Restricciones del motor de reglas

- **`filterLabels` es compartido entre todas las redes** del YML — un alias definido es visible para PEER01, PEER02 y cualquier red nueva.
- **El scope del `filterLabels`**: los labels definidos en `local.filterLabels` están disponibles en las reglas de `local.orchestrations` y `local.validations`. Son independientes de `global.filterLabels`.
- **La extracción es por reflexión**: si el getter no existe o retorna null, la condición falla silenciosamente.
- **Sensibilidad a mayúsculas en el alias**: `messageType` ≠ `MessageType` ≠ `MESSAGETYPE`. La comparación en `filterLabels.get()` es case-sensitive.

---

## ⚠️ Antipatrones — NO hacer

### ❌ Usar la ruta completa directamente en `condition.name` en lugar de definir un alias
```yaml
# INCORRECTO — ruta completa en name
- condition:
    - name: AddendumData/AdditionalData("UNSP")/Value
      operation: In
      value: "0100,0110"
# kgwy busca ese string exacto en filterLabels → null → condición falla silenciosamente
```
✅ Correcto: definir el alias en `filterLabels` y usarlo en `name`.

### ❌ Usar el mismo alias para rutas diferentes en distintas partes del YML
```yaml
local:
  filterLabels:
    messageType: AddendumData/AdditionalData("UNSP")/Value
# No hay forma de redefinir messageType para que apunte a otro campo
# filterLabels es un Map<String,String> — una clave, un valor
```

### ❌ Referenciar un alias de `global.filterLabels` en las reglas `local.orchestrations`
```yaml
# application-global.yml tiene:
global:
  filterLabels:
    networkType: Transaction/NetworkType

# application-local.yml — INCORRECTO:
- condition:
    - name: networkType   # ← alias definido en global, no en local.filterLabels
      operation: Equals
      value: "VISA"
# RulesCommon usa filterLabelsLocal para las reglas locales
# Si networkType no está en local.filterLabels → null → condición falla silenciosamente
```
✅ Correcto: si necesitas el mismo alias en reglas locales, definirlo también en `local.filterLabels`.

### ❌ Hardcodear la ruta con formato incorrecto
```yaml
local:
  filterLabels:
    # INCORRECTO — punto como separador
    messageType: AddendumData.AdditionalData.Value
    # CORRECTO — slash como separador
    messageType: AddendumData/AdditionalData("UNSP")/Value
```

---

## Cómo invocar este skill con Claude Code

```
execute docs/claude-skills/configure-filter-labels.md
```
