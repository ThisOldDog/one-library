package pers.dog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import pers.dog.boot.JavaFXSpringBootApplication;

/**
 * JavaFX App
 */
@SpringBootApplication
public class OneLibraryApplication extends JavaFXSpringBootApplication {
    public static void main(String[] args) {
        run(OneLibraryApplication.class, args);
    }
}