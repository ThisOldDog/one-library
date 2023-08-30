package pers.dog.infra.action.git;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.util.Duration;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.git.GitSettingController;
import pers.dog.app.service.GitService;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.boot.infra.util.PlatformUtils;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class GitSettingAction extends Action {
    private static final String VIEW = "git/git-setting";
    private final GitService gitService;
    private boolean saveToProject;

    private GitSettingAction(GitService gitService) {
        super(I18nMessageSource.getResource("info.action.git.setting"));
        this.gitService = gitService;
        super.setEventHandler(this::onAction);
    }

    @SuppressWarnings("DuplicatedCode")
    public void onAction(ActionEvent event) {
        Parent parent = FXMLUtils.loadFXML(VIEW);
        GitSettingController controller = FXMLUtils.getController(parent);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(getText());
        dialog.setResizable(true);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.getButtonTypes().addAll(ButtonType.NEXT, ButtonType.OK, ButtonType.CANCEL);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, okAction -> gitService.save(controller.getSetting()));

        Button testButton = (Button) dialogPane.lookupButton(ButtonType.NEXT);
        testButton.setText(I18nMessageSource.getResource("info.action.git.setting.test"));
        InvalidationListener listener = observable -> testButton.setDisable(ObjectUtils.isEmpty(controller.gitRepository.getText())
                || ObjectUtils.isEmpty(controller.username.getText())
                || ObjectUtils.isEmpty(controller.privateToken.getText()));
        testButton.setDisable(true);
        controller.gitRepository.textProperty().addListener(listener);
        controller.username.textProperty().addListener(listener);
        controller.privateToken.textProperty().addListener(listener);
        testButton.addEventFilter(ActionEvent.ACTION, previewAction -> {
            gitService.test(controller.getSetting());
            previewAction.consume();
        });



        dialog.showAndWait();
    }

}
