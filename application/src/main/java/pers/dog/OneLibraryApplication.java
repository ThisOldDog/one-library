package pers.dog;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import pers.dog.boot.JavaFXSpringBootApplication;

/**
 * JavaFX App
 */
@SpringBootApplication
public class OneLibraryApplication extends JavaFXSpringBootApplication {

    public static void main(String[] args) throws IOException {
        run(OneLibraryApplication.class, args);
    }
}