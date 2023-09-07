package pers.dog;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pers.dog.boot.JavaFXSpringBootApplication;
import pers.dog.config.OneLibraryProperties;

/**
 * JavaFX App
 */
@EnableConfigurationProperties(OneLibraryProperties.class)
@SpringBootApplication
public class OneLibraryApplication extends JavaFXSpringBootApplication {
    public static void main(String[] args) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(
                "Hello",
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage("zh"));
        System.out.println(translation.getTranslatedText());
//        run(OneLibraryApplication.class, args);
    }
}