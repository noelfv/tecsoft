package com.bbva.orchestrator.core.utils;

import com.bbva.orchlib.parser.ParserException;
import java.nio.charset.Charset;
import java.util.HexFormat;
import java.util.Map;

public class ISOUtil {

    private ISOUtil() {
    }

    public static final Charset EBCDIC_CHARSET = Charset.forName("Cp1047");
    private static final HexFormat FORMATTER = HexFormat.of().withUpperCase();

    public static final Map<Character, String> hexChart = Map.ofEntries(
            Map.entry('0', "0000"),
            Map.entry('1', "0001"),
            Map.entry('2', "0010"),
            Map.entry('3', "0011"),
            Map.entry('4', "0100"),
            Map.entry('5', "0101"),
            Map.entry('6', "0110"),
            Map.entry('7', "0111"),
            Map.entry('8', "1000"),
            Map.entry('9', "1001"),
            Map.entry('A', "1010"),
            Map.entry('a', "1010"),
            Map.entry('B', "1011"),
            Map.entry('b', "1011"),
            Map.entry('C', "1100"),
            Map.entry('c', "1100"),
            Map.entry('D', "1101"),
            Map.entry('d', "1101"),
            Map.entry('E', "1110"),
            Map.entry('e', "1110"),
            Map.entry('F', "1111"),
            Map.entry('f', "1111")
    );

    public static final Map<String, Character> binChart = Map.ofEntries(
            Map.entry("0000", '0'),
            Map.entry("0001", '1'),
            Map.entry("0010", '2'),
            Map.entry("0011", '3'),
            Map.entry("0100", '4'),
            Map.entry("0101", '5'),
            Map.entry("0110", '6'),
            Map.entry("0111", '7'),
            Map.entry("1000", '8'),
            Map.entry("1001", '9'),
            Map.entry("1010", 'A'),
            Map.entry("1011", 'B'),
            Map.entry("1100", 'C'),
            Map.entry("1101", 'D'),
            Map.entry("1110", 'E'),
            Map.entry("1111", 'F')
    );

    // --- Métodos de Conversión EBCDIC Hex a String (ASCII) ---
    public static String convertHEXtoEBCDIC(String valueHex) {
        byte[] ebcdic = hexStringToByteArray(valueHex);
        return new String(ebcdic, EBCDIC_CHARSET);
    }
    public static byte[] hexStringToByteArray(String s) {
        return HexFormat.of().parseHex(s);
    }

    // --- Métodos de Conversión de Bitmap ---
    public static String convertHEXtoBITMAP(String valueHex) {
        StringBuilder binaryBitMap = new StringBuilder();
        for (char c : valueHex.toCharArray()) {
            if (!hexChart.containsKey(c)) {
                throw new ParserException("Malformed BitMap: " + binaryBitMap);
            }
            binaryBitMap.append(hexChart.get(c));
        }
        return String.valueOf(binaryBitMap);
    }

    public static String convertBITMAPtoHEX(String binaryBitmap) {
        StringBuilder hexBitmap = new StringBuilder();
        for (int i = 0; i < binaryBitmap.length(); i += 4) {
            String binary = binaryBitmap.substring(i, i + 4);
            hexBitmap.append(binChart.get(binary));
        }
        return hexBitmap.toString();
    }

    // --- Métodos de Conversión EBCDIC Hex a String (Numérico) ---
    public static String ebcdicToString(String messageHexEbcdic) {
        // Asume que 'messageHexEbcdic' es una cadena HEX EBCDIC
        return new String(FORMATTER.parseHex(messageHexEbcdic), EBCDIC_CHARSET);
    }

    // --- Métodos de Conversión String (ASCII) a EBCDIC Hex ---
    public static String stringToEBCDICHex(String inputAscii) {
        byte[] ebcdicBytes = inputAscii.getBytes(EBCDIC_CHARSET);
        return HexFormat.of().withUpperCase().formatHex(ebcdicBytes);
    }

    // --- Metodo para validar longitud, si es impar le añade +1 ---
    public static int alignToEvenLength(int value) {
        return value + (value % 2);
    }

    // Añade un '0' como prefijo a un String si su longitud actual es impar.
    public static String prefixZeroIfOdd(String value) {
        return ((value.length() & 1) == 1) ? "0" + value : value;
    }
}