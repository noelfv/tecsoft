/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import com.novatronic.formatter.field.support.BitMap;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.Arrays;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class BmpUtil {

    private static final Logger log = Logger.getLogger(BmpUtil.class);
    private static final int BMP_BYTES_LENGTH = 16;
    private static final int BITMAP_RADIX = 16;
    private static final int BMP_BITS_LENGTH = 64;
    private static final byte FLG_SEC_BMP = 0;

    public static void init() {
        //Para ser invocado y pueda generarse los arreglos estaticos.
    }

    public static void setUpBitFlag(BitMap bitmap, int id) {
        log.debug("SetUP BMP field=" + id);
        bitmap.set(id - 1);
        if (id >= BMP_BITS_LENGTH) {
            log.debug("SetUP BMP secundario");
            bitmap.set(FLG_SEC_BMP);
        }
    }

    public static byte[] makeBitmap(BitMap bitmap, boolean compress) {
        byte[] bmp;
        int nible;
        int bitmapLength;

        bmp = createBmpBytes(bitmap.get(FLG_SEC_BMP), compress);
        bitmapLength = bitmap.get(FLG_SEC_BMP) ? BMP_BITS_LENGTH * 2 : BMP_BITS_LENGTH;
        LogMF.debug(log, "bitmap=[{0}], bitmapLength=", bitmap, bitmapLength);

        if (compress) {
            for (int i = 0; i < bitmapLength; i += BitMap.BYTE_LENGTH) {
                bmp[i / BitMap.BYTE_LENGTH] = (byte) bitmap.getByte(i);
            }
            log.debug("bmp=" + Converter.toHexaString(bmp) + ", bitmapLength=" + bitmapLength);
        } else {
            for (int i = 0; i < bitmapLength; i += BitMap.NIBBLE_LENGTH) {
                nible = bitmap.getNibble(i);
                bmp[i / BitMap.NIBBLE_LENGTH] = (byte) Character.toUpperCase(
                        Character.forDigit(nible, BITMAP_RADIX));
            }
            log.debug("bmp=" + Arrays.toString(bmp) + ", bitmapLength=" + bitmapLength);
        }

        return bmp;
    }

    private static byte[] createBmpBytes(boolean hasSecBimap, boolean isCompress) {
        if (hasSecBimap && isCompress) {
            return new byte[BMP_BYTES_LENGTH];
        } else if (!hasSecBimap && isCompress) {
            return new byte[BMP_BYTES_LENGTH / 2];
        } else if (hasSecBimap && !isCompress) {
            return new byte[BMP_BYTES_LENGTH * 2];
        } else {
            return new byte[BMP_BYTES_LENGTH];
        }
    }

    /**
     * Devuelve la cantidad de campos que el bitmap esta contemplando. Esto es dependiente
     * si existe o no el bitmap secundario
     *
     * @param bitmap Un objeto Bitmap que gestiona el los flags.
     * @return 64 si solo se tiene el bitmap primario, 128 si el bitmap tambien incluye el
     * secundario.
     */
    public static int getBitmapLength(BitMap bitmap) {
        return bitmap.get(FLG_SEC_BMP) ? BMP_BITS_LENGTH * 2 : BMP_BITS_LENGTH;
    }

    /**
     *
     * @param bitmap
     * @param compress
     * @return
     */
    public static int getBitmapBytesLength(BitMap bitmap, boolean compress) {
        if (compress) {
            return bitmap.get(FLG_SEC_BMP) ? BMP_BYTES_LENGTH : BMP_BYTES_LENGTH / 2;
        } else {
            return bitmap.get(FLG_SEC_BMP) ? BMP_BYTES_LENGTH * 2 : BMP_BYTES_LENGTH;
        }
    }

    /**
     *
     * @param bitmap
     * @param frame
     * @param posicion
     * @param compress
     */
    public static void readBitmap(BitMap bitmap, VariableByteBuffer frame, int posicion, boolean compress) {
        byte[] bmpTemp;
        int blockBmpLength;

        LogMF.debug(log, "Frame=[{0}]", frame);
        blockBmpLength = getBlockBmpLength(compress);
        bmpTemp = frame.getBytes(posicion, blockBmpLength);
        LogMF.debug(log, "BMP Primario=[{0}],HEXA=[{1}]", new String(bmpTemp), Converter.toHexaString(bmpTemp));
        BmpUtil.fillBitmap(bitmap, 0, bmpTemp, compress);
        log.trace("Bitmap primario leido:" + bitmap);
        posicion += blockBmpLength;
        if (bitmap.get(0)) {
            log.trace("Porcesando Bitmap secundario...");
            bmpTemp = frame.getBytes(posicion, blockBmpLength);
            LogMF.debug(log, "BMP Secundario=[{0}],HEXA=[{1}]", new String(bmpTemp), Converter.toHexaString(bmpTemp));
            BmpUtil.fillBitmap(bitmap, BMP_BITS_LENGTH, bmpTemp, compress);
        }
    }

    private static int getBlockBmpLength(boolean compress) {
        return compress ? BMP_BYTES_LENGTH / 2 : BMP_BYTES_LENGTH;
    }

    /**
     * Coloca los valores booleanos recibidos en {@code bitmap} segun los valores
     * recibidos en {@code bmp}, y lo realice a partire de la posicion indicada por el
     * {@code posInit}
     *
     * @param bitmap
     * @param posInit
     * @param bmpFrame
     */
    private static void fillBitmap(BitMap bitmap, int posInit, byte[] bmpFrame, boolean compress) {
        int size = getBlockBmpLength(compress);
        byte[] bmpByte;
        char[] bmpChar;
        String bmpStr;
        
        log.trace("Llenando Bitmap con size=[" + size + "], bytes=[" + Converter.toHexaString(bmpFrame) + "]");
        if (compress) {
            bmpByte = bmpFrame;
            log.trace("Procesando comprimido");
            for (int i = 0; i < size; i++) {
                bitmap.populateFrom(bmpByte[i], posInit + i * BitMap.BYTE_LENGTH);
            }
        } else {
            log.trace("Procesando ASCII");
            bmpStr = new String(bmpFrame);
            bmpChar = bmpStr.toCharArray();
            log.trace("Bytes a procesar:" + bmpStr);
            for (int i = 0; i < size; i++) {
                bitmap.populateFrom(bmpChar[i], posInit + i * BitMap.NIBBLE_LENGTH);
            }
        }
    }
}
