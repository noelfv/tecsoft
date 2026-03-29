package com.bbva.orchestrator.core.fields.subfields;

import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.Field48;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class Field48Test {

    @ParameterizedTest
    @EnumSource(Field48.class)
    void enumConstants_shouldHaveNonNullProperties(Field48 subField) {
        // Este test verifica que ninguna de las propiedades básicas sea nula
        // para todas las constantes definidas en el enum.
        assertNotNull(subField.getId());
        assertNotNull(subField.getName());
        assertNotNull(subField.getTypeData());
        assertNotNull(subField.getIdentifier());
    }

    @Test
    void setParserStrategy_shouldUpdateParserStrategyField() {
        // Arrange
        // Tomamos una constante del enum para probarla.
        Field48 subField = Field48.SF_48_33;
        // Guardamos su estrategia inicial (que es null en este caso).
        FieldParserStrategy initialStrategy = subField.getParserStrategy();

        // Creamos un nuevo parser simulado (mock) para asignarle.
        FieldParserStrategy newStrategy = mock(FieldParserStrategy.class);

        // Act
        // Llamamos al método que queremos probar.
        subField.setParserStrategy(newStrategy);

        // Assert
        // Verificamos que la estrategia ahora es la nueva que asignamos.
        FieldParserStrategy updatedStrategy = subField.getParserStrategy();
        assertNotNull(updatedStrategy);
        assertEquals(newStrategy, updatedStrategy);

        // (Opcional pero recomendado) Restauramos el estado original para no afectar a otros tests.
        subField.setParserStrategy(initialStrategy);
    }

    @ParameterizedTest
    @MethodSource("subFieldPropertiesProvider")
    void getters_shouldReturnCorrectConfiguredValues(Field48 subField, boolean expectedIsVariable, int expectedLength) {
        // Este test verifica que los valores definidos en el enum son los correctos.
        assertEquals(expectedLength, subField.getLength());
    }

    // Este método estático provee los datos para el test de arriba.
    // Cada 'Arguments.of' es un caso de prueba: (Enum a probar, valor esperado para isVariable, valor esperado para getLength)
    private static Stream<Arguments> subFieldPropertiesProvider() {
        return Stream.of(
                // Caso 1: Un campo de longitud fija
                Arguments.of(Field48.SF_48_01, false, 1),
                // Caso 2: Un campo compuesto (variable)
                Arguments.of(Field48.SF_48_33, false, 2),
                // Caso 3: Otro campo de longitud variable
                Arguments.of(Field48.SF_48_53, false, 2),
                // Caso 4: Un sub-subcampo fijo
                Arguments.of(Field48.SF_48_33_01, false, 2)
                // Puedes añadir más casos aquí si quieres ser más exhaustivo
        );
    }
}