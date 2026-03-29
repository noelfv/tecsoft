package com.bbva.orchestrator.configuration;

import com.bbva.orchlib.configuration.BusinessDataLoad;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Bin;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Currency;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Custom;
import com.bbva.orchlib.configuration.preloaddto.businessdata.Fields;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ApplicationDataCache {

    private final BusinessDataLoad businessDataLoad;
    private static Map<String, String> inputCurrencyId;
    private static Map<String, Map<String, String>> inputBinesByNetwork;
    private static Map<String, Map<String, String>> customData;

    public ApplicationDataCache(BusinessDataLoad businessDataLoad) {
        this.businessDataLoad = businessDataLoad;
    }

    @PostConstruct
    public void init() {
        inputCurrencyId = createMapBiderectionalByCurrencyCode(businessDataLoad.getData().getCurrency());
        inputBinesByNetwork= createListBines(businessDataLoad.getData().getBins());
        customData = createCustom(businessDataLoad.getData().getCustom());
    }

    public String getCurrencyCode(String currencyId) {
        String code = inputCurrencyId.get(currencyId);
        return code != null ? code : currencyId;
    }

    public String getBinDescription(String network, String key) {
        Map<String, String> map = inputBinesByNetwork.get(network);

        if (map == null) {
            return key;
        }

        if(!map.containsKey(key)){
            return "BIN_NO_BBVA";
        }

        String description = map.get(key);
        return description != null ? description : key;
    }

    public String getCustomValue(String section, String key) {
        if (customData != null && customData.containsKey(section)) {
            return customData.get(section).get(key);
        }
        return null;
    }

    private Map<String, String> createMapBiderectionalByCurrencyCode(List<Currency> currencyList) {
        Map<String, String> currencyMap = new ConcurrentHashMap<>();
        for (Currency currency : currencyList) {
            currencyMap.put(currency.getNumericCurrencyId(), currency.getCurrencyCode());
            currencyMap.put(currency.getCurrencyCode(), currency.getNumericCurrencyId());
        }
        return currencyMap;
    }


    private Map<String, Map<String, String>> createCustom(Map<String, List<Custom>> mapCustom) {
        Map<String, Map<String, String>> result = new ConcurrentHashMap<>();
        if (mapCustom == null) {
            return result;
        }
        for (Map.Entry<String, List<Custom>> entry : mapCustom.entrySet()) {
            String sectionKey = entry.getKey();
            List<Custom> customList = entry.getValue();
            Map<String, String> sectionMap = customList.stream()
                    .collect(Collectors.toMap(Custom::getName, Custom::getValue));
            result.put(sectionKey, sectionMap);
        }
        return result;
    }

    private Map<String, Map<String, String>> createListBines(List<Bin> binList) {
        Map<String, Map<String, String>> result = new ConcurrentHashMap<>();
        for (Bin bin : binList) {
            Map<String, String> fieldsMap = new HashMap<>();
            for (Fields field : bin.getFields()) {
                fieldsMap.put(field.getName(), field.getValue());
            }
            result.put(bin.getKey(), fieldsMap);
        }
        return result;
    }

}