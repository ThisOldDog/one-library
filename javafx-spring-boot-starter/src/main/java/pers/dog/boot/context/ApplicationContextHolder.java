package pers.dog.boot.context;

import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import pers.dog.boot.JavaFXSpringBootApplication;
import pers.dog.boot.context.property.ApplicationProperties;

/**
 * @author 废柴 2021/6/3 19:16
 */
public class ApplicationContextHolder {
    private static Class<? extends JavaFXSpringBootApplication> applicationClass;
    private static String[] args;
    private static ResourceLoader resourceLoader = new DefaultResourceLoader();
    private static ApplicationProperties applicationProperties = new ApplicationProperties();
    private static ApplicationContext context;
    private static Stage stage;

    public static Class<? extends JavaFXSpringBootApplication> getApplicationClass() {
        return applicationClass;
    }

    public static void setApplicationClass(Class<? extends JavaFXSpringBootApplication> applicationClass) {
        ApplicationContextHolder.applicationClass = applicationClass;
        ApplicationContextHolder.resourceLoader = new DefaultResourceLoader(applicationClass.getClassLoader());
    }

    public static String[] getArgs() {
        return args;
    }

    public static void setArgs(String[] args) {
        ApplicationContextHolder.args = args;
    }

    public static ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public static void setResourceLoader(ResourceLoader resourceLoader) {
        ApplicationContextHolder.resourceLoader = resourceLoader;
    }

    public static void setContext(ApplicationContext context) {
        ApplicationContextHolder.context = context;
        ApplicationContextHolder.applicationProperties = context.getBean(ApplicationProperties.class);
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        ApplicationContextHolder.stage = stage;
    }

    public static ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }
}
