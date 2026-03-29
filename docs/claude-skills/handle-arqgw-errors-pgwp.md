# Skill: handle-arqgw-errors-pgwp.md
# Manejo de errores de arqGw en pgwp_java_orchestrator

## Diferencia con el skill heredado

El skill `handle-arqgw-errors.md` de kgwy documenta las excepciones de arqGw en
general. Este skill documenta **cómo pgwp_java_orchestrator específicamente maneja
esos errores** — con el código real de pgwp como referencia, incluyendo los patrones
de fallback existentes y cuándo agregar nuevo manejo.

---

## Cuándo usar este skill

- Al investigar por qué un error de mapeo ISO-20022 no se propaga al caller
- Al agregar manejo de una nueva excepción propia de pgwp en el mapper
- Al entender qué errores de arqGw puede recibir pgwp vs. cuáles recibe el caller
- Al modificar el comportamiento de fallback del mapper

---

## Situación actual: cómo pgwp maneja los errores

### Principio fundamental

**pgwp NO captura excepciones de `com.bbva.gateway.*` (arqGw) directamente.**

Los `catch` de pgwp son sobre excepciones propias o de Java estándar.
La documentación completa está en:
```
dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md §5
```

### Mapa de errores por origen

| Origen del error | Excepción | Quién la captura | Qué ocurre |
|-----------------|-----------|-----------------|------------|
| Header gRPC faltante (`traceid`/`spanid`/`network`/`port`) | `StatusRuntimeException(INVALID_ARGUMENT)` de arqGw | El **caller** de pgwp — pgwp no llega a ejecutarse | Arqgw rechaza el request antes de que pgwp actúe |
| Error de microservicio en cadena (`MonitorService`, `ProxyService`) | `InternalServerException` de arqGw | kgwy — lo serializa en `traceData` del ISO20022 | pgwp recibe el ISO20022 con error en traceData, sin excepción |
| Error en estrategia de mapeo ISO-20022 | `MapperFieldsException` de pgwp | `DefaultDelegateMapper` | Fallback silencioso — ver abajo |
| Error no contemplado en el mapper | `Exception` de Java | `DefaultDelegateMapper` | Fallback silencioso — ver abajo |
| Error al construir MonitoringDTO | `RuntimeException` de Java | `MonitoringBuilder` | Retorna monitoring parcial — ver abajo |

---

## Código real de manejo de errores en pgwp

### 1. DefaultDelegateMapper — patrón catch actual

```java
// src/main/java/com/bbva/orchestrator/core/mapper/factory/impl/DefaultDelegateMapper.java
@Override
public ISO20022 mapper(ISO8583 input, Map<String, String> subFields) {
    try {
        // ... lógica de mapeo con 10 estrategias ...
        return iso20022Builder.build();

    } catch (MapperFieldsException e) {
        // MapperFieldsException es propia de pgwp (com.bbva.orchestrator.core.exception)
        LogsTraces.writeWarning(e.getCode() + " " + e.getDescription() + " " + e.getCause());
        return buildFallbackResponse(input); // ⚠️ Fallback silencioso — deuda técnica
    } catch (Exception e) {
        // Exception genérica — captura cualquier error no contemplado
        LogsTraces.writeWarning("PGWP-00121 - ExceptionError al mapear desde ISO8583: " + e);
        return buildFallbackResponse(input); // ⚠️ Fallback silencioso — deuda técnica
    }
}
```

**El fallback que se retorna:**

```java
// DefaultDelegateMapper.java — buildFallbackResponse()
private ISO20022 buildFallbackResponse(ISO8583 input) {
    return ISO20022.builder()
        .networkName(input.getNetworkName())
        .messageFunction(MessageFunction.convertMessageFunction(input.getMessageType()))
        .socketPort(GrpcHeadersInfo.getPort())
        .transaction(TransactionDTO.builder()
            .transactionId(TransactionIdDTO.builder()
                .transactionReference("fallback-%s".formatted(UUID.randomUUID()))
                .build())
            .build())
        .environment(EnvironmentDTO.builder()
            .card(CardDTO.builder()
                .pan(input.getPrimaryAccountNumber())
                .build())
            .build())
        .addendumData(AddendumDataDTO.builder()
            .additionalData(List.of(
                AdditionalDataDTO.builder()
                    .key("ISO8583_HOST").value(input.getOriginalMessage()).build(),
                AdditionalDataDTO.builder()
                    .key("UNSP").value(input.getMessageType()).build()
            ))
            .build())
        .monitoring(MonitoringDTO.builder().isNextGen(Boolean.FALSE).build())
        .build();
}
```

> ⚠️ El fallback siempre pone `isNextGen=false` — esto fuerza el flujo passthrough
> en `convert20022to8583()`, que retorna el ISO8583 original del host sin mapeo.
> Si el error ocurrió en la petición (no en la respuesta), el mensaje ISO8583 original
> puede no estar disponible en `addendumData["ISO8583_HOST"]`.

### 2. MonitoringBuilder — patrón catch actual

```java
// src/main/java/com/bbva/orchestrator/core/builders/MonitoringBuilder.java
public MonitoringDTO build(ISO8583 input, ...) {
    try {
        // ... construye MonitoringDTO con BINs, MCC, P2P, etc. ...
        return monitoringDTO;
    } catch (RuntimeException e) {
        LogsTraces.writeError("Error creating monitoring: " + e.getMessage());
        return monitoring; // retorna el monitoring parcialmente construido
    }
}
```

---

## Excepciones propias de pgwp (no de arqGw)

Estas son las excepciones que pgwp lanza y pueden llegar al catch del mapper:

| Excepción | Package | Cuándo se lanza | Código de error |
|-----------|---------|----------------|----------------|
| `MapperFieldsException` | `com.bbva.orchestrator.core.exception` | Error en estrategia de mapeo ISO-20022 | `PGWP-00121` y otros `PGWP-001XX` |
| `ParserFieldsException` | `com.bbva.orchestrator.core.exception` | Error al procesar subcampos TLV del Campo 48 | `PGWP-00140` |
| `LogicFieldsException` | `com.bbva.orchestrator.core.exception` | Error en lógica de operadores de red | — |
| `MandatoryFieldsException` | `com.bbva.orchestrator.core.exception` | Campo obligatorio ausente en el ISO-8583 | — |

---

## Patrón para agregar manejo de una nueva excepción propia de pgwp

Si una nueva estrategia de mapeo lanza una nueva excepción propia de pgwp,
el patrón a seguir basado en el código real es:

### Paso 1 — Crear la excepción si es nueva

```java
// src/main/java/com/bbva/orchestrator/core/exception/NuevaException.java
package com.bbva.orchestrator.core.exception;

public class NuevaException extends RuntimeException {
    private final String code;
    private final String description;

    public NuevaException(String code, String description, Throwable cause) {
        super(description, cause);
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
```

### Paso 2 — Agregar el catch en DefaultDelegateMapper (antes del catch genérico)

```java
// DefaultDelegateMapper.java — agregar ANTES del catch(Exception e)
} catch (NuevaException e) {
    LogsTraces.writeWarning(e.getCode() + " " + e.getDescription() + " " + e.getCause());
    return buildFallbackResponse(input);
} catch (MapperFieldsException e) {
    // ... catch existente sin cambios ...
```

> **Regla de orden**: los catches específicos van ANTES del catch genérico `Exception`.
> El catch de `Exception` debe ser siempre el último.

### Paso 3 — Si la nueva excepción viene de arqGw (caso infrecuente)

Si kgwy_javalib_orchestrator expone una nueva excepción de arqGw que antes no llegaba a pgwp:

1. Verificar que está documentada en:
   `dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md §5`
   Si no está → **actualizar el TRANSITIVE-CONTRACT.md primero**.

2. Agregar el catch en el método correspondiente siguiendo el patrón:
   ```java
   } catch (com.bbva.gateway.exception.NuevaExcepcionArqGw e) {
       LogsTraces.writeWarning("PGWP-00XXX - " + e.getMessage());
       return buildFallbackResponse(input);
   ```

3. Actualizar el TRANSITIVE-CONTRACT.md §5 con la nueva excepción capturada.

---

## StatusRuntimeException — qué NO debe hacer pgwp

`StatusRuntimeException` es la excepción gRPC que arqGw lanza cuando faltan headers.
**pgwp NO debe capturarla internamente** porque:

1. `HeadersInterceptor` la lanza ANTES de que el código de pgwp se ejecute
2. El request ni siquiera llega a `IParser.convert8583to20022()`
3. El caller de pgwp es quien debe manejarla

```java
// ❌ INCORRECTO — agregar en pgwp:
} catch (io.grpc.StatusRuntimeException e) {
    // Esto nunca se ejecutará — el request fue rechazado antes
}

// ✅ CORRECTO — el caller de pgwp maneja StatusRuntimeException
// No agregar catch de StatusRuntimeException en el código de pgwp
```

---

## Checklist de validación

Al agregar o modificar manejo de errores:

- [ ] La excepción a capturar está en el package `com.bbva.orchestrator.core.exception`
  (excepción propia de pgwp) o documentada en `TRANSITIVE-CONTRACT.md §5` (arqGw)
- [ ] El nuevo catch va **antes** del `catch(Exception e)` genérico
- [ ] Se usa `LogsTraces.writeWarning()` o `LogsTraces.writeError()` para registrar
  el error (no `System.out` ni Logger de Java)
- [ ] Si el método retorna `ISO20022`, se retorna `buildFallbackResponse(input)`
  como fallback (no `null`)
- [ ] Si la excepción viene de arqGw, el `TRANSITIVE-CONTRACT.md §5` fue actualizado
- [ ] El código de error sigue la convención `PGWP-00XXX`

---

## Antipatrones

- **Capturar `Exception` genérica para simplificar**: ya existe un catch genérico
  en `DefaultDelegateMapper`. Agregar uno nuevo oculta errores específicos que podrían
  requerir tratamiento distinto.

- **Retornar `null` en lugar de fallback**: kgwy/arqGw recibirán `null` como ISO20022
  y lanzarán `NullPointerException` en la cadena. Siempre retornar `buildFallbackResponse()`.

- **Capturar `StatusRuntimeException` dentro de pgwp**: ese error ocurre en el interceptor
  de arqGw antes de que pgwp actúe. El catch nunca se ejecutará.

- **No actualizar TRANSITIVE-CONTRACT.md**: si se agrega captura de una excepción
  de `com.bbva.gateway.*`, el contrato transitivo queda desactualizado.

- **Usar `LogsTraces.writeInfo()` para errores**: los errores y advertencias deben
  ir a `writeWarning()` o `writeError()` según su severidad — nunca a `writeInfo()`.

---

## Referencias

- `src/main/java/com/bbva/orchestrator/core/mapper/factory/impl/DefaultDelegateMapper.java` — código real del mapper con los catches actuales
- `src/main/java/com/bbva/orchestrator/core/builders/MonitoringBuilder.java` — catch de RuntimeException
- `dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md §5` — excepciones de arqGw que llegan a pgwp
- `CLAUDE.md §4` — reglas para interpretar com.bbva.gateway.* en pgwp
- `docs/architecture.md §7` — tabla completa de manejo de errores
