package pers.dog.api.controller.git;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pers.dog.api.dto.GitSetting;

/**
 * @author 废柴 2023/8/30 15:34
 */
public class GitSettingController {
    @FXML
    public TextField gitRepository;
    @FXML
    public TextField username;
    @FXML
    public PasswordField privateToken;

    public GitSetting getSetting() {
        return new GitSetting()
                .setGitRepository(gitRepository.getText())
                .setUsername(username.getText())
                .setPrivateToken(privateToken.getText());
    }
}
