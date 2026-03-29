# Skills de pgwp_java_orchestrator

## Skills propios de pgwp

Generados en `prompts/phase4-skills.md`.
Usan redes, reglas y código reales de pgwp_java_orchestrator — no los ejemplos genéricos de kgwy.

| Skill | Cuándo usarlo |
|-------|--------------|
| `run-local.md` | Buildear y ejecutar el servicio localmente con terminal de logs en tiempo real |
| `setup-new-environment.md` | Configurar pgwp_java_orchestrator en un nuevo ambiente desde cero |
| `handle-arqgw-errors-pgwp.md` | Agregar o modificar el manejo de errores en el código Java de pgwp |
| `update-kgwy-version.md` | Actualizar la versión de kgwy_javalib_orchestrator en pgwp |

## Skills heredados de kgwy_javalib_orchestrator

Disponibles en:
```
dependencies/kgwy_javalib_orchestrator/claude-skills/
```

| Skill | Cuándo usarlo |
|-------|--------------|
| `add-network-rule.md` | Agregar una regla a una red existente (PEER01 o PEER02) en el YML |
| `add-new-network.md` | Agregar una red nueva al YML (ej. PEER03) |
| `add-new-function.md` | Activar una nueva función de kgwy en una regla del YML |
| `configure-filter-labels.md` | Agregar o modificar aliases en `local.filterLabels` |
| `troubleshoot-rules.md` | Diagnosticar por qué un tipo de mensaje no se enruta correctamente |

## Cómo invocar un skill

```
# Skill propio de pgwp:
execute docs/claude-skills/run-local.md
execute docs/claude-skills/[nombre-skill].md

# Skill heredado de kgwy:
execute dependencies/kgwy_javalib_orchestrator/claude-skills/[nombre-skill].md
```

## Decisión: ¿skill propio o heredado?

```
¿El cambio modifica el YML de pgwp (reglas, redes, aliases)?
  → Usar skill heredado de kgwy

¿El cambio configura pgwp en un ambiente nuevo (variables de entorno, YMLs completos)?
  → Usar setup-new-environment.md

¿El cambio modifica el manejo de errores en el código Java de pgwp?
  → Usar handle-arqgw-errors-pgwp.md

¿Se actualiza la versión de orchestratorlib en el pom.xml?
  → Usar update-kgwy-version.md
```

## Contexto de los skills heredados aplicado a pgwp

Los skills heredados usan ejemplos genéricos de kgwy. Al aplicarlos a pgwp, recordar:

| Aspecto en el skill heredado | Equivalente en pgwp |
|------------------------------|---------------------|
| Redes de ejemplo (`PEER01_EXAMPLE`) | Redes reales de pgwp: `PEER01` (Visa), `PEER02` (Mastercard) |
| Funciones disponibles (16 total) | pgwp solo usa: `monitor`, `updatemonitor`, `host`, `processor` |
| filterLabels con múltiples aliases | pgwp tiene solo 2: `pan` y `messageType` |
| `validations` con reglas | pgwp tiene `validations:` vacío en todos los perfiles |

## Lectura recomendada antes de usar cualquier skill

```
1. CLAUDE.md — contexto general y reglas de interpretación
2. docs/configuration-examples/application-local.yml — reglas reales actuales
3. dependencies/kgwy_javalib_orchestrator/CONTRACT.md — si el cambio afecta la integración con kgwy
4. dependencies/kgwy_javalib_orchestrator/transitive/arqGw/TRANSITIVE-CONTRACT.md — si el cambio toca código Java con com.bbva.gateway.*
```
