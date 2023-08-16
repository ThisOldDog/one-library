package pers.dog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pers.dog.boot.JavaFXSpringBootApplication;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.config.OneLibraryProperties;

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

    @Override
    protected void onRunning(Stage stage) throws IOException {
        copyResource("lib");
        copyResource("style");
        super.onRunning(stage);
    }

    private void copyResource(String name) throws IOException {
        startingStepProperty.setValue(String.format("Loading %s ...", name));
        ApplicationDirFileOperationHandler handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/" + name));
        Path targetLibPath = handler.directory();
        Path sourceLibPath = getClassPath("static/" + name);
        Files.walkFileTree(sourceLibPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path target = targetLibPath.resolve(sourceLibPath.relativize(file));
                Path targetDirectory = target.getParent();
                if (!Files.exists(targetDirectory)) {
                    Files.createDirectories(targetDirectory);
                }
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return super.visitFile(file, attrs);
            }
        });
    }

    private Path getClassPath(String path) {
        try {
            return Path.of(OneLibraryApplication.class.getClassLoader().getResource(path).toURI());
        } catch (URISyntaxException e) {
            logger.error("[Loading] Unable to load " + path, e);
            throw new IllegalStateException(e);
        }
    }
}