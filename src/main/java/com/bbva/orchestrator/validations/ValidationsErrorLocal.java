package com.bbva.orchestrator.validations;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchlib.validations.IValidationsLocalErr;
import org.springframework.stereotype.Component;

@Component
public class ValidationsErrorLocal implements IValidationsLocalErr {
    private static final String ERROR_RESULT = "Error";

    @Override
    public ISO20022 validationsLocalErr(ISO20022 iso20022, String validationName) {
        iso20022.getProcessingResult().getResultData().setResult(ERROR_RESULT);
        return iso20022;
    }

}
