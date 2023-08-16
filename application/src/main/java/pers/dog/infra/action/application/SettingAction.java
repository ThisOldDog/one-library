package pers.dog.infra.action.application;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TabPane;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class SettingAction extends Action {
    @FXMLControl(controller = OneLibraryController.class)
    private final ControlProvider<TabPane> projectEditorWorkspace = new ControlProvider<>();

    private SettingAction() {
        super(I18nMessageSource.getResource("info.action.setting"));
        super.setEventHandler(this::openSetting);
    }

    public void openSetting(ActionEvent event) {
        Parent parent = FXMLUtils.loadFXML("setting/setting");
        Dialog<Void> settingDialog = new Dialog<>();
        settingDialog.setTitle(getText());

        DialogPane dialogPane = settingDialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.requestLayout();
        dialogPane.autosize();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        settingDialog.show();
    }
}