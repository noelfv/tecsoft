package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.SecurityTrailerDTO;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityTrailerMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;

    @InjectMocks
    private SecurityTrailerMappingStrategy securityTrailerMappingStrategy;

    @Test
    void map_whenSecurityInfoExists_shouldReturnSecurityTrailerDTO() {
        String securityInfo = "01020304";
        CanonicalFields fields = CanonicalFields.of(Map.of("securityControlInformation", securityInfo));

        when(fieldService.isNullOrEmptySubstring(securityInfo, 0, 2)).thenReturn("01");
        when(fieldService.isNullOrEmptySubstring(securityInfo, 2, 4)).thenReturn("02");
        when(fieldService.isNullOrEmptySubstring(securityInfo, 4, 6)).thenReturn("03");
        when(fieldService.isNullOrEmptySubstring(securityInfo, 6, 8)).thenReturn("04");

        SecurityTrailerDTO result = securityTrailerMappingStrategy.mapper(fields);

        assertNotNull(result);
        assertNotNull(result.getMacData());
        assertEquals("01", result.getMacData().getKeyProtection());
        assertEquals("02", result.getMacData().getAlgorithm());
        assertEquals("03", result.getMacData().getDerivedInformation());
        assertEquals("04", result.getMacData().getKeyIndex());
    }

    @Test
    void map_whenSecurityInfoIsNull_shouldReturnNull() {
        // securityControlInformation absent → getSecurityControlInformation() returns null
        CanonicalFields fields = CanonicalFields.of(new HashMap<>());

        SecurityTrailerDTO result = securityTrailerMappingStrategy.mapper(fields);

        assertNull(result);
    }

    @Test
    void unMapper_simplestTest() {
        SecurityTrailerDTO securityTrailerDTO = Mockito.mock(SecurityTrailerDTO.class, Mockito.RETURNS_DEEP_STUBS);

        Map<String, String> result = securityTrailerMappingStrategy.unMapper("PEER02", securityTrailerDTO);

        assertNull(result.get("securityControlInforma"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenNullInput() {
        // null input causes NPE → MapperFieldsException
        assertThatThrownBy(() -> securityTrailerMappingStrategy.mapper(null))
                .isInstanceOf(MapperFieldsException.class)
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }
}
