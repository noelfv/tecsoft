package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.SecurityTrailerDTO;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityTrailerMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;
    @Mock
    private ISO8583 mockInput;

    @InjectMocks
    private SecurityTrailerMappingStrategy securityTrailerMappingStrategy;

    @Test
    void map_whenSecurityInfoExists_shouldReturnSecurityTrailerDTO() {
        String securityInfo = "01020304";
        when(mockInput.getSecurityControlInformation()).thenReturn(securityInfo);
        when(fieldService.isNullOrEmptySubstring(securityInfo, 0, 2)).thenReturn("01");
        when(fieldService.isNullOrEmptySubstring(securityInfo, 2, 4)).thenReturn("02");
        when(fieldService.isNullOrEmptySubstring(securityInfo, 4, 6)).thenReturn("03");
        when(fieldService.isNullOrEmptySubstring(securityInfo, 6, 8)).thenReturn("04");

        SecurityTrailerDTO result = securityTrailerMappingStrategy.mapper(mockInput, Collections.emptyMap());

        assertNotNull(result);
        assertNotNull(result.getMacData());
        assertEquals("01", result.getMacData().getKeyProtection());
        assertEquals("02", result.getMacData().getAlgorithm());
        assertEquals("03", result.getMacData().getDerivedInformation());
        assertEquals("04", result.getMacData().getKeyIndex());
    }

    @Test
    void map_whenSecurityInfoIsNull_shouldReturnNull() {
        when(mockInput.getSecurityControlInformation()).thenReturn(null);

        SecurityTrailerDTO result = securityTrailerMappingStrategy.mapper(mockInput, Collections.emptyMap());

        assertNull(result);
    }

    @Test
    void unMapper_simplestTest() {

        SecurityTrailerDTO securityTrailerDTO = Mockito.mock(SecurityTrailerDTO.class, Mockito.RETURNS_DEEP_STUBS);

        Map<String, String> result = securityTrailerMappingStrategy.unMapper("PEER02",securityTrailerDTO);

        assertNull(result.get("securityControlInforma"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getSecurityControlInformation()).thenThrow(new RuntimeException("Error al mapear desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> securityTrailerMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }
}