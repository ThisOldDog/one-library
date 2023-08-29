package pers.dog;

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
        run(OneLibraryApplication.class, args);
    }
}