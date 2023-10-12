package pers.dog.api.controller;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.controlsfx.control.textfield.CustomTextField;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import pers.dog.api.callback.ProjectTreeCallback;
import pers.dog.app.service.ProjectService;
import pers.dog.domain.entity.Project;

public class OneLibraryController implements Initializable {

    @FXML
    public BorderPane oneLibraryWorkspace;
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
    @FXML
    public Button sidebarVisibleButton;

    private final ProjectService projectService;
    private final ProjectTreeCallback projectTreeCallback;

    private final ObservableList<TreeItem<Project>> searchCandidateList = FXCollections.observableArrayList();
    private final ObjectProperty<Pair<Integer, TreeItem<Project>>> currentSearched = new SimpleObjectProperty<>();

    public OneLibraryController(ProjectService projectService, ProjectTreeCallback projectTreeCallback) {
        this.projectService = projectService;
        this.projectTreeCallback = projectTreeCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectService.syncLocal();
        TreeItem<Project> projectTreeItem = projectService.tree();
        projectTree.setRoot(projectTreeItem);
        projectTree.requestFocus();
        projectTree.setEditable(false);
        projectTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        projectTree.setCellFactory(projectTreeCallback);
        projectEditorWorkspace.getSelectionModel().selectedItemProperty().addListener(observable -> {
            Tab selectedItem = projectEditorWorkspace.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ProjectEditorController projectEditorController = (ProjectEditorController) selectedItem.getUserData();
                projectEditorController.requestFocus();
            }
        });
        projectSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchCandidateList.clear();
            expand();
            if (!ObjectUtils.isEmpty(newValue)) {
                ArrayDeque<TreeItem<Project>> queue = new ArrayDeque<>();
                queue.push(projectTree.getRoot());
                while (!queue.isEmpty()) {
                    TreeItem<Project> item = queue.pop();
                    if (!CollectionUtils.isEmpty(item.getChildren())) {
                        queue.addAll(item.getChildren());
                    }
                    if (item.getValue() != null && item.getValue().getSimpleProjectName() != null && item.getValue().getSimpleProjectName().contains(newValue)) {
                        searchCandidateList.add(item);
                    }
                }
                if (searchCandidateList.isEmpty()) {
                    currentSearched.setValue(null);
                } else if (currentSearched.get() == null || !searchCandidateList.contains(currentSearched.get().getSecond())) {
                    currentSearched.setValue(Pair.of(0, searchCandidateList.get(0)));
                }
            }
        });
        projectSearch.setOnKeyPressed(event -> {
            if (KeyCode.UP.equals(event.getCode()) || KeyCode.DOWN.equals(event.getCode())) {
                if (!searchCandidateList.isEmpty()) {
                    int index = (Optional.ofNullable(currentSearched.getValue()).map(Pair::getFirst).orElse(0)
                            + (KeyCode.UP.equals(event.getCode()) ? -1 : 1)
                            + searchCandidateList.size()) % searchCandidateList.size();
                    currentSearched.setValue(Pair.of(index, searchCandidateList.get(index)));
                }
                event.consume();
            }
        });
        currentSearched.addListener((observable, oldValue, newValue) -> {
            projectTree.getSelectionModel().clearSelection();
            if (newValue != null) {
                projectTree.getSelectionModel().select(newValue.getSecond());
            }
        });
    }

    public void expand() {
        expand(projectTree.getRoot(), true);
    }

    public void collapse() {
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
