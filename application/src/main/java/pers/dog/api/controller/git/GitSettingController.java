package pers.dog.api.controller.git;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.control.MaskerPane;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;
import pers.dog.boot.infra.util.PlatformUtils;
import pers.dog.infra.constant.GitRepositoryType;

/**
 * @author 废柴 2023/8/30 15:34
 */
public class GitSettingController implements Initializable {
    @FXML
    public ComboBox<GitRepositoryType> gitRepositoryType;
    @FXML
    public TextField gitRepository;
    @FXML
    public TextField repositoryName;
    @FXML
    public TextField username;
    @FXML
    public PasswordField privateToken;
    @FXML
    public MaskerPane masker;

    private final GitService gitService;

    public GitSettingController(GitService gitService) {
        this.gitService = gitService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gitRepositoryType.setItems(FXCollections.observableArrayList(GitRepositoryType.values()));
        gitRepositoryType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                gitRepository.setDisable(GitRepositoryType.GitHub.equals(newValue))
        );
    }

    public GitSetting getSetting() {
        return new GitSetting()
                .setGitRepositoryType(gitRepositoryType.getValue())
                .setGitRepository(gitRepository.getText())
                .setRepositoryName(repositoryName.getText())
                .setUsername(username.getText())
                .setPrivateToken(privateToken.getText());
    }

    public void test(Consumer<Boolean> onTest) {
        masker.setVisible(true);
        new Thread(() -> {
            AtomicBoolean result = new AtomicBoolean();
            try {
                result.set(gitService.test(getSetting()));
            } finally {
                Platform.runLater(() -> {
                    masker.setVisible(false);
                    onTest.accept(result.get());
                });
            }
        }).start();
    }
}
