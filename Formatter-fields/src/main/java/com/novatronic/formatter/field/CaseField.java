package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldException;
import com.novatronic.formatter.field.util.IdItem;
import com.novatronic.formatter.field.util.IdParser;
import com.novatronic.formatter.field.util.KeyBuilder;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Case Field
 *
 */
public class CaseField extends Field {

    private static final Logger log = Logger.getLogger(CaseField.class);
    private String caseFields;
    private IdItem[] ids;
    private final Map<String, List<Field>> cases;
    private final List<Field> defaultCase;
    private boolean existDefaultCase;
    private int referenceUp;
    private List<Element> tempElemCases;
    private final List<String> tempKeys;
    private List<Element> tempCaseFields;

    private static class Attr {

        private static final String CASE_FIELDS = "casefields";
        private static final String VALUE = "value";//Para cada case
        private static final String REF_UP = "refUp";
    }

    private static class Tag {

        private static final String CASE = "case";
        private static final String FIELD = "field";
        private static final String DEFAULT = "default";
    }

    public CaseField() {
        tempKeys = new ArrayList<String>();
        cases = new TreeMap<String, List<Field>>();
        defaultCase = new ArrayList<Field>();
    }

    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        List<Field> FieldsCase;
        InternalFormat intFmt;
        String keyCase;

        LogMF.trace(log, "{0}Creando intFormat por omision con param={1}", logId, getKeyParam());
        intFmt = new InternalFormat(getId());
        intFmtCtx.addInternalField(intFmt);
        //intFmt.setParent(intFmtCtx);
        keyCase = makeKeyCase(params, intFmtCtx);
        LogMF.trace(log, "{0}KeyCase creado=[{1}]", logId, keyCase);

        if ((keyCase != null) && (cases.get(keyCase) != null)) {
            log.trace(logId + "Creando IntFmt por omision a partir del case recibido");
            FieldsCase = cases.get(keyCase);
            LogMF.trace(log, "{0}DefaultKeyCase=[{1}], FieldsCase.isNull?={2}", logId, keyCase,
                    FieldsCase == null);
            log.trace(logId + "Se encontro la clave, se procede a crear el FI");
            for (Field field : FieldsCase) {
                if (!field.isNullable()) {
                    intFmt.addInternalField(field.getValueAsInternalField(params, intFmt));
                }
            }
        } else if (existDefaultCase) {
            log.debug("No se encontro la clave, se procesa defaultCase");
            for (Field field : defaultCase) {
                if (!field.isNullable()) {
                    intFmt.addInternalField(field.getValueAsInternalField(params, intFmt));
                }
            }
        }

        return intFmt;
    }

    private String makeKeyCase(Properties params, InternalFormat intFmtCtx) {
        String keyCase;

        keyCase = getParamValue(params, intFmtCtx);
        keyCase = (keyCase != null)
                ? keyCase
                : KeyBuilder.generateKeyCase(getReferenceIntFormat(intFmtCtx), ids);

        return keyCase;
    }

    @Override
    protected void readCustomConfiguration(Element element) {
        try {
            caseFields = element.getAttributeValue(Attr.CASE_FIELDS);
            referenceUp = Integer.parseInt(
                    (element.getAttributeValue(Attr.REF_UP) == null
                    ? "-1"
                    : element.getAttributeValue(Attr.REF_UP)));
            ids = IdParser.parseIds(caseFields);
            tempElemCases = element.getChildren(Tag.CASE);
            for (Element elemKeyCase : tempElemCases) {
                processKeyCase(elemKeyCase);
            }
            log.debug(logId + "cases=" + cases);

            processDefaultCase(element.getChild(Tag.DEFAULT));
            log.debug(logId + "defaultCase=" + defaultCase);
            log.trace(toString());
        } catch (Exception ex) {
            throw new FieldConfigurationException("[" + this.getId() + "]Error en la configuracion", ex);
        }
    }

    private void processKeyCase(Element elemKeyCase) {
        tempCaseFields = elemKeyCase.getChildren(Tag.FIELD);
        log.debug(logId + "case-value=" + elemKeyCase.getAttributeValue(Attr.VALUE));

        tempKeys.add(elemKeyCase.getAttributeValue(Attr.VALUE));
        if (!tempCaseFields.isEmpty()) {
            List<Field> tempFieldKeys = new ArrayList<Field>();
            for (Element elemField : tempCaseFields) {
                Field field = FieldFactory.getField(elemField, this);
                tempFieldKeys.add(field);
            }
            for (String key : tempKeys) {
                cases.put(key, tempFieldKeys);
            }
            tempKeys.clear();
        }
    }

    private void processDefaultCase(Element elemDefault) {
        if (elemDefault != null) {
            tempCaseFields = elemDefault.getChildren(Tag.FIELD);
            for (Element elemField : tempCaseFields) {
                Field field = FieldFactory.getField(elemField, this);
                defaultCase.add(field);
            }
            existDefaultCase = true;
        } else {
            existDefaultCase = false;
        }
    }

    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        InternalFormat innerIntFormat = internalFormat.getIFmt(this.getId());

        try {
            if (innerIntFormat == null) {
                throw new FieldException("No se encuentra el formato interno, id="
                        + this.getId());
            }
            String keyCase = KeyBuilder.generateKeyCase(getReferenceIntFormat(internalFormat), ids);
            List<Field> tempFieldKeys = (keyCase == null) ? null : cases.get(keyCase);

            int bytesWritten = 0;
            if (tempFieldKeys != null) {
                for (Field field : tempFieldKeys) {
                    bytesWritten += field.putBytes(innerIntFormat, frame);
                }
            } else if (!existDefaultCase) {
                throw new FieldException("No existe un caso para la clave=" + keyCase);
            } else {
                for (Field field : defaultCase) {
                    bytesWritten += field.putBytes(innerIntFormat, frame);
                }
            }
            return bytesWritten;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al generar la trama", ex);
        }
    }

    /**
     * THOT: Esto ya podría llevarse a otro lugar como funcionalidad pues sería mas
     * practico tenerlo en el FormatoInterno en lugar de aca.
     *
     * @param internalFormat
     * @return
     */
    private InternalFormat getReferenceIntFormat(InternalFormat internalFormat) {
        InternalFormat newIntFormat = internalFormat;
        int currentUpLevel = 0;

        log.trace(logId + "Recibimos intFormat [id=" + internalFormat.getId() + ", path=" + internalFormat.getPath() + "]");
        if (referenceUp == -1) {
            log.trace(logId + "referenceUp=" + referenceUp + ". Retornando ParentRoot");
            newIntFormat = internalFormat.getParentRoot();
        } else {
            log.trace(logId + "referenceUp=" + referenceUp + ". Subiendo niveles");
            while (currentUpLevel != referenceUp) {
                if (newIntFormat.isParentRoot()) {
                    log.warn(logId + "Se alcanzo el ROOT subiendo " + currentUpLevel + " niveles de "
                            + referenceUp + " solicitados");
                    break;
                } else {
                    newIntFormat = newIntFormat.getParent();
                    currentUpLevel++;
                }
            }
        }
        log.trace(logId + "Devolvemos intFormat [id=" + internalFormat.getId() + ", path=" + internalFormat.getPath() + "]");

        return newIntFormat;
    }
    
    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        String keyCase;
        List<Field> tempFieldKeys;
        InternalFormat innerIntFormat;

        try {
            keyCase = KeyBuilder.generateKeyCase(getReferenceIntFormat(internalFormat), ids);
            tempFieldKeys = (keyCase == null) ? null : cases.get(keyCase);
            innerIntFormat = new InternalFormat(this.getId());
            internalFormat.addInternalField(innerIntFormat);

            if (tempFieldKeys != null) {
                for (Field field : tempFieldKeys) {
                    posicion = field.readBytes(innerIntFormat, frame, posicion);
                }
            } else if (!existDefaultCase) {
                throw new FieldException(logId + "No existe un caso para la clave=" + keyCase);
            } else {
                for (Field field : defaultCase) {
                    posicion = field.readBytes(innerIntFormat, frame, posicion);
                }
            }

            return posicion;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al leer la trama", ex);
        }
    }

    @Override
    public String toString() {
        return "CaseField{"
                + "caseFields=" + caseFields
                + ", ids=" + ids
                + ", cases=" + cases
                + ", defaultCase=" + defaultCase
                + ", existDefaultCase=" + existDefaultCase
                + ", referenceUp=" + referenceUp
                + ", tempElemCases=" + tempElemCases
                + ", tempKeys=" + tempKeys
                + ", tempCaseFields=" + tempCaseFields + '}';
    }
}
