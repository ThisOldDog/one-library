package pers.dog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import pers.dog.boot.JavaFXSpringBootApplication;

/**
 * JavaFX App
 */
@EnableScheduling
@SpringBootApplication
public class OneLibraryApplication extends JavaFXSpringBootApplication {

    public static void main(String[] args) {
        run(OneLibraryApplication.class, args);
    }
}