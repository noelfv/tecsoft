package com.bbva.orchestrator.configuration;

import com.bbva.orchlib.configuration.BusinessDataLocalLoad;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Custom;
import com.bbva.orchlib.configuration.preloaddto.businessdatalocal.BusinessDataLocal;
import com.bbva.orchlib.configuration.preloaddto.businessdatalocal.InputLRA;
import com.bbva.orchlib.configuration.preloaddto.businessdatalocal.OutputLRA;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ApplicationDataLocalCache {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BusinessDataLocalLoad businessDataLocalLoad;
    private final static Map<String, Map<String, String>> inputLRAByNetwork = new ConcurrentHashMap<>();
    private final static Map<String, Map<String, String>> outputLRAByNetwork = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Map<String, String>>> customDataByNetwork = new ConcurrentHashMap<>();

    public ApplicationDataLocalCache(BusinessDataLocalLoad businessDataLocalLoad) {
        this.businessDataLocalLoad = businessDataLocalLoad;
    }

    /**
     * Este método se ejecuta automáticamente después de que el bean ha sido construido por Spring.
     * Es ideal para la carga inicial de datos estáticos.
     */
    @PostConstruct
    public void init() {

        for (BusinessDataLocal dataLocal : businessDataLocalLoad.getDatalocal()) {
            String network = dataLocal.getNetwork();
            // Precargar inputLRA
            if (dataLocal.getInputLRA() != null) {
                Map<String, String> inputMap = createInputLRA(dataLocal.getInputLRA());
                inputLRAByNetwork.put(network, inputMap);
            }
            // Precargar outputLRA
            if (dataLocal.getOutputLRA() != null) {
                Map<String, String> outputMap = createOutputLRA(dataLocal.getOutputLRA());
                outputLRAByNetwork.put(network, outputMap);
            }

            // Precargar custom
            if (dataLocal.getCustom() != null) {
                Map<String, Map<String,String>> outputMap = createCustom(dataLocal.getCustom());
                customDataByNetwork.put(network, outputMap);
            }
        }
    }
    /**
     * Obtiene el valor de inputLRA por network y key
     */
    public String getInputLRAValue(String network, String key) {
        Map<String, String> map = inputLRAByNetwork.get(network);
        return map != null ? map.get(key) : null;
    }


    /**
     * Obtiene el valor de outputLRA por network y key
     */
    public String getOutputLRAValue(String network, String key) {
        Map<String, String> map = outputLRAByNetwork.get(network);
        return map != null ? map.get(key) : null;
    }
    // Red - responde code - label/codigo de respuesta
    public String getCustomValue(String network,String section, String key) {

        Map<String, Map<String, String>> networkMap = customDataByNetwork.get(network);
        if (networkMap == null) {
            return null;
        }
        Map<String, String> sectionMap = networkMap.get(section);
        if (sectionMap == null) {
            return null;
        }
        return sectionMap.get(key);
    }

    public Map<Integer, String> getFieldsResponse(String network, String key) {

        Map<String, Map<String, String>> networkMap = customDataByNetwork.get(network);
        if (networkMap == null) {
            return null;
        }
        Map<String, String> sectionMap = networkMap.get("map_fields_response");
        if (sectionMap == null) {
            return null;
        }

        String jsonInput=sectionMap.get(key);
        return convertJsonToMap(jsonInput);
    }

    private Map<String, Map<String, String>> createCustom(Map<String, List<Custom>> mapCustom) {
        Map<String, Map<String, String>> result = new ConcurrentHashMap<>();
        if (mapCustom == null) {
            return result;
        }
        for (Map.Entry<String, List<Custom>> entry : mapCustom.entrySet()) {
            String sectionKey = entry.getKey();
            List<Custom> customList = entry.getValue();
            Map<String, String> sectionMap;

            if ("response_code".equals(sectionKey)) {
                // Para la sección 'response_code', carga el mapeo directo e inverso.
                sectionMap = new ConcurrentHashMap<>();
                for (Custom custom : customList) {
                    sectionMap.put(custom.getName(), custom.getValue());
                    sectionMap.put(custom.getValue(), custom.getName());
                }
            } else {
                // Para todas las demás secciones, carga solo el mapeo directo.
                sectionMap = customList.stream()
                        .collect(Collectors.toMap(Custom::getName, Custom::getValue, (v1, v2) -> v2, ConcurrentHashMap::new));
            }
            result.put(sectionKey, sectionMap);
        }
        return result;
    }

    private Map<Integer, String> convertJsonToMap(String jsonInput) {
        if (jsonInput == null || jsonInput.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(jsonInput, new TypeReference<Map<Integer, String>>() {});
        } catch (IOException e) {
            // Manejar la excepción apropiadamente
            throw new RuntimeException("Error al parsear la cadena JSON", e);
        }
    }

    private  Map<String, String> createInputLRA(List<InputLRA> listInputLRA) {
        return listInputLRA.stream()
                .collect(Collectors.toMap(InputLRA::getKey, InputLRA::getValue));
    }

    private  Map<String, String> createOutputLRA(List<OutputLRA> listOutputLRA) {
        return listOutputLRA.stream()
                .collect(Collectors.toMap(OutputLRA::getKey, OutputLRA::getValue));
    }

}