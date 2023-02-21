package pers.dog.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.controlsfx.control.textfield.CustomTextField;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.util.FXMLUtils;

public class OneLibraryController implements Initializable {
    private static final String PROJECT_EMPTY_SCENE = "project-empty";
    @FXML
    private CustomTextField projectSearch;
    @FXML
    private Button projectExpand;
    @FXML
    private Button projectCollapse;
    @FXML
    private Pane projectWorkspace;

    private final ProjectService projectService;

    public OneLibraryController() {
        this.projectService = ProjectService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectService.init(change -> {
            try {
                Parent projectEmptyScene = FXMLUtils.loadFXML(PROJECT_EMPTY_SCENE);
                projectWorkspace.getChildren().clear();
                projectWorkspace.getChildren().add(projectEmptyScene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
