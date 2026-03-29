package com.bbva.orchestrator.core.logic.factory;

import com.bbva.orchestrator.core.dto.ISO8583;
import java.util.Map;

public interface NetworkDelegateFieldLogic {

    Map<String, String> applyLogicFields( Map<String, String> mapValues);

    Map<String, String> parseSubfields(ISO8583 iso8583);

}
