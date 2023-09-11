package pers.dog.infra.constant;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/11 14:01
 */
public enum TranslateServiceType {
    AZURE_AI_TRANSLATE("Azure AI Translate");

    private final String productName;

    TranslateServiceType(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }
}
