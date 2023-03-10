package pers.dog.api.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import org.controlsfx.control.textfield.CustomTextField;
import pers.dog.app.service.ProjectService;
import pers.dog.domain.entity.Project;

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
    @FXML
    private TreeView<Project> projectTree;

    private final ProjectService projectService;

    public OneLibraryController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeItem<Project> projectTreeItem = projectService.tree();
        projectTree.setRoot(projectTreeItem);
        projectTree.requestFocus();
    }
}
