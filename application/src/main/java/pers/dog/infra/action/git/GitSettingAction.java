package pers.dog.infra.action.git;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.git.GitSettingController;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class GitSettingAction extends Action {
    private static final String VIEW = "git/git-setting";
    private final GitService gitService;

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
        ButtonType testButtonType = new ButtonType(I18nMessageSource.getResource("info.action.git.setting.test"));
        ButtonType createButtonType = new ButtonType(I18nMessageSource.getResource("info.action.git.setting.create"));
        dialogPane.getButtonTypes().addAll(testButtonType, createButtonType, ButtonType.OK, ButtonType.CANCEL);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, okAction -> gitService.save(controller.getSetting()));

        Button testButton = (Button) dialogPane.lookupButton(testButtonType);
        Button createButton = (Button) dialogPane.lookupButton(createButtonType);
        Bindings.bindBidirectional(testButton.disableProperty(), createButton.disableProperty());
        InvalidationListener listener = observable -> testButton.setDisable(ObjectUtils.isEmpty(controller.gitRepositoryType.getValue())
                || ObjectUtils.isEmpty(controller.gitRepository.getText())
                || ObjectUtils.isEmpty(controller.repositoryName.getText())
                || ObjectUtils.isEmpty(controller.username.getText())
                || ObjectUtils.isEmpty(controller.privateToken.getText()));
        testButton.setDisable(true);
        testButton.addEventFilter(ActionEvent.ACTION, previewAction -> {
            previewAction.consume();
            controller.masker.setVisible(true);
            controller.test(testResult ->
                    Notifications.create()
                            .owner(dialogPane)
                            .position(Pos.TOP_RIGHT)
                            .graphic(buildRepositoryTestNotification(testResult, controller.repositoryName.getText()))
                            .show()
            );
        });

        controller.gitRepositoryType.valueProperty().

                addListener(listener);
        controller.gitRepository.textProperty().

                addListener(listener);
        controller.repositoryName.textProperty().

                addListener(listener);
        controller.username.textProperty().

                addListener(listener);
        controller.privateToken.textProperty().

                addListener(listener);

        // init
        GitSetting gitSetting = gitService.getGitSetting();
        controller.gitRepositoryType.setValue(gitSetting.getGitRepositoryType());
        controller.gitRepository.setText(gitSetting.getGitRepository());
        controller.repositoryName.setText(gitSetting.getRepositoryName());
        controller.username.setText(gitSetting.getUsername());
        controller.privateToken.setText(gitSetting.getPrivateToken());

        dialog.showAndWait();
    }

    private Node buildRepositoryTestNotification(boolean testResult, String repository) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(16));
        hBox.setAlignment(Pos.CENTER_LEFT);
        Glyph icon = testResult
                ? new Glyph("FontAwesome",  FontAwesome.Glyph.CHECK_CIRCLE).color(Color.GREEN)
                : new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.RED);
        Text text = testResult
                ? new Text(I18nMessageSource.getResource("info.action.git.setting.test.repository.exists", repository))
                : new Text(I18nMessageSource.getResource("info.action.git.setting.test.repository.not_exists", repository));
        HBox.setMargin(text, new Insets(0, 0, 0, 8));
        hBox.getChildren().addAll(icon, text);
        return hBox;
    }

}
