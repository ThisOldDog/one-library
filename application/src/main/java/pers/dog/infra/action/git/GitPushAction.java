package pers.dog.infra.action.git;

import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.app.service.GitService;
import pers.dog.app.service.ProjectService;
import pers.dog.app.service.impl.GitServiceImpl;
import pers.dog.boot.infra.control.ProgressDialog;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class GitPushAction extends Action {
    private final GitService gitService;
    private final ProjectService projectService;

    public GitPushAction(GitService gitService, ProjectService projectService) {
        super(I18nMessageSource.getResource("info.action.git.push"));
        this.gitService = gitService;
        this.projectService = projectService;
        super.setEventHandler(this::onAction);
    }

    public void onAction(ActionEvent event) {
        ProgressDialog<ButtonType> progressDialog = new ProgressDialog<>();
        BooleanProperty showing = new SimpleBooleanProperty(true);
        showing.addListener((observable, oldValue, newValue) -> {
            if (BooleanUtils.isNotTrue(newValue) && progressDialog.isShowing()) {
                Platform.runLater(progressDialog::close);
            }
        });
        Consumer<GitServiceImpl.GitPushStep> gitPushStepListener = gitPushStep -> {
            switch (gitPushStep) {
                case CHECK ->
                        Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.check")));
                case OPEN ->
                        Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.open")));
                case PULL ->
                        Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.pull")));
                case COPY ->
                        Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.copy")));
                default ->
                        Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.push")));
            }
        };
        new Thread(() -> {
            Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.project.dirty_check")));
            if (!projectService.dirtyProject().isEmpty()) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle(I18nMessageSource.getResource("confirmation"));
                confirmation.setHeaderText(I18nMessageSource.getResource("confirmation.git.project.dirty"));
                confirmation.setContentText(I18nMessageSource.getResource("confirmation.git.project.dirty.prompt"));
                confirmation.getButtonTypes().clear();
                ButtonType saveButtonType = new ButtonType(I18nMessageSource.getResource("info.action.git.push.save_and_push"));
                ButtonType skipButtonType = new ButtonType(I18nMessageSource.getResource("info.action.git.push.skip_and_push"));
                confirmation.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL, skipButtonType);
                confirmation.showAndWait().ifPresent(buttonType -> {
                    try {
                        if (saveButtonType.equals(buttonType)) {
                            Platform.runLater(() -> progressDialog.setText(I18nMessageSource.getResource("confirmation.git.project.dirty.save")));
                            projectService.saveAll();
                        }
                        if (saveButtonType.equals(buttonType) || skipButtonType.equals(buttonType)) {
                            gitService.push(gitPushStepListener);
                        }
                    } finally {
                        showing.set(false);
                    }
                });
            } else {
                try {
                    gitService.push(gitPushStepListener);
                } finally {
                    showing.set(false);
                }
            }
        }).start();
        if (showing.get()) {
            progressDialog.showAndWait();
        }
    }


}
