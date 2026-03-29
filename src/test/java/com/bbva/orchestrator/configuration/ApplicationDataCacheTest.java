package com.bbva.orchestrator.configuration;

import com.bbva.orchlib.configuration.BusinessDataLoad;
import com.bbva.orchlib.configuration.preloaddto.businessdata.BusinessData;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Bin;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Currency;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Custom;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Fields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // IMPORTANTE: Agregado

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDataCacheTest {

    @Mock
    private BusinessDataLoad businessDataLoad;

    @InjectMocks
    private ApplicationDataCache applicationDataCache;

    private BusinessData data;

    @BeforeEach
    void setUp() {
        data = new BusinessData();

        Currency usdCurrency = new Currency();
        usdCurrency.setNumericCurrencyId("840");
        usdCurrency.setCurrencyCode("001");
        Currency mxnCurrency = new Currency();
        mxnCurrency.setNumericCurrencyId("484");
        mxnCurrency.setCurrencyCode("002");
        data.setCurrency(List.of(usdCurrency, mxnCurrency));

        Bin bin1 = new Bin();
        bin1.setKey("network1");
        Fields field1 = new Fields();
        field1.setName("bin123");
        field1.setValue("Description 123");
        bin1.setFields(List.of(field1));

        Bin bin2 = new Bin();
        bin2.setKey("network2");
        Fields field2 = new Fields();
        field2.setName("bin456");
        field2.setValue("Description 456");
        bin2.setFields(List.of(field2));
        data.setBins(List.of(bin1, bin2));

        Custom custom1 = new Custom();
        custom1.setName("customKey1");
        custom1.setValue("customValue1");
        Custom custom2 = new Custom();
        custom2.setName("customKey2");
        custom2.setValue("customValue2");
        data.setCustom(Map.of("section1", List.of(custom1), "section2", List.of(custom2)));

        when(businessDataLoad.getData()).thenReturn(data);
        applicationDataCache.init(); // Esto llena los mapas estáticos de forma natural
    }

    @Test
    void testGetCurrencyCode_WithValidId() {
        assertEquals("001", applicationDataCache.getCurrencyCode("840"));
        assertEquals("840", applicationDataCache.getCurrencyCode("001"));
    }

    @Test
    void testGetCurrencyCode_WithInvalidId() {
        assertEquals("999", applicationDataCache.getCurrencyCode("999"));
    }

    @Test
    void testGetCurrencyCode_Bidirectional_AllCurrencies() {
        assertEquals("001", applicationDataCache.getCurrencyCode("840"));
        assertEquals("840", applicationDataCache.getCurrencyCode("001"));
        assertEquals("002", applicationDataCache.getCurrencyCode("484"));
        assertEquals("484", applicationDataCache.getCurrencyCode("002"));
    }


    @Test
    void testGetBinDescription_WithValidNetworkAndKey() {
        assertEquals("Description 123", applicationDataCache.getBinDescription("network1", "bin123"));
    }

    @Test
    void testGetBinDescription_WithInvalidNetwork() {
        assertEquals("bin789", applicationDataCache.getBinDescription("nonExistentNetwork", "bin789"));
    }

    @Test
    void testGetBinDescription_WithInvalidKey() {
        assertEquals("BIN_NO_BBVA", applicationDataCache.getBinDescription("network1", "nonExistentBin"));
    }

    @Test
    void testGetBinDescription_WithValidNetworkAndNullValueField() {
        Bin bin = new Bin();
        bin.setKey("networkNull");
        Fields fieldNull = new Fields();
        fieldNull.setName("binNull");
        fieldNull.setValue(null);
        bin.setFields(List.of(fieldNull));
        data.setBins(List.of(bin));
        when(businessDataLoad.getData()).thenReturn(data);
        applicationDataCache.init();

        assertEquals("binNull", applicationDataCache.getBinDescription("networkNull", "binNull"));
    }

    @Test
    void testGetCustomValue_WithValidSectionAndName() {
        assertEquals("customValue1", applicationDataCache.getCustomValue("section1", "customKey1"));
        assertEquals("customValue2", applicationDataCache.getCustomValue("section2", "customKey2"));
    }

    @Test
    void testGetCustomValue_WithInvalidSection() {
        assertNull(applicationDataCache.getCustomValue("nonExistentSection", "anyKey"));
    }

    @Test
    void testGetCustomValue_WithInvalidName() {
        assertNull(applicationDataCache.getCustomValue("section1", "nonExistentName"));
    }

    @Test
    void getCustomValue_WhenCustomDataIsNull_ReturnsNull() {
        ReflectionTestUtils.setField(ApplicationDataCache.class, "customData", null);
        assertNull(applicationDataCache.getCustomValue("section1", "customKey1"));
    }

    @Test
    void testInit_WithNullCustomData_GetCustomValueReturnsNull() {
        data.setCustom(null);
        when(businessDataLoad.getData()).thenReturn(data);
        applicationDataCache.init();

        assertNull(applicationDataCache.getCustomValue("section1", "customKey1"));
    }

    @Test
    void testInit_WithMultipleFieldsPerBin_AllFieldsLoaded() {
        Bin binMulti = new Bin();
        binMulti.setKey("networkMulti");
        Fields f1 = new Fields(); f1.setName("bin001"); f1.setValue("Desc001");
        Fields f2 = new Fields(); f2.setName("bin002"); f2.setValue("Desc002");
        Fields f3 = new Fields(); f3.setName("bin003"); f3.setValue("Desc003");
        binMulti.setFields(List.of(f1, f2, f3));
        data.setBins(List.of(binMulti));
        when(businessDataLoad.getData()).thenReturn(data);
        applicationDataCache.init();

        assertEquals("Desc001", applicationDataCache.getBinDescription("networkMulti", "bin001"));
        assertEquals("Desc002", applicationDataCache.getBinDescription("networkMulti", "bin002"));
        assertEquals("Desc003", applicationDataCache.getBinDescription("networkMulti", "bin003"));
    }

    @Test
    void testInit_WithMultipleCustomsPerSection_AllValuesLoaded() {
        Custom c1 = new Custom(); c1.setName("key1"); c1.setValue("val1");
        Custom c2 = new Custom(); c2.setName("key2"); c2.setValue("val2");
        Custom c3 = new Custom(); c3.setName("key3"); c3.setValue("val3");
        data.setCustom(Map.of("sectionA", List.of(c1, c2, c3)));
        when(businessDataLoad.getData()).thenReturn(data);
        applicationDataCache.init();

        assertEquals("val1", applicationDataCache.getCustomValue("sectionA", "key1"));
        assertEquals("val2", applicationDataCache.getCustomValue("sectionA", "key2"));
        assertEquals("val3", applicationDataCache.getCustomValue("sectionA", "key3"));
    }
}

