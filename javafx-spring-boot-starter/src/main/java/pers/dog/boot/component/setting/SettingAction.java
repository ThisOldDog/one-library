package pers.dog.boot.component.setting;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.boot.JavaFXSpringBootApplication;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class SettingAction extends Action {

    private SettingAction() {
        super(I18nMessageSource.getResource("info.action.setting"));
        super.setEventHandler(this::openSetting);
    }

    public void openSetting(ActionEvent event) {
        Parent parent = FXMLUtils.loadFXML("setting", JavaFXSpringBootApplication.class);
        SettingController controller = FXMLUtils.getController(parent);
        Dialog<ButtonType> settingDialog = new Dialog<>();
        settingDialog.setTitle(getText());
        settingDialog.setResizable(true);

        DialogPane dialogPane = settingDialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.requestLayout();
        dialogPane.autosize();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);
        dialogPane.lookupButton(ButtonType.APPLY).addEventFilter(ActionEvent.ACTION, actionEvent -> {
            controller.saveSetting();
            actionEvent.consume();
        });
        settingDialog.showAndWait()
                .ifPresent(buttonType -> {
                    if (ButtonType.OK.equals(buttonType)) {
                        controller.saveSetting();
                    }
                });
    }
}
