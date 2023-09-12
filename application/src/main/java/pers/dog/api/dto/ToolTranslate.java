package pers.dog.api.dto;

import pers.dog.infra.constant.TranslateServiceType;

/**
 * @author 废柴 2023/9/11 14:00
 */
public class ToolTranslate {
    private TranslateServiceType serviceType;
    private String apiKey;
    private String endpoint;
    private String region;

    public TranslateServiceType getServiceType() {
        return serviceType;
    }

    public ToolTranslate setServiceType(TranslateServiceType serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public ToolTranslate setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public ToolTranslate setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public ToolTranslate setRegion(String region) {
        this.region = region;
        return this;
    }
}
