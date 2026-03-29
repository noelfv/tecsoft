package com.bbva.orchestrator.validations;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.gateway.dto.iso20022.ProcessingResultDTO;
import com.bbva.gateway.dto.iso20022.ResultDataDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationsErrorLocalTest {

	@Mock
	ISO20022 iso;
	@Mock
	ProcessingResultDTO processing;
	@Mock
	ResultDataDTO result;
	@InjectMocks
	ValidationsErrorLocal local;
	
	@Test
	void validationsLocalErr_OK() {
		when(iso.getProcessingResult()).thenReturn(processing);
		when(processing.getResultData()).thenReturn(result);
		ISO20022 response =  local.validationsLocalErr(iso, "validateFieldsISO");
		assertNotNull(response);
	}
	
	@Test
	void validationsLocalErr_default() {
		when(iso.getProcessingResult()).thenReturn(processing);
		when(processing.getResultData()).thenReturn(result);
		ISO20022 response =  local.validationsLocalErr(iso, "test");
		assertNotNull(response);
	}

}
