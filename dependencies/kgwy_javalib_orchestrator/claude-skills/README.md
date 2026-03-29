# Skills de kgwy_javalib_orchestrator

> Versión: `orchestratorlib 2.16.0`
> Ver también: `docs/architecture.md`, `docs/orchestration-rules-contract.md`

---

## Para consumidores de kgwy_javalib_orchestrator

**No requieren modificar código Java de kgwy_javalib_orchestrator.**
Solo modifican el `application-local.yml` del proyecto consumidor.

| Skill | Cuándo usarlo |
|---|---|
| [add-network-rule.md](add-network-rule.md) | Agregar una regla a una red existente (PEER01, PEER02, etc.) en el YML |
| [add-new-network.md](add-new-network.md) | Agregar una red completamente nueva al YML |
| [configure-filter-labels.md](configure-filter-labels.md) | Agregar o modificar alias de campos en `filterLabels` |
| [troubleshoot-rules.md](troubleshoot-rules.md) | Diagnosticar por qué un mensaje no se enruta correctamente |

---

## Para el equipo de kgwy_javalib_orchestrator

**Requieren modificar código Java.**
Afectan a todos los consumidores de la librería.

| Skill | Cuándo usarlo |
|---|---|
| [add-new-function.md](add-new-function.md) | Agregar una nueva función al mapeador de `OrchestrationsHandler` |
| [troubleshoot-rules.md](troubleshoot-rules.md) | Diagnosticar problemas internos del motor de reglas |

---

## Skills heredados de arqGw

Disponibles en `dependencies/kgwy_javalib_gateway/claude-skills/`.
También aplican a `kgwy_javalib_orchestrator` al ser un consumidor de arqGw.

| Skill | Cuándo usarlo |
|---|---|
| [use-logtraces.md](../../dependencies/kgwy_javalib_gateway/claude-skills/use-logtraces.md) | Implementar trazabilidad con `LogsTraces` en clientes gRPC |
| [register-interceptors.md](../../dependencies/kgwy_javalib_gateway/claude-skills/register-interceptors.md) | Configurar interceptores de arqGw |
| [handle-arqgw-errors.md](../../dependencies/kgwy_javalib_gateway/claude-skills/handle-arqgw-errors.md) | Manejar errores que vienen de arqGw (`InternalServerException`, `StatusRuntimeException`) |

---

## Regla fundamental del motor de reglas

> ⚠️ El campo `function` en el YML es una **lista de presencia**, no de orden.
> `function: host,monitor` ejecuta **monitor antes que host** — el orden de ejecución
> está fijo en `OrchestrationsHandler.processOrchestrationRules()`.
> Ver orden completo en `docs/architecture.md §4.4`.

---

## Cómo invocar un skill con Claude Code

```
execute docs/claude-skills/[nombre-del-skill].md
```

Ejemplos:
```
execute docs/claude-skills/add-network-rule.md
execute docs/claude-skills/troubleshoot-rules.md
```
