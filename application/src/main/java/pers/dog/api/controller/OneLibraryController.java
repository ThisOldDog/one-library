package pers.dog.api.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.controlsfx.control.textfield.CustomTextField;
import pers.dog.app.service.ProjectService;
import pers.dog.domain.entity.Project;

public class OneLibraryController implements Initializable {
    private static final String PROJECT_EMPTY_SCENE = "project-empty";

    @FXML
    private SplitPane projectSplitPane;
    @FXML
    private CustomTextField projectSearch;
    @FXML
    private Button projectExpand;
    @FXML
    private Button projectCollapse;
    @FXML
    private Pane projectWorkspace;
    @FXML
    private TabPane projectEditorWorkspace;
    @FXML
    private TreeView<Project> projectTree;

    private final ProjectService projectService;

    public OneLibraryController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectService.syncLocal();
        TreeItem<Project> projectTreeItem = projectService.tree();
        projectTree.setRoot(projectTreeItem);
        projectTree.requestFocus();
        projectTree.setEditable(false);
    }

    public void expand(ActionEvent event) {
        expand(projectTree.getRoot(), true);
    }

    public void collapse(ActionEvent event) {
        expand(projectTree.getRoot(), false);
    }

    private void expand(TreeItem<Project> node, boolean expanded) {
        if (!projectTree.getRoot().equals(node)) {
            node.setExpanded(expanded);
        }
        for (TreeItem<Project> child : node.getChildren()) {
            expand(child, expanded);
        }
    }
}
