package com.bbva.orchestrator.core.builders;

import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ISO8583BuilderTest {

    @Test
    void testBuildISO8583() {
        // Arrange
        String originalMessage = "Test Original Message";
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("header", "Test Header");
        mapValues.put("messageType", "0200");
        mapValues.put("primaryAccountNumber", "1234567890123456");
        mapValues.put("processingCode", "000000");
        mapValues.put("transactionAmount", "1000");

        // Act
        ISO8583 iso8583 = ISO8583Builder.buildISO8583(originalMessage, mapValues);

        // Assert
        assertThat(iso8583).isNotNull();
        assertThat(iso8583.getOriginalMessage()).isEqualTo(originalMessage);
        assertThat(iso8583.getHeader()).isEqualTo("Test Header");
        assertThat(iso8583.getMessageType()).isEqualTo("0200");
        assertThat(iso8583.getPrimaryAccountNumber()).isEqualTo("1234567890123456");
        assertThat(iso8583.getProcessingCode()).isEqualTo("000000");
        assertThat(iso8583.getTransactionAmount()).isEqualTo("1000");
    }

    @Test
    void testBuildMapISO8583() {
        // Arrange
        ISO8583 iso8583 = ISO8583.builder()
                .header("Test Header")
                .messageType("0200")
                .primaryAccountNumber("1234567890123456")
                .processingCode("000000")
                .transactionAmount("1000")
                .build();

        // Act
        Map<String, String> mapValues = ISO8583Builder.buildMapISO8583(iso8583);

        // Assert
        assertThat(mapValues).isNotNull();
        assertThat(mapValues).containsEntry("header", "Test Header");
        assertThat(mapValues).containsEntry("messageType", "0200");
        assertThat(mapValues).containsEntry("primaryAccountNumber", "1234567890123456");
        assertThat(mapValues).containsEntry("processingCode", "000000");
        assertThat(mapValues).containsEntry("transactionAmount", "1000");
    }

    @Test
    void testBuildISO8583WithMissingFields() {
        // Arrange
        String originalMessage = "Test Original Message";
        Map<String, String> mapValues = new HashMap<>();

        // Act
        ISO8583 iso8583 = ISO8583Builder.buildISO8583(originalMessage, mapValues);

        // Assert
        assertThat(iso8583).isNotNull();
        assertThat(iso8583.getOriginalMessage()).isEqualTo(originalMessage);
        assertThat(iso8583.getHeader()).isEmpty();
        assertThat(iso8583.getMessageType()).isEmpty();
    }

    @Test
    void testInstanceBuildISO8583_callsStoreISO8583() {
        // Arrange
        ISO8583DelegateParser delegateParser = Mockito.mock(ISO8583DelegateParser.class);
        Map<String, String> mapValues = new HashMap<>();
    }
}