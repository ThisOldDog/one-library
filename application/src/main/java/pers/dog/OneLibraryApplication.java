package pers.dog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pers.dog.boot.JavaFXSpringBootApplication;
import pers.dog.config.OneLibraryProperties;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * JavaFX App
 */
@EnableConfigurationProperties(OneLibraryProperties.class)
@SpringBootApplication
public class OneLibraryApplication extends JavaFXSpringBootApplication {
    private static final Logger logger = LoggerFactory.getLogger(OneLibraryApplication.class);

    public static void main(String[] args) {
        run(OneLibraryApplication.class, args);
    }
}