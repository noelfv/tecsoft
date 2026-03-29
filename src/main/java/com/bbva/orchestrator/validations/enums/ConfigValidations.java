package com.bbva.orchestrator.validations.enums;

import com.bbva.orchestrator.validations.ValidationsLocal;
import lombok.Getter;

@Getter
public enum ConfigValidations {
    TRANSACTION_TRANSACTIONTYPE(new ConfigMessageFunctions[]{//03
            ConfigMessageFunctions.REQUIRED_0200,
            ConfigMessageFunctions.REQUIRED_0210,
            ConfigMessageFunctions.REQUIRED_0220,
            ConfigMessageFunctions.REQUIRED_0221,
            ConfigMessageFunctions.REQUIRED_0230,
            ConfigMessageFunctions.REQUIRED_0420,
            ConfigMessageFunctions.REQUIRED_0421,
            ConfigMessageFunctions.REQUIRED_0430
    }, ValidationsLocal.NUMBER),
    TRANSACTION_AMOUNT(new ConfigMessageFunctions[]{//04
            ConfigMessageFunctions.REQUIRED_0200,
            ConfigMessageFunctions.REQUIRED_0210,
            ConfigMessageFunctions.REQUIRED_0220,
            ConfigMessageFunctions.REQUIRED_0221,
            ConfigMessageFunctions.REQUIRED_0230,
            ConfigMessageFunctions.REQUIRED_0420,
            ConfigMessageFunctions.REQUIRED_0421,
            ConfigMessageFunctions.REQUIRED_0430
    }, ValidationsLocal.NUMBER),
    TRACK_2_DATA(new ConfigMessageFunctions[]{// 35
            ConfigMessageFunctions.REQUIRED_0200,
            ConfigMessageFunctions.REQUIRED_0210,
            ConfigMessageFunctions.REQUIRED_0220,
            ConfigMessageFunctions.REQUIRED_0221,
            ConfigMessageFunctions.OPTIONAL_0230,
            ConfigMessageFunctions.REQUIRED_0420,
            ConfigMessageFunctions.REQUIRED_0421,
            ConfigMessageFunctions.REQUIRED_0430
    }, ValidationsLocal.ALPHA_NUMERIC_SPECIAL),
    RETRIVAL_REFERENCE_NUMBER(new ConfigMessageFunctions[]{// 37
            ConfigMessageFunctions.REQUIRED_0200,
            ConfigMessageFunctions.REQUIRED_0210,
            ConfigMessageFunctions.REQUIRED_0220,
            ConfigMessageFunctions.REQUIRED_0221,
            ConfigMessageFunctions.REQUIRED_0230,
            ConfigMessageFunctions.REQUIRED_0420,
            ConfigMessageFunctions.REQUIRED_0421,
            ConfigMessageFunctions.REQUIRED_0430
    }, ValidationsLocal.ALPHA_NUMERIC),
    ACCOUNT_2_IDENTIFICATION(new ConfigMessageFunctions[]{// 103
            ConfigMessageFunctions.OPTIONAL_0200,
            ConfigMessageFunctions.OPTIONAL_0210,
            ConfigMessageFunctions.OPTIONAL_0220,
            ConfigMessageFunctions.OPTIONAL_0230,
            ConfigMessageFunctions.OPTIONAL_0420,
            ConfigMessageFunctions.OPTIONAL_0421,
            ConfigMessageFunctions.OPTIONAL_0430
    }, ValidationsLocal.ALPHA_NUMERIC_SPECIAL);

    private final ConfigMessageFunctions[] required;
    private final String format;


    ConfigValidations(ConfigMessageFunctions[] required, String format) {
        this.required = required;
        this.format = format;
    }

}
