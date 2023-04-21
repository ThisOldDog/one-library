package pers.dog.boot.infra.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

public class PropertySheetDialog<T> extends Dialog<T> {

    public PropertySheetDialog(T value) {
        DialogPane dialogPane = getDialogPane();
        setGraphic(buildFormPane(value));
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }

    private PropertySheet buildFormPane(T value) {
        PropertySheet propertySheet = new PropertySheet();
        propertySheet.modeSwitcherVisibleProperty().set(false);
        propertySheet.searchBoxVisibleProperty().set(false);
        propertySheet.getItems().setAll(BeanPropertyUtils.getProperties(value));
        return propertySheet;
    }
}
