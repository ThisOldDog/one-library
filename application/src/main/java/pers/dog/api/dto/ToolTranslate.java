package pers.dog.api.dto;

import pers.dog.infra.constant.TranslateServiceType;

/**
 * @author 废柴 2023/9/11 14:00
 */
public class ToolTranslate {
    private TranslateServiceType serviceType;
    private String apiKey;
    private String region;
    private String textTranslateEndpoint;
    private String documentTranslateEndpoint;

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

    public String getTextTranslateEndpoint() {
        return textTranslateEndpoint;
    }

    public ToolTranslate setTextTranslateEndpoint(String textTranslateEndpoint) {
        this.textTranslateEndpoint = textTranslateEndpoint;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public ToolTranslate setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getDocumentTranslateEndpoint() {
        return documentTranslateEndpoint;
    }

    public ToolTranslate setDocumentTranslateEndpoint(String documentTranslateEndpoint) {
        this.documentTranslateEndpoint = documentTranslateEndpoint;
        return this;
    }
}
