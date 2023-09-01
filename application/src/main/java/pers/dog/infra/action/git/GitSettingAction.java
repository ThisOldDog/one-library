package pers.dog.infra.action.git;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.git.GitSettingController;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;
import pers.dog.app.service.impl.GitServiceImpl;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class GitSettingAction extends Action {
    private static final String VIEW = "git/git-setting";
    private final GitService gitService;

    public GitSettingAction(GitService gitService) {
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
        InvalidationListener listener = observable -> {
            testButton.setDisable(ObjectUtils.isEmpty(controller.gitRepositoryType.getValue())
                    || ObjectUtils.isEmpty(controller.gitRepository.getText())
                    || ObjectUtils.isEmpty(controller.repositoryName.getText())
                    || ObjectUtils.isEmpty(controller.username.getText())
                    || ObjectUtils.isEmpty(controller.privateToken.getText()));
            controller.testResultBox.getChildren().clear();
        };
        testButton.setDisable(true);
        testButton.addEventFilter(ActionEvent.ACTION, previewAction -> {
            previewAction.consume();
            controller.masker.setVisible(true);
            controller.test(testResult -> {
                controller.testResultBox.getChildren().clear();
                Glyph icon;
                Text text;
                if (GitServiceImpl.GitRepositoryResult.SUCCESS.equals(testResult)) {
                    icon = new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE).color(Color.GREEN);
                    text = new Text(I18nMessageSource.getResource("info.action.git.setting.test.repository.exists", controller.repositoryName.getText()));
                } else {
                    icon = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.RED);
                    text = new Text(I18nMessageSource.getResource("info.action.git.setting.test.repository.not_exists", controller.repositoryName.getText()));
                }
                HBox.setMargin(text, new Insets(0, 0, 0, 8));
                controller.testResultBox.getChildren().addAll(icon, text);
            });
        });

        Button createButton = (Button) dialogPane.lookupButton(createButtonType);
        Bindings.bindBidirectional(testButton.disableProperty(), createButton.disableProperty());
        createButton.addEventFilter(ActionEvent.ACTION, createAction -> {
            createAction.consume();
            controller.create(createResult -> {
                controller.testResultBox.getChildren().clear();
                Glyph icon;
                Text text;
                if (GitServiceImpl.GitRepositoryResult.SUCCESS.equals(createResult)) {
                    icon = new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE).color(Color.GREEN);
                    text = new Text(I18nMessageSource.getResource("info.action.git.setting.repository.create_success", controller.repositoryName.getText()));
                } else if (GitServiceImpl.GitRepositoryResult.INVALID.equals(createResult)) {
                    icon = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.ORANGE);
                    text = new Text(I18nMessageSource.getResource("info.action.git.setting.repository.create_invalid", controller.repositoryName.getText()));
                } else {
                    icon = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.RED);
                    text = new Text(I18nMessageSource.getResource("info.action.git.setting.repository.create_failed", controller.repositoryName.getText()));
                }
                HBox.setMargin(text, new Insets(0, 0, 0, 8));
                controller.testResultBox.getChildren().addAll(icon, text);
            });
        });

        controller.gitRepositoryType.valueProperty().addListener(listener);
        controller.gitRepository.textProperty().addListener(listener);
        controller.repositoryName.textProperty().addListener(listener);
        controller.username.textProperty().addListener(listener);
        controller.privateToken.textProperty().addListener(listener);

        // init
        GitSetting gitSetting = gitService.getGitSetting();
        controller.gitRepositoryType.setValue(gitSetting.getGitRepositoryType());
        controller.gitRepository.setText(gitSetting.getGitRepository());
        controller.repositoryName.setText(gitSetting.getRepositoryName());
        controller.username.setText(gitSetting.getUsername());
        controller.privateToken.setText(gitSetting.getPrivateToken());

        dialog.showAndWait();
    }


}
