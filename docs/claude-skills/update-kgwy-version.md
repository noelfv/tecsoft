# Skill: update-kgwy-version.md
# Actualizar la versión de kgwy_javalib_orchestrator en pgwp_java_orchestrator

## Cuándo usar este skill

Cuando kgwy_javalib_orchestrator publica una nueva versión de `orchestratorlib` y
pgwp_java_orchestrator debe actualizar su integración.

> Este proceso puede impactar la integración con arqGw transitivamente.
> Seguir los 6 pasos **en orden** — no actualizar el `pom.xml` antes de revisar
> los contratos, o puede quedar una versión rota en producción.

---

## Contexto: qué puede cambiar en una nueva versión de kgwy

| Capa | Qué puede cambiar | Impacto en pgwp |
|------|------------------|----------------|
| `orchestratorlib` (kgwy directo) | Estructura del YML esperado, nuevas/eliminadas funciones, nuevas interfaces requeridas | Potencialmente alto |
| `arqGw` (transitivo vía kgwy) | API de `LogsTraces`, `GrpcHeadersInfo`, estructura de `ISO20022` + DTOs | Potencialmente alto |
| Solo internos de kgwy/arqGw | Comportamiento interno sin cambio de API pública | Ninguno |

---

## Paso 1 — Revisar el orchestratorlib-contract.md de la nueva versión

Solicitar al equipo de kgwy_javalib_orchestrator el nuevo `orchestratorlib-contract.md`
y compararlo con el que pgwp tiene actualmente en:

```
dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md
```

**Preguntas clave al comparar:**

### Sobre la estructura del YML

```
¿Cambió la estructura de local.orchestrations?
¿Cambió la estructura de local.filterLabels?
¿Cambió la estructura de application-data.yml?
¿Cambió la estructura de application-datalocal.yml?
```
→ Si sí: identificar qué archivos YML de pgwp deben actualizarse.

### Sobre las funciones disponibles

```
¿Se eliminó alguna de las funciones que pgwp usa?
  Funciones actualmente usadas: monitor, updatemonitor, host, processor
```
→ Si sí: **CRÍTICO** — revisar todas las reglas de `application-local.yml`.
Una función eliminada causa error en startup o en la evaluación de reglas.

```
¿Se agregaron nuevas funciones?
```
→ Opcional — pgwp puede usarlas si conviene para nuevos requisitos.

### Sobre los puertos requeridos

```
¿Se agregaron nuevas interfaces que pgwp debe implementar?
  Interfaces actuales de pgwp: IParser, IGrpcControlDialogoClient, IGrpcDummyClient,
  IValidationsLocal, IValidationsLocalErr
```
→ Si sí: crear nueva clase en pgwp que implemente la interfaz.

```
¿Se eliminó alguna interfaz que pgwp implementa?
```
→ Si sí: eliminar la clase de pgwp correspondiente.

### Sobre el timeout recomendado

```
¿Cambió el timeout mínimo recomendado para el consumidor?
  Timeout actual en pgwp: keep-alive 1200s (sin deadline de request explícito)
```
→ Si sí: ajustar la configuración de timeout en `application.yml`.

---

## Paso 2 — Revisar el TRANSITIVE-CONTRACT.md por cambios en arqGw

Si la nueva versión de kgwy actualiza también su versión de arqGw, revisar:

```
dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md
```

**Preguntas clave:**

### Sobre LogsTraces (11 archivos de pgwp afectados)

```
¿Cambió la firma de writeInfo() / writeWarning() / writeError()?
```
→ Si sí: buscar todos los usos en pgwp:
```
Grep: LogsTraces.write en src/main/java/
Archivos afectados: OrchestratorFlowProcess, DefaultDelegateMapper,
MonitoringBuilder, MapperUtil, FieldUtil, VisaProcessField,
MastercardProcessField, CompositeVariableFieldParser,
CompositeTlvFieldParser, CompositeFixedFieldParser, MastercardDelegateFieldLogic
```

### Sobre GrpcHeadersInfo (6 archivos de pgwp afectados)

```
¿Cambió el nombre de getNetwork() / getPort() / getTraceId()?
```
→ Si sí: buscar todos los usos en pgwp:
```
Grep: GrpcHeadersInfo.get en src/main/java/
Archivos afectados: OrchestratorFlowProcess, MapperFactory, ParserFactory,
DefaultDelegateMapper, TraceDataMappingStrategy, DefaultDelegateParser
```

### Sobre el modelo ISO20022 y sus DTOs

```
¿Se agregaron campos obligatorios al builder de ISO20022?
¿Se renombraron DTOs o sus métodos?
¿Se eliminaron campos existentes?
```
→ Si sí: este es el cambio de mayor impacto — afecta toda la capa de mapeo:
```
Grep: ISO20022.builder() en src/main/java/
Grep: com.bbva.gateway.dto.iso20022 en src/main/java/
```

---

## Paso 3 — Actualizar el pom.xml

Solo después de haber completado los Pasos 1 y 2 y tener un plan claro:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.bbva.orchlib</groupId>
    <artifactId>orchestratorlib</artifactId>
    <version>[nueva versión]</version>  <!-- era 2.16.0 -->
</dependency>
```

Versión actual en pgwp: `2.16.0`

---

## Paso 4 — Actualizar los YMLs si es necesario

Si el Paso 1 detectó cambios en la estructura del YML esperado por kgwy:

```
# Archivos a revisar y posiblemente actualizar:
src/main/resources/application-local.yml
src/main/resources/application-global.yml
src/main/resources/application-data.yml
src/main/resources/application-datalocal.yml

# Actualizar también los ejemplos de referencia:
docs/configuration-examples/application-local.yml
docs/configuration-examples/application-datalocal.yml
```

Para cambios específicos en reglas o redes, usar los skills heredados:

```
# Agregar nueva regla a una red existente:
execute dependencies/kgwy_javalib_orchestrator/claude-skills/add-network-rule.md

# Diagnosticar si las reglas actuales siguen siendo válidas:
execute dependencies/kgwy_javalib_orchestrator/claude-skills/troubleshoot-rules.md
```

---

## Paso 5 — Actualizar el código Java si es necesario

Si el Paso 2 detectó cambios en la API transitiva de arqGw:

**Para cambios en `LogsTraces`:**

```
Reemplazar todos los usos de la firma antigua por la nueva
en los 11 archivos que usan LogsTraces
execute docs/claude-skills/handle-arqgw-errors-pgwp.md
```

**Para cambios en `GrpcHeadersInfo`:**

```
Reemplazar los métodos renombrados en los 6 archivos afectados
Verificar que el ComponentScan sigue descubriendo HeadersInterceptor
```

**Para cambios en `ISO20022` o sus DTOs:**

```
Actualizar DefaultDelegateMapper y buildFallbackResponse()
Actualizar las 10 SectionMappingStrategy impactadas
Actualizar MonitoringBuilder si se reestructuró MonitoringDTO
Compilar y revisar errores de tipo
```

**Para nuevas interfaces requeridas por kgwy:**

```
Crear nueva clase @Component que implemente la nueva interfaz
Registrarla en el contexto Spring (automático via ComponentScan "com.bbva.orchestrator")
```

---

## Paso 6 — Actualizar la documentación de pgwp

Documentos a actualizar después de completar el código:

### 6a. orchestratorlib-contract.md

```
Reemplazar el contenido actual con el nuevo proporcionado por el equipo de kgwy:
dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md
```

### 6b. TRANSITIVE-CONTRACT.md (solo si cambió arqGw)

```
Si la nueva versión de kgwy actualiza arqGw, regenerar el contrato transitivo:
execute prompts/generate-transitive-contract.md
```

### 6c. CONTRACT.md (si cambió cómo pgwp usa kgwy)

```
Si se agregaron/eliminaron puertos o cambió significativamente la integración:
execute prompts/generate-consumer-contract.md
```

### 6d. CLAUDE.md de pgwp

```
Actualizar Sección 2 — Stack técnico:
- kgwy_javalib_orchestrator: [nueva versión]
- arqGw: [nueva versión si cambió]

Actualizar Sección 3 si cambiaron funciones usadas/no usadas.
```

### 6e. Registrar en CHANGELOG.md

```
Crear o actualizar updates/CHANGELOG.md con:
| [versión kgwy nueva] | [fecha YYYY-MM-DD] | [cambios aplicados en pgwp] |
```

---

## Checklist completo

### Pre-actualización

- [ ] `orchestratorlib-contract.md` comparado — breaking changes identificados
- [ ] `TRANSITIVE-CONTRACT.md` comparado — cambios en arqGw identificados
- [ ] Plan de cambios definido antes de tocar el `pom.xml`
- [ ] Equipo informado del alcance de la actualización

### Código

- [ ] `pom.xml` actualizado con la nueva versión
- [ ] YMLs actualizados si la estructura del YML cambió
- [ ] `LogsTraces` actualizado en los 11 archivos si cambió la API
- [ ] `GrpcHeadersInfo` actualizado en los 6 archivos si cambiaron los métodos
- [ ] `ISO20022` + DTOs actualizados en la capa de mapeo si cambió la estructura
- [ ] Nuevas interfaces implementadas si kgwy agregó puertos requeridos
- [ ] El proyecto compila sin errores

### Ejecución

- [ ] La aplicación inicia sin errores de binding de properties
- [ ] Health endpoint responde: `GET http://localhost:8083/actuator/health`
- [ ] Un mensaje de prueba `0100 PEER01` llega a MonitorService
- [ ] Un mensaje de prueba `0110 PEER01` llega a ProxyService/processor

### Documentación

- [ ] `orchestratorlib-contract.md` actualizado con la nueva versión
- [ ] `TRANSITIVE-CONTRACT.md` regenerado si cambió arqGw
- [ ] `CONTRACT.md` regenerado si cambió cómo pgwp usa kgwy
- [ ] `CLAUDE.md` §2 actualizado con las nuevas versiones
- [ ] `CHANGELOG.md` registrado

---

## Antipatrones

- **Actualizar el pom.xml sin revisar los contratos primero**: la compilación puede
  ser exitosa pero el comportamiento en runtime puede cambiar si kgwy modificó la
  estructura del YML esperado o eliminó funciones.

- **Asumir que una actualización de orchestratorlib no impacta arqGw**: kgwy puede
  actualizar arqGw internamente sin mencionarlo explícitamente. Siempre revisar el
  TRANSITIVE-CONTRACT.md actualizado.

- **No regenerar TRANSITIVE-CONTRACT.md después de un cambio de versión de arqGw**:
  el contrato transitivo queda desactualizado y las próximas sesiones de Claude
  interpretarán incorrectamente el código de pgwp.

- **No registrar en CHANGELOG.md**: sin historial de cambios es imposible saber qué
  versión de kgwy introdujo un comportamiento o una rotura.

- **Actualizar solo la versión en pom.xml sin actualizar CLAUDE.md**: la Sección 2
  de CLAUDE.md quedará desactualizada y Claude usará la versión incorrecta como
  referencia en futuras sesiones.

---

## Referencias

- `pom.xml` — versión actual de orchestratorlib
- `dependencies/kgwy_javalib_orchestrator/orchestratorlib-contract.md` — contrato actual de kgwy
- `dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md` — contrato transitivo actual
- `CLAUDE.md §2` — versiones registradas de kgwy y arqGw
- `docs/architecture.md §9` — tabla de cambios en dependencias que impactan a pgwp
