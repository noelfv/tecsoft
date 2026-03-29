package com.bbva.orchestrator.core.fields.definitions.subfields.tlv;

import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeTlvFieldParser;

import java.util.*;

public class TLVFieldLoadStructure {

    private static final Map<String, Field48> BY_ID = new HashMap<>();
    // Mapa de Nietos: ID del Padre ("11") -> Mapa de Hijos ("01" -> Field)
    private static final Map<String, Map<String, Field48>> SUB_SUBFIELD_MAP = new LinkedHashMap<>();
    // Mapa de Hijos Directos del 48
    private static final Map<String, Field48> DIRECT_FIELD_48_DEFS = new LinkedHashMap<>();

    static {
        // --- FASE 1: CLASIFICACIÓN ---
        for (Field48 field : Field48.values()) {
            String id = field.getId();
            BY_ID.put(id, field);

            if (id.contains(".")) {
                // Es un Nieto (ej: "11.01")
                String[] parts = id.split("\\.");
                String parentId = parts[0];
                String subId = parts[1];

                SUB_SUBFIELD_MAP
                        .computeIfAbsent(parentId, k -> new LinkedHashMap<>())
                        .put(subId, field);
            } else {
                // Es un Hijo Directo (ej: "11", "33", "01", "48")
                // NOTA: Aquí ya NO filtramos el "48", permitimos que entre como hijo.
                DIRECT_FIELD_48_DEFS.put(id, field);
            }
        }

        // --- FASE 2: PROMOCIÓN DE PADRES ---
        for (Map.Entry<String, Field48> entry : DIRECT_FIELD_48_DEFS.entrySet()) {
            String id = entry.getKey();
            Field48 fieldDef = entry.getValue();

            Map<String, Field48> childrenDefs = SUB_SUBFIELD_MAP.get(id);

            // Si tiene nietos definidos, le asignamos la estrategia compuesta
            if (childrenDefs != null && !childrenDefs.isEmpty()) {
                fieldDef.setParserStrategy(new CompositeTlvFieldParser(id, childrenDefs));
            }
        }
    }

    private TLVFieldLoadStructure() {}

    /**
     * Obtiene solo los hijos directos del Campo 48 (Nivel 1).
     * Útil solo para inicializar el Parser Raíz.
     */
    public static Map<String, Field48> getDirectSubFieldDefinitionsForField48() {
        ensureInitialized();
        return DIRECT_FIELD_48_DEFS;
    }

    /**
     * Busca definiciones de NIETOS para un tag específico (Nivel 2).
     * Ej: Si pides "11", devuelve los hijos de 11.
     * Si pides "48" (el subcampo), devolverá vacío (porque no definiste 48.48.01).
     */
    public static Map<String, Field48> getSubFieldDefinitionsForComposite(String compositeFieldId) {
        ensureInitialized();
        // CORRECCIÓN: Quitamos el if ("48".equals(...)) para evitar el conflicto.
        return SUB_SUBFIELD_MAP.getOrDefault(compositeFieldId, Collections.emptyMap());
    }

    private static void ensureInitialized() {
        // Método dummy para forzar la carga del bloque static si aún no se ha hecho
        if (BY_ID.isEmpty()) { /* no-op */ }
    }
}