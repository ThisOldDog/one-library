package pers.dog.boot.infra.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.exception.ExceptionUtils;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2023/8/31 11:11
 */
public class AlertUtils {
    private AlertUtils(){
    }

    public static void showException(String title, String headerText, String contentText, String labelText, Throwable e) {
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
    }
}
