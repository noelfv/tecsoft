package com.bbva.orchestrator.core.utils;

import com.bbva.gateway.utils.LogsTraces;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

public class FieldUtil {

    public static boolean requiredProcess(String messageType) {
        return Set.of("0100","0101","0120","0400","0401","0420").contains(messageType);
    }
    /**
     * Parsea un String a Double, devuelve null si es nulo, vacío o inválido.
     */
    public static Double convertAmountDouble(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(amount.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String convertAmountString(double amount) {
        // Usar BigDecimal para evitar problemas de precisión con double
        BigDecimal amountInCents = BigDecimal.valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        // Formatea a 12 dígitos con ceros a la izquierda
        return String.format("%012d", amountInCents.longValue());
    }



    /**
     * Extrae un substring seguro. Si el índice está fuera de rango, devuelve null.
     */
    public static String isNullOrEmptySubstring(String source, int beginIndex, int endIndex) {
        if (source == null || source.length() < endIndex) {
            return null;
        }
        String substring = source.substring(beginIndex, endIndex);
        return substring.isEmpty() ? null : substring;
    }

    /**
     * Convierte fecha-hora de formato MMddHHmmss a yyyyMMddHHmmss
     */
    public static String convertFormatDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int year = LocalDate.now().getYear(); // year of the system
            String fullDate = year + value;  // yyyyMMddHHmmss

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime localDateTime = LocalDateTime.parse(fullDate, inputFormatter);
            Instant instant = localDateTime.atZone(ZoneOffset.UTC).toInstant();
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            return outputFormatter.withZone(ZoneOffset.UTC).format(instant);
        } catch (Exception e) {
            LogsTraces.writeWarning("Fecha por defecto por error en el formato de fecha: " + value);
            // USA LocalDateTime o Instant en lugar de LocalDate para incluir la hora
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        }
    }

    public static String reConvertFormatDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            // Parseando la fecha ISO con zona horaria Z (UTC)
            Instant instant = Instant.parse(value);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);

            // Formateo al patrón MMddHHmmss
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMddHHmmss");

            return zonedDateTime.format(outputFormatter);
        } catch (Exception e) {
            return null;
        }
    }


    public static String convertFormatDateExpiration(String dateExpiration) {
        if (dateExpiration == null || dateExpiration.isEmpty()) {
            return null;
        }

        try {
            // Se usa YearMonth para parsear directamente y manejar la lógica de fechas
            YearMonth yearMonth = YearMonth.parse("20"+dateExpiration, DateTimeFormatter.ofPattern("yyyyMM"));

            // Obtener el último día del mes
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            // Formatear la fecha a YYYY-MM-DD
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return lastDayOfMonth.format(formatter);

        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String reConvertFormatExpiryDate(String dateExpiration) {
        if (dateExpiration == null || dateExpiration.isEmpty()) {
            return null;
        }

        try {
            // Se define el formato de entrada
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Se parsea la cadena a un objeto LocalDate
            LocalDate date = LocalDate.parse(dateExpiration, inputFormatter);
            // Se define el formato de salida
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyMM");
            // Se formatea el objeto LocalDate al formato deseado
            return date.format(outputFormatter);

        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Valida tasa de conversión: si es 000000, devuelve null.
     */
    public static Double conversionRateValidation(String rate) {
        Double value = convertAmountDouble(rate);
        return (value != null && value == 0.0) ? null : value;
    }

    public static String convertEffectiveExchangeRate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        StringBuilder conversionRate = new StringBuilder(value);

        // Obtenemos la precisión del primer dígito
        int precision = Character.getNumericValue(conversionRate.charAt(0));
        conversionRate.deleteCharAt(0);

        // Calculamos la posición donde debe ir el punto
        int insertPosition = conversionRate.length() - precision;

        // Si la posición es negativa, significa que faltan ceros a la izquierda
        if (insertPosition < 0) {
            int zerosToAdd = Math.abs(insertPosition);
            for (int i = 0; i < zerosToAdd; i++) {
                conversionRate.insert(0, "0");
            }
            insertPosition = 0; // El punto irá al inicio
        }

        conversionRate.insert(insertPosition, ".");

        // Para buena legibilidad: si el string empieza con punto (ej. ".1234567"), agregar un "0"
        if (conversionRate.charAt(0) == '.') {
            conversionRate.insert(0, "0");
        }

        return conversionRate.toString();
    }

    public static String convertConversionRate(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        StringBuilder conversionRate = new StringBuilder(value);
        int dotPosition = conversionRate.indexOf(".");
        int precision = 0;

        // Si tiene punto decimal, lo procesamos
        if (dotPosition != -1) {
            precision = conversionRate.length() - (dotPosition + 1);
            conversionRate.deleteCharAt(dotPosition);
        }

        // Aseguramos que la precisión no sea mayor a 9 (solo ocupa 1 dígito en ISO8583)
        if (precision > 9) precision = 9;

        while(conversionRate.length() > 7) {
            conversionRate.deleteCharAt(0);
        }

        // Insertamos el dígito de precisión al inicio
        conversionRate.insert(0, precision);

        // Rellenamos con ceros a la derecha hasta alcanzar la longitud de 8 caracteres estándar
        while (conversionRate.length() < 8) {
            conversionRate.append("0");
        }

        // Si nos pasamos de 8 caracteres (trama malformada), lo truncamos a los primeros 8
        if (conversionRate.length() > 8) {
            return conversionRate.substring(0, 8);
        }

        return conversionRate.toString();
    }


    // --- Métodos Específicos para Montos ---
    public static String validAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return null;
        }
        String amountGeneral = amount.substring(0, amount.length() - 2);
        String amountCents = amount.substring(amount.length() - 2);
        return amountGeneral + "." + amountCents;
    }

    public static String revertValidAmount(String amount) {
        String revertedAmount = amount;
        if (amount.contains(",") || amount.contains(".")) {
            revertedAmount = amount.replace(",", "").trim();
            revertedAmount = revertedAmount.replace(".", "").trim();
        }
        return revertedAmount;
    }

    //TODO revisar el processError
    // --- Métodos para manejar errores de trama ---
    public static String processError(String messageIso,String network, boolean containsSecondaryBitmap) {
        if(!containsSecondaryBitmap){
            return replaceWithF0(messageIso,28,24);
        }
        return replaceWithF0(messageIso,44,24);
    }

    public static String formatMessageException(String code,String description,Throwable cause) {
        return String.format("Error Code: %s, Description: %s, Cause: %s", code, description, cause != null ? cause.getMessage() : "No cause provided");
    }

    public static String replaceWithF0(String originalString, int startPosition, int charsToReplace) {
        if (originalString == null || originalString.isEmpty()) {
            return originalString;
        }
        if (startPosition >= originalString.length()) {
            return originalString;
        }
        int endPosition = Math.min(startPosition + charsToReplace, originalString.length());
        StringBuilder replacement = new StringBuilder();
        for (int i = 0; i < charsToReplace / 2; i++) {
            replacement.append("F0");
        }
        if (charsToReplace % 2 != 0) {
            replacement.append("F");
        }
        return originalString.substring(0, startPosition) +
                replacement.toString() +
                originalString.substring(endPosition);
    }

    public static String getValue(String fieldName, Map<String, String> values) {
        if (values.containsKey(fieldName)) {
            return values.get(fieldName);
        }
        return "";
    }

    public static String extractSegment(String cadena, boolean containsSecondaryBitmap) {
        final int PRIMARIO_CORTE = 64;
        final int PRIMARIO_OFFSET = 82;
        final int SECUNDARIO_CORTE = 80;
        final int SECUNDARIO_OFFSET = 98;

        if (cadena == null) {
            return "La cadena no puede ser nula.";
        }

        int corte = containsSecondaryBitmap ? SECUNDARIO_CORTE : PRIMARIO_CORTE;
        int offset = containsSecondaryBitmap ? SECUNDARIO_OFFSET : PRIMARIO_OFFSET;

        if (cadena.length() < offset) {
            return "Longitud de la cadena " + cadena.length() + " offset :" + offset;
        }

        // Extrae desde el inicio hasta 'corte', y desde 'offset' hasta el final
        return cadena.substring(0, corte) + cadena.substring(offset);
    }
}