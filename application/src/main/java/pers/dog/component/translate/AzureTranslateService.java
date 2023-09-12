package pers.dog.component.translate;

import java.util.Collections;
import java.util.List;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.*;
import com.azure.core.credential.AzureKeyCredential;
import pers.dog.boot.infra.dto.ValueMeaning;

/**
 * @author 废柴 2023/9/12 16:49
 */
public class AzureTranslateService implements TranslateService {
    private final TextTranslationClient textClient;
    private final List<ValueMeaning> languages;

    public AzureTranslateService(String apiKey, String region, String textTranslateEndpoint) {
        textClient = new TextTranslationClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(textTranslateEndpoint)
                .region(region)
                .buildClient();
        languages = textClient.getLanguages(null, null, "zh-hans", null)
                .getTranslation()
                .entrySet()
                .stream()
                .map(entry -> new ValueMeaning().setValue(entry.getKey()).setMeaning(entry.getValue().getName()))
                .toList();
    }

    @Override
    public List<ValueMeaning> languages() {
        return languages;
    }

    @Override
    public String translateMarkdown(String document, String sourceLanguage, String targetLanguage) {
        List<TranslatedTextItem> translate = textClient.translate(Collections.singletonList(targetLanguage), Collections.singletonList(new InputTextItem(document)), null, sourceLanguage, TextType.HTML, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);
        return translate.get(0).getTranslations().get(0).getText();
    }
}
