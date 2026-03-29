package com.bbva.orchestrator.validations.enums;

import com.bbva.orchestrator.validations.ValidationsLocal;
import lombok.Getter;

@Getter
public enum ConfigMessageFunctions {
    REQUIRED_0200("PAGO", ValidationsLocal.REQUIRED),
    OPTIONAL_0200("PAGO", ValidationsLocal.OPTIONAL),
    REQUIRED_0210("0210", ValidationsLocal.REQUIRED),
    OPTIONAL_0210("0210", ValidationsLocal.OPTIONAL),
    REQUIRED_0220("0220", ValidationsLocal.REQUIRED),
    OPTIONAL_0220("0220", ValidationsLocal.OPTIONAL),
    REQUIRED_0221("0221", ValidationsLocal.REQUIRED),
    REQUIRED_0230("0230", ValidationsLocal.REQUIRED),
    OPTIONAL_0230("0230", ValidationsLocal.OPTIONAL),
    REQUIRED_0420("0420", ValidationsLocal.REQUIRED),
    OPTIONAL_0420("0420", ValidationsLocal.OPTIONAL),
    REQUIRED_0421("0421", ValidationsLocal.REQUIRED),
    OPTIONAL_0421("0421", ValidationsLocal.OPTIONAL),
    REQUIRED_0430("0430", ValidationsLocal.REQUIRED),
    OPTIONAL_0430("0430", ValidationsLocal.OPTIONAL);

    private final String messageFunction;
    private final String fieldRequired;

    ConfigMessageFunctions(String messageFunction, String fieldRequired) {
        this.messageFunction = messageFunction;
        this.fieldRequired = fieldRequired;
    }
}