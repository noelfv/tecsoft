/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class CompareFrame {
    private static final Logger log = Logger.getLogger(CompareFrame.class);
    
    /**
     * Este metodo compara dos tramas. Devuelve como resultado en una clase {@link 
     * CompareFrameResult}. Apartir de esta clase puede obtener la razon
     * @param frameTested
     * @param frameExpected
     * @return 
     */
    public static CompareFrameResult compareFrames(VariableByteBuffer frameTested, VariableByteBuffer frameExpected){
        CompareFrameResult compResult = new CompareFrameResult();
        int length;
        
        if((frameExpected == null) || (frameTested == null)){
            compResult.setSuccess(false);
            compResult.setReason(CompareFrameResult.FRAME_MISSING);
            return compResult;
        }
        
        compResult.setExpectedSize(frameExpected.getLength());
        compResult.setTestedSize(frameTested.getLength());
        if(frameExpected.getLength() < frameTested.getLength()){
            compResult.setSuccess(false);
            compResult.setReason(CompareFrameResult.EXPECTED_INCLUDE_IN_TESTED);
            length = frameExpected.getLength();
        }else if(frameExpected.getLength() > frameTested.getLength()){
            compResult.setSuccess(false);
            compResult.setReason(CompareFrameResult.TESTED_INCLUDE_IN_EXPECTED);
            length = frameTested.getLength();
        }else{
            compResult.setSuccess(true);
            compResult.setReason(CompareFrameResult.EQUALS);
            length = frameTested.getLength();   //son iguales, cualquiera vale
        }
        
        for (int i = 0; i < length; i++) {
            byte byteExpected = frameExpected.get(i);
            byte byteTested = frameTested.get(i);
            if (byteExpected != byteTested){
                compResult.setSuccess(false);
                compResult.setReason(CompareFrameResult.DIFFERENTS_PART);
                compResult.setPosition(i);
                break;
            }
        }
        return compResult;
    }
}
