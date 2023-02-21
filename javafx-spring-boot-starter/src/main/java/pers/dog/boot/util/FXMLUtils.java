package pers.dog.boot.util;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import pers.dog.boot.context.ApplicationContextHolder;
import pers.dog.boot.i18n.I18nMessageSource;

public class FXMLUtils {
    private static final Set<String> bundleBaseNames = new HashSet<>();

    public static void addBundleBaseName(String baseName) {
        bundleBaseNames.add(baseName);
    }

    public static Parent loadFXML(String fxml) throws IOException {
        return loadFXML(ApplicationContextHolder.getApplicationClass().getResource(fxml + ".fxml"));
    }

    public static Parent loadFXML(String fxml, Class<? extends Application> resourceHandler) throws IOException {
        return loadFXML(resourceHandler.getResource(fxml + ".fxml"));
    }

    private static Parent loadFXML(URL url) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        if (!bundleBaseNames.isEmpty()) {
            bundleBaseNames.forEach(baseName -> fxmlLoader.setResources(ResourceBundle.getBundle(baseName)));
        }
        return fxmlLoader.load();
    }
}
