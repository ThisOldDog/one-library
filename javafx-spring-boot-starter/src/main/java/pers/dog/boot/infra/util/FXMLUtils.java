package pers.dog.boot.infra.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import pers.dog.boot.component.control.FXMLControlValueHandler;
import pers.dog.boot.context.ApplicationContextHolder;

public class FXMLUtils {
    private static final Logger logger = LoggerFactory.getLogger(FXMLUtils.class);

    public static class JavaFXSpringBootControllerLoader implements Callback<Class<?>, Object> {
        @Override
        public Object call(Class<?> type) {
            Constructor<?>[] constructors = type.getConstructors();
            Constructor<?> autowiredConstructor = null;
            Constructor<?> defaultConstructor = null;
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    autowiredConstructor = constructor;
                }
                if (constructor.getParameters() == null || constructor.getParameters().length == 0) {
                    defaultConstructor = constructor;
                }
            }
            if (autowiredConstructor != null) {
                return createInstance(type, autowiredConstructor);
            }
            if (defaultConstructor != null) {
                return createInstance(type, defaultConstructor);
            }
            if (constructors.length == 1) {
                return createInstance(type, constructors[0]);
            }
            return new LoadException("[FXMLoader] No matching constructor found: " + type);
        }

        private Object createInstance(Class<?> type, Constructor<?> constructor) {
            try {
                Parameter[] parameters = constructor.getParameters();
                if (parameters == null || parameters.length == 0) {
                    return constructor.newInstance();
                }
                ApplicationContext context = ApplicationContextHolder.getContext();
                Object[] parameterValues = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
                    if (qualifier != null) {
                        parameterValues[i] = context.getBean(qualifier.value());
                    } else {
                        parameterValues[i] = context.getBean(parameter.getType());
                    }
                }
                return constructor.newInstance(parameterValues);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                return new LoadException("[FXMLoader] An error occurred while creating an instance of the class : " + type + "#" + constructor, e);
            }
        }
    }

    public static class JavaFXSpringBootBuilderFactory implements BuilderFactory {
        @Override
        public Builder<?> getBuilder(Class<?> type) {
            ApplicationContext context = ApplicationContextHolder.getContext();
            if (context == null) {
                return null;
            }
            Map<String, ?> beanMap = context.getBeansOfType(type);
            if (beanMap.isEmpty()) {
                return null;
            }
            if (beanMap.size() > 2) {
                logger.error("[FXMLLoader] Load into multiple beans of the same type: {}", type);
                return null;
            }
            return (Builder<Object>) () -> beanMap.values().iterator().next();
        }
    }

    private static final Map<Parent, ?> PARENT_CONTROLLER_MAP = new WeakHashMap<>();

    private static final Set<String> bundleBaseNames = new HashSet<>();

    public static void addBundleBaseName(String baseName) {
        bundleBaseNames.add(baseName);
    }

    public static Parent loadFXML(String fxml) {
        return loadFXML(ApplicationContextHolder.getApplicationClass().getResource(fxml + ".fxml"));
    }

    public static Parent loadFXML(String fxml, Class<? extends Application> resourceHandler) {
        return loadFXML(resourceHandler.getResource(fxml + ".fxml"));
    }

    private static Parent loadFXML(URL url) {
        FXMLLoader fxmlLoader = new FXMLLoader(url, null, new JavaFXSpringBootBuilderFactory(), new JavaFXSpringBootControllerLoader());
        if (!bundleBaseNames.isEmpty()) {
            bundleBaseNames.forEach(baseName -> fxmlLoader.setResources(ResourceBundle.getBundle(baseName)));
        }
        try {
            Parent scene = fxmlLoader.load();
            PARENT_CONTROLLER_MAP.put(scene, fxmlLoader.getController());
            FXMLControlValueHandler.consumer(fxmlLoader.getController());
            return scene;
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setTitle("An error occurred when the scene load!");
            alert.setContentText(ExceptionUtils.getMessage(e));
            alert.showAndWait().ifPresent(response -> Platform.exit());
            return null;
        }
    }

    public static <T> T getController(Parent parent) {
        Object o = PARENT_CONTROLLER_MAP.get(parent);
        if (o instanceof Throwable) {
            throw new RuntimeException((Throwable) o);
        }
        return (T) o;
    }
}
