package com.bbva.orchestrator.core.logic;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.logic.factory.FieldLogicFactory;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

// Aún usamos MockitoExtension para poder simular llamadas estáticas (GrpcHeadersInfo)
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para FieldLogicFactory (con Stubs)")
class FieldLogicFactoryTest {
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    // --- Stubs ---
    // En lugar de mocks, creamos clases reales y vacías con los nombres exactos
    // que la fábrica espera. Estas clases son "dobles de prueba" o Stubs.
    private static class VisaDelegateFieldLogic implements NetworkDelegateFieldLogic {
        @Override
        public Map<String, String> applyLogicFields(Map<String, String> mapValues) {
            return null;
        }

        @Override
        public Map<String, String> parseSubfields(ISO8583 iso8583) {
            return null;
        }
    }
    private static class MastercardDelegateFieldLogic implements NetworkDelegateFieldLogic {
        @Override
        public Map<String, String> applyLogicFields(Map<String, String> mapValues) {
            return null;
        }

        @Override
        public Map<String, String> parseSubfields(ISO8583 iso8583) {
            return null;
        }
    }
    private static class DefaultDelegateFieldLogic implements NetworkDelegateFieldLogic {
        @Override
        public Map<String, String> applyLogicFields(Map<String, String> mapValues) {
            return null;
        }

        @Override
        public Map<String, String> parseSubfields(ISO8583 iso8583) {
            return null;
        }
    }


    private FieldLogicFactory fieldLogicFactory;

    @Nested
    @DisplayName("Pruebas para getDelegateFieldLogic(String peerId)")
    class GetDelegateByPeerIdTests {

        @Test
        @DisplayName("Debería devolver el delegado 'Visa' para el peerId 'peer01'")
        void shouldReturnVisaDelegateForPeer01() {
            // Arrange
            NetworkDelegateFieldLogic visaStub = new VisaDelegateFieldLogic();
            NetworkDelegateFieldLogic mastercardStub = new MastercardDelegateFieldLogic();
            fieldLogicFactory = new FieldLogicFactory(List.of(visaStub, mastercardStub));

            // Act
            NetworkDelegateFieldLogic result = fieldLogicFactory.getDelegateFieldLogic("peer01");

            // Assert
            assertThat(result).isSameAs(visaStub);
        }

        @Test
        @DisplayName("Debería devolver el delegado 'Mastercard' para el peerId 'peer02'")
        void shouldReturnMastercardDelegateForPeer02() {
            // Arrange
            NetworkDelegateFieldLogic visaStub = new VisaDelegateFieldLogic();
            NetworkDelegateFieldLogic mastercardStub = new MastercardDelegateFieldLogic();
            fieldLogicFactory = new FieldLogicFactory(List.of(visaStub, mastercardStub));

            // Act
            NetworkDelegateFieldLogic result = fieldLogicFactory.getDelegateFieldLogic("peer02");

            // Assert
            assertThat(result).isSameAs(mastercardStub);
        }

        @Test
        @DisplayName("Debería devolver el primer delegado de la lista si el peerId es desconocido")
        void shouldReturnDefaultDelegateWhenPeerIdIsUnknown() {
            // Arrange
            NetworkDelegateFieldLogic visaStub = new VisaDelegateFieldLogic();
            NetworkDelegateFieldLogic mastercardStub = new MastercardDelegateFieldLogic();
            // Visa es el primero en la lista, por lo tanto, el default
            fieldLogicFactory = new FieldLogicFactory(List.of(visaStub, mastercardStub));

            // Act
            NetworkDelegateFieldLogic result = fieldLogicFactory.getDelegateFieldLogic("unknown_peer");

            // Assert
            assertThat(result).isSameAs(visaStub);
        }

        @Test
        @DisplayName("Debería devolver el delegado 'default' si existe y el peerId es desconocido")
        void shouldReturnSpecificDefaultDelegateWhenPeerIdIsUnknown() {
            // Arrange
            NetworkDelegateFieldLogic visaStub = new VisaDelegateFieldLogic();
            NetworkDelegateFieldLogic mastercardStub = new MastercardDelegateFieldLogic();
            NetworkDelegateFieldLogic defaultStub = new DefaultDelegateFieldLogic();
            // Aunque Visa es el primero, debe preferir el que se llama 'default'
            fieldLogicFactory = new FieldLogicFactory(List.of(visaStub, defaultStub, mastercardStub));

            // Act
            NetworkDelegateFieldLogic result = fieldLogicFactory.getDelegateFieldLogic("unknown_peer");

            // Assert
            assertThat(result).isSameAs(defaultStub);
        }

        @Test
        @DisplayName("Debería devolver el delegado por defecto si el peerId es nulo")
        void shouldReturnDefaultDelegateWhenPeerIdIsNull() {
            // Arrange
            NetworkDelegateFieldLogic visaStub = new VisaDelegateFieldLogic();
            NetworkDelegateFieldLogic mastercardStub = new MastercardDelegateFieldLogic();
            fieldLogicFactory = new FieldLogicFactory(List.of(visaStub, mastercardStub));

            // Act
            NetworkDelegateFieldLogic result = fieldLogicFactory.getDelegateFieldLogic(null);

            // Assert
            assertThat(result).isSameAs(visaStub);
        }
    }

    @Nested
    @DisplayName("Pruebas de casos borde y errores")
    class EdgeCaseTests {

        @Test
        @DisplayName("Debería lanzar IllegalStateException si no hay delegados disponibles")
        void shouldThrowExceptionWhenNoDelegatesAreAvailable() {
            // Arrange
            // Creamos la fábrica con una lista vacía
            fieldLogicFactory = new FieldLogicFactory(Collections.emptyList());

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                fieldLogicFactory.getDelegateFieldLogic("any_peer");
            });

            // NOTA: Este mensaje de error en tu código fuente es incorrecto, pero el test
            // debe verificar el comportamiento actual del código. Lo ideal sería corregirlo.
            assertThat(exception.getMessage()).isEqualTo("No hay ningún ISO8583DelegateParser disponible");
        }
    }
}