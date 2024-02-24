/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class CompareInternal {
    private static final Logger log = Logger.getLogger(CompareInternal.class);
    
    /**
     * Compara dos formatos internos bien tengas estos elementos anidados o no.
     * Dicha comparacion no espera que estos IF sean iguales, pero si espera que
     * todos los elementos o subelementos del FI esperado esten contenidos en el
     * FI a testear. Podemos decir entonces que IFExpected esta incluido en IFTest
     * @param IFTest El IF por comparar
     * @param IFExpected El IF esperado
     * @param nestedId Si estos IF's tienen provienen de otros, se puede agregar
     * el identificador anidado separados por punto. Por ejemplo:
     * ID#1.ID#2.ID#3...ID#N
     * @return Un objeto CompareInternalResult con los datos de la comparacion
     */
    public static CompareInternalResult compareInternalFormat(InternalFormat IFTest,
            InternalFormat IFExpected, String nestedId) {

        CompareInternalResult compResult = new CompareInternalResult();
        
        if((IFTest == null) || (IFExpected == null)){
            compResult.setSuccess(false);
            compResult.setReason(CompareInternalResult.MISSING_INTERNALFORMAT);
            return compResult;
        }
        
        compResult.setSuccess(true);
        compResult.setReason(CompareInternalResult.EXISTS_EQUAL);
        List<InternalField> listFields = IFExpected.getInternalFieldsAsList();

        for (InternalField intFieldExp : listFields) {
            InternalField intFieldTest = IFTest.getInternalField(intFieldExp.getId());
            if (intFieldTest == null) {
                compResult = setCompareFail(nestedId, intFieldExp,
                        CompareInternalResult.NOT_EXISTS);
                break;
            } else if (areDifferentsType(intFieldTest, intFieldExp)) {
                compResult = setCompareFail(nestedId, intFieldExp,
                        CompareInternalResult.TYPE_DIFFERENCES);
                break;
            } else if (areInternalFields(intFieldTest, intFieldExp)) {
                if (!intFieldExp.equals(intFieldTest)) {
                    compResult = setCompareFail(nestedId, intFieldExp,
                            CompareInternalResult.EXISTS_NOT_EQUAL);
                    break;
                }
            } else {
                compResult = compareInternalFormat((InternalFormat) intFieldTest,
                        (InternalFormat) intFieldExp, makeNestedId(nestedId, intFieldExp));
                if (!compResult.isSuccess()) {
                    break;
                }
            }
        }

        return compResult;
    }

    private static boolean areDifferentsType(InternalField intFieldTest, InternalField intFieldExp) {
        return ((intFieldTest instanceof InternalFormat) && !(intFieldExp instanceof InternalFormat))
                || (!(intFieldTest instanceof InternalFormat) && (intFieldExp instanceof InternalFormat));
    }

    private static boolean areInternalFields(InternalField intFieldTest, InternalField intFieldExp) {
        return (!(intFieldTest instanceof InternalFormat) && !(intFieldExp instanceof InternalFormat));
    }

    private static CompareInternalResult setCompareFail(String nestedID,
            InternalField intFieldExp, String reason) {
        nestedID = makeNestedId(nestedID, intFieldExp);
        CompareInternalResult compResult = new CompareInternalResult();
        compResult.setReason(reason);
        compResult.setFieldID(nestedID);
        compResult.setSuccess(false);

        return compResult;
    }
    
    private static String makeNestedId(String nestedId, InternalField intFieldExp){
        return (nestedId == null) ? intFieldExp.getId() : nestedId + "." + intFieldExp.getId();
    }
}
