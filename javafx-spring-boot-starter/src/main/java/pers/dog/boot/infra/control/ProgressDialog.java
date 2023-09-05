package pers.dog.boot.infra.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;
import pers.dog.boot.JavaFXSpringBootApplication;
import pers.dog.boot.controller.ProgressDialogController;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2023/9/4 11:17
 */
public class ProgressDialog<T> extends Dialog<T> {
    private static final String VIEW = "process-dialog";

    private final StringProperty text = new SimpleStringProperty();

    public ProgressDialog() {
        Parent parent = FXMLUtils.loadFXML(VIEW, JavaFXSpringBootApplication.class);
        ProgressDialogController controller = FXMLUtils.getController(parent);
        initStyle(StageStyle.UNDECORATED);
        setResizable(true);

        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.getButtonTypes().clear();
        dialogPane.getButtonTypes().add(ButtonType.OK);
        dialogPane.lookupButton(ButtonType.OK).setVisible(false);
        dialogPane.autosize();
        Bindings.bindBidirectional(text, controller.text.textProperty());
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }
}
