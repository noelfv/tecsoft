package com.bbva.orchestrator.validations;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchlib.validations.CheckValidations;
import com.bbva.orchlib.validations.IValidationsLocal;
import org.springframework.stereotype.Component;

@Component
public class ValidationsLocal implements IValidationsLocal {

    public static final String REQUIRED = "REQUIRED";
    public static final String OPTIONAL = "OPTIONAL";
    public static final String NUMBER = "number";
    public static final String ALPHA_NUMERIC = "alphanumeric";
    public static final String ALPHA_NUMERIC_SPECIAL = "alphanumericSpecial";

    private final CheckValidations checkValidations;

    public ValidationsLocal(CheckValidations checkValidations) {
        this.checkValidations = checkValidations;
    }

    @Override
    public boolean validationsLocal(ISO20022 iso20022) {
        return checkValidations.checkValidationsLocal(iso20022, this);
    }

    public boolean validateFieldsISO(ISO20022 iso20022) {
        iso20022.getMessageFunction();
        return true;
    }

}