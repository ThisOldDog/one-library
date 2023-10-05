package pers.dog.boot.infra.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2023/8/31 11:11
 */
public class AlertUtils {
    private static final Logger logger = LoggerFactory.getLogger(AlertUtils.class);

    private AlertUtils() {
    }

    public static void showException(String title, String headerText, String contentText, String labelText, Throwable e) {
        logger.error("[Alert Error] " + e.getMessage(), e);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18nMessageSource.getResource(title));
            alert.setHeaderText(I18nMessageSource.getResource(headerText));
            alert.setContentText(I18nMessageSource.getResource(contentText));
            Label label = new Label(I18nMessageSource.getResource(labelText));
            TextArea textArea = new TextArea(ExceptionUtils.getStackTrace(e));
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane content = new GridPane();
            content.setMaxWidth(Double.MAX_VALUE);
            content.add(label, 0, 0);
            content.add(textArea, 0, 1);
            alert.getDialogPane().setExpandableContent(content);
            alert.showAndWait();
        });
    }

    public static void showWarning(String title, String headerText, String contentText) {
        showAlert(Alert.AlertType.WARNING, title, headerText, contentText);
    }

    public static void showError(String title, String headerText, String contentText) {
        showAlert(Alert.AlertType.ERROR, title, headerText, contentText);
    }

    private static void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        logger.warn("[Alert {}] {}", alertType, title);
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(I18nMessageSource.getResource(title));
            alert.setHeaderText(I18nMessageSource.getResource(headerText));
            alert.setContentText(I18nMessageSource.getResource(contentText));
            alert.showAndWait();
        });
    }
}
