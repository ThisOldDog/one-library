package pers.dog.component.translate;

import java.util.List;

import pers.dog.boot.infra.dto.ValueMeaning;

/**
 * @author 废柴 2023/9/12 16:48
 */
public interface TranslateService {
    List<ValueMeaning> languages();

    default ValueMeaning defaultSourceLanguage() {
        for (ValueMeaning language : languages()) {
            if (language.getValue() != null && language.getValue().startsWith("en")) {
                return language;
            }
        }
        return null;
    }
    default ValueMeaning defaultTargetLanguage() {
        for (ValueMeaning language : languages()) {
            if (language.getValue() != null && language.getValue().startsWith("zh")) {
                return language;
            }
        }
        return null;
    }

    String translateMarkdown(String document, String sourceLanguage, String targetLanguage);
}
