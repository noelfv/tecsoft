package com.bbva.orchestrator.configuration;

import com.bbva.orchlib.configuration.BusinessDataLocalLoad;
import com.bbva.orchlib.configuration.preloaddto.businessdatalocal.BusinessDataLocal;
import com.bbva.orchlib.configuration.preloaddto.businessdatalocal.InputLRA;
import com.bbva.orchlib.configuration.preloaddto.businessdatalocal.OutputLRA;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Custom;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDataLocalCacheTest {

    @Mock
    private BusinessDataLocalLoad businessDataLocalLoad;

    @InjectMocks
    private ApplicationDataLocalCache applicationDataLocalCache;

    private static Map<String, Map<String, Map<String, String>>> customDataByNetwork;


    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        applicationDataLocalCache = new ApplicationDataLocalCache(businessDataLocalLoad);
        // Creamos una instancia de la clase. Como no probamos init(), la dependencia puede ser null.
        //applicationDataLocalCache = new ApplicationDataLocalCache(null);

        // --- Limpieza del estado estático ---
        // Usamos reflexión para obtener el mapa estático y limpiarlo antes de cada test.
        // Esto es CRUCIAL para garantizar la independencia de las pruebas.
        Field field = ApplicationDataLocalCache.class.getDeclaredField("customDataByNetwork");
        field.setAccessible(true);
        customDataByNetwork = (Map<String, Map<String, Map<String, String>>>) field.get(null);
        customDataByNetwork.clear();
    }

    @Test
    void testInit_WithNoData() {
        // Act: Llama al método init().
        applicationDataLocalCache.init();

        // Assert: Comprueba que el caché está vacío.
        assertNull(applicationDataLocalCache.getInputLRAValue("network1", "key1"));
        assertNull(applicationDataLocalCache.getOutputLRAValue("network2", "keyB"));
    }

    @Test
    void testGetInputLRAValue_NotFound() {
        assertNull(applicationDataLocalCache.getInputLRAValue("nonexistentNetwork", "anyKey"));

        applicationDataLocalCache.init(); // Carga el caché con datos (ej. del test anterior)
        assertNull(applicationDataLocalCache.getInputLRAValue("network1", "anotherKey"));
    }

    @Test
    void testGetOutputLRAValue_NotFound() {

        assertNull(applicationDataLocalCache.getOutputLRAValue("nonexistentNetwork", "anyKey"));

        applicationDataLocalCache.init();
        assertNull(applicationDataLocalCache.getOutputLRAValue("network1", "anotherKey"));
    }

    @Test
    void testInit_WithData() {
        // Arrange
        InputLRA inputLRA = new InputLRA();
        inputLRA.setKey("key1");
        inputLRA.setValue("value1");
        OutputLRA outputLRA = new OutputLRA();
        outputLRA.setKey("keyA");
        outputLRA.setValue("valueA");
        BusinessDataLocal dataLocal = new BusinessDataLocal();
        dataLocal.setNetwork("network1");
        dataLocal.setInputLRA(Collections.singletonList(inputLRA));
        dataLocal.setOutputLRA(Collections.singletonList(outputLRA));
        when(businessDataLocalLoad.getDatalocal()).thenReturn(Collections.singletonList(dataLocal));

        // Act
        applicationDataLocalCache.init();

        // Assert
        assertEquals("value1", applicationDataLocalCache.getInputLRAValue("network1", "key1"));
        assertEquals("valueA", applicationDataLocalCache.getOutputLRAValue("network1", "keyA"));
    }

    @Test
    void testInit_OverwriteData() {
        // Arrange: primer dato
        InputLRA inputLRA1 = new InputLRA();
        inputLRA1.setKey("key1");
        inputLRA1.setValue("value1");
        OutputLRA outputLRA1 = new OutputLRA();
        outputLRA1.setKey("keyA");
        outputLRA1.setValue("valueA");
        BusinessDataLocal dataLocal1 = new BusinessDataLocal();
        dataLocal1.setNetwork("network1");
        dataLocal1.setInputLRA(Collections.singletonList(inputLRA1));
        dataLocal1.setOutputLRA(Collections.singletonList(outputLRA1));
        when(businessDataLocalLoad.getDatalocal()).thenReturn(Collections.singletonList(dataLocal1));
        applicationDataLocalCache.init();
        assertEquals("value1", applicationDataLocalCache.getInputLRAValue("network1", "key1"));
        assertEquals("valueA", applicationDataLocalCache.getOutputLRAValue("network1", "keyA"));

        // Arrange: sobreescribir con nuevos datos
        InputLRA inputLRA2 = new InputLRA();
        inputLRA2.setKey("key2");
        inputLRA2.setValue("value2");
        OutputLRA outputLRA2 = new OutputLRA();
        outputLRA2.setKey("keyB");
        outputLRA2.setValue("valueB");
        BusinessDataLocal dataLocal2 = new BusinessDataLocal();
        dataLocal2.setNetwork("network1");
        dataLocal2.setInputLRA(Collections.singletonList(inputLRA2));
        dataLocal2.setOutputLRA(Collections.singletonList(outputLRA2));
        when(businessDataLocalLoad.getDatalocal()).thenReturn(Collections.singletonList(dataLocal2));
        applicationDataLocalCache.init();
        // Ahora solo debe existir el nuevo valor
        assertNull(applicationDataLocalCache.getInputLRAValue("network1", "key1"));
        assertEquals("value2", applicationDataLocalCache.getInputLRAValue("network1", "key2"));
        assertNull(applicationDataLocalCache.getOutputLRAValue("network1", "keyA"));
        assertEquals("valueB", applicationDataLocalCache.getOutputLRAValue("network1", "keyB"));
    }

    @Test
    void testGetCustomValue_NullCases() {
        // No hay datos cargados
        assertNull(applicationDataLocalCache.getCustomValue("networkX", "sectionX", "keyX"));
    }

    @Test
    void testGetCustomValue_SectionNull() {
        // Arrange
        BusinessDataLocal dataLocal = new BusinessDataLocal();
        dataLocal.setNetwork("network1");
        dataLocal.setCustom(new HashMap<>());
        when(businessDataLocalLoad.getDatalocal()).thenReturn(List.of(dataLocal));
        applicationDataLocalCache.init();
        // No existe la sección
        assertNull(applicationDataLocalCache.getCustomValue("network1", "sectionX", "keyX"));
    }

    @Test
    void testGetCustomValue_SectionAndKey() {
        // Arrange
        Custom custom = new Custom();
        custom.setName("n1");
        custom.setValue("v1");
        Map<String, List<Custom>> customMap = new HashMap<>();
        customMap.put("section1", List.of(custom));
        BusinessDataLocal dataLocal = new BusinessDataLocal();
        dataLocal.setNetwork("network1");
        dataLocal.setCustom(customMap);
        when(businessDataLocalLoad.getDatalocal()).thenReturn(List.of(dataLocal));
        applicationDataLocalCache.init();
        // Existe la sección y la key
        assertEquals("v1", applicationDataLocalCache.getCustomValue("network1", "section1", "n1"));
        // Key inexistente
        assertNull(applicationDataLocalCache.getCustomValue("network1", "section1", "nope"));
    }

    @Test
    void testGetCustomValue_ResponseCodeBidirectional() {
        // Arrange
        Custom custom = new Custom();
        custom.setName("codeA");
        custom.setValue("descA");
        Map<String, List<Custom>> customMap = new HashMap<>();
        customMap.put("response_code", List.of(custom));
        BusinessDataLocal dataLocal = new BusinessDataLocal();
        dataLocal.setNetwork("network1");
        dataLocal.setCustom(customMap);
        when(businessDataLocalLoad.getDatalocal()).thenReturn(List.of(dataLocal));
        applicationDataLocalCache.init();
        // Mapeo directo
        assertEquals("descA", applicationDataLocalCache.getCustomValue("network1", "response_code", "codeA"));
        // Mapeo inverso
        assertEquals("codeA", applicationDataLocalCache.getCustomValue("network1", "response_code", "descA"));
    }

    @Test
    void shouldReturnMapWhenDataIsValid() {
        // Arrange
        String network = "VISA";
        String key = "00";
        String validJson = "{\"39\":\"00\", \"12\":\"153000\"}";

        // Poblamos manualmente el mapa estático con los datos de prueba
        Map<String, String> sectionMap = new ConcurrentHashMap<>();
        sectionMap.put(key, validJson);
        Map<String, Map<String, String>> networkMap = new ConcurrentHashMap<>();
        networkMap.put("map_fields_response", sectionMap);
        customDataByNetwork.put(network, networkMap);

        // Act
        Map<Integer, String> result = applicationDataLocalCache.getFieldsResponse(network, key);

        // Assert
        Assertions.assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsEntry(39, "00")
                .containsEntry(12, "153000");
    }

    @Test
    void shouldReturnNullWhenNetworkNotFound() {
        // Act
        Map<Integer, String> result = applicationDataLocalCache.getFieldsResponse("RED_INEXISTENTE", "00");

        // Assert
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenSectionNotFound() {
        // Arrange
        String network = "MASTERCARD";
        customDataByNetwork.put(network, new ConcurrentHashMap<>()); // Red existe, pero sin secciones

        // Act
        Map<Integer, String> result = applicationDataLocalCache.getFieldsResponse(network, "00");

        // Assert
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnEmptyMapWhenKeyNotFound() {
        // Este test prueba indirectamente el caso de convertJsonToMap(null)
        // Arrange
        String network = "VISA";
        Map<String, String> sectionMap = new ConcurrentHashMap<>();
        Map<String, Map<String, String>> networkMap = new ConcurrentHashMap<>();
        networkMap.put("map_fields_response", sectionMap);
        customDataByNetwork.put(network, networkMap);

        // Act
        Map<Integer, String> result = applicationDataLocalCache.getFieldsResponse(network, "CLAVE_INEXISTENTE");

        // Assert
        Assertions.assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void shouldReturnEmptyMapForEmptyJsonString() {
        // Este test prueba indirectamente el caso de convertJsonToMap("")
        // Arrange
        String network = "VISA";
        String key = "EMPTY_JSON";
        Map<String, String> sectionMap = new ConcurrentHashMap<>();
        sectionMap.put(key, "");
        Map<String, Map<String, String>> networkMap = new ConcurrentHashMap<>();
        networkMap.put("map_fields_response", sectionMap);
        customDataByNetwork.put(network, networkMap);

        // Act
        Map<Integer, String> result = applicationDataLocalCache.getFieldsResponse(network, key);

        // Assert
        Assertions.assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void shouldThrowRuntimeExceptionForInvalidJson() {
        // Este test prueba indirectamente el caso de convertJsonToMap("json inválido")
        // Arrange
        String network = "VISA";
        String key = "INVALID_JSON";
        String invalidJson = "esto no es un json";

        Map<String, String> sectionMap = new ConcurrentHashMap<>();
        sectionMap.put(key, invalidJson);
        Map<String, Map<String, String>> networkMap = new ConcurrentHashMap<>();
        networkMap.put("map_fields_response", sectionMap);
        customDataByNetwork.put(network, networkMap);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            applicationDataLocalCache.getFieldsResponse(network, key);
        });

        Assertions.assertThat(exception.getMessage()).isEqualTo("Error al parsear la cadena JSON");
    }

}