package com.bbva.orchestrator.validations;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchlib.validations.CheckValidations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ValidationsLocalTest {

    @Mock
    CheckValidations checkValidations;
    @Mock
    ISO20022 iso20022;

    @Mock
    TransactionDTO transaction;

    @Mock
    TransactionAmountsDTO amounts;

    @Mock
    AccountFromDTO account;

    @Mock
    TransactionAmountDTO amount;

    @Mock
    AccountToDTO accountto;

    @Mock
    ReconciliationAmountDTO reconciliation;

    @Mock
    TransactionIdDTO transactionId;

    @Mock
    CardholderBillingAmountDTO cardholder;

    @Mock
    TransactionContextDTO tcontext;

    @Mock
    ContextDTO context;

    @Mock
    SettlementServiceDTO setlement;

    @Mock
    SettlementServiceDatesDTO settlementdates;

    @Mock
    PointOfServiceContextDTO pointofservice;

    @Mock
    EnvironmentDTO enviroment;

    @Mock
    AcquirerDTO acquirer;

    @Mock
    CardDTO card;

    @Mock
    ProcessingResultDTO processing;

    @Mock
    ResultDataDTO result;

    @Mock
    TerminalDTO terminal;

    @Mock
    TerminalIdDTO terminalId;

    @Mock
    AcceptorDTO acceptor;

    @Mock
    SenderDTO sender;

    @Mock
    AdditionalIdDTO additionalId;

    @Mock
    SecurityTrailerDTO trailer;

    @Mock
    MacDataDTO macdata;

    @Mock
    AdditionalAmountDTO aditional;

    @Mock
    AdditionalDataDTO aditionalData;

    @Mock
    SaleContextDTO sale;

    @Mock
    IssuerDTO issuer;

    @Mock
    TraceDataDTO trace;

    @Mock
    OriginalDataElementsDTO original;

    @InjectMocks
    ValidationsLocal local;

    private MockedStatic<LogsTraces> logsTracesMock;

    @BeforeEach
    void setup() {
        logsTracesMock = mockStatic(LogsTraces.class);
    }

    @AfterEach
    void tearDown() {
        logsTracesMock.close();
    }

    @Test
    void validationsLocal() {
        boolean validation = local.validationsLocal(iso20022);
        assertFalse(validation);
    }

    @Test
    void validateFieldsIso() {
        boolean response = local.validateFieldsISO(iso20022);
        assertTrue(response);
    }

}