package pers.dog.api.callback;

import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.action.ActionUtils;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.api.controller.ProjectItemController;
import pers.dog.api.controller.ProjectItemEditingController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.action.project.CreateDirectoryAction;
import pers.dog.infra.action.project.CreateMarkdownAction;
import pers.dog.infra.action.project.DeleteProjectAction;
import pers.dog.infra.action.project.OpenRenameProjectAction;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2023/2/21 20:37
 */
@Component
public class ProjectTreeCallback implements Callback<TreeView<Project>, TreeCell<Project>> {
    private static final String PROJECT_ITEM_FXML = "project-item";
    private static final String PROJECT_ITEM_EDITING_FXML = "project-item-editing";
    private static final String PROJECT_EDITOR_FXML = "project-editor";
    private final ContextMenu BLANK_CONTEXT_MENU;
    private final ContextMenu PROJECT_CONTEXT_MENU;

    @SuppressWarnings("unused")
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;
    private final ProjectService projectService;

    public ProjectTreeCallback(ProjectService projectService,
                               CreateMarkdownAction createMarkdownAction,
                               CreateDirectoryAction createDirectoryAction,
                               DeleteProjectAction deleteProjectAction,
                               OpenRenameProjectAction openRenameProjectAction) {
        this.projectService = projectService;
        BLANK_CONTEXT_MENU = ActionUtils.createContextMenu(Arrays.asList(
                createMarkdownAction,
                createDirectoryAction
        ));
        PROJECT_CONTEXT_MENU = ActionUtils.createContextMenu(Arrays.asList(
                createMarkdownAction,
                createDirectoryAction,
                deleteProjectAction,
                ACTION_SEPARATOR,
                openRenameProjectAction
        ));
    }

    @Override
    public TreeCell<Project> call(TreeView<Project> projectTree) {

        return new TreeCell<>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    handleEmpty();
                } else {
                    handleProject(item);
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                handleProjectEditing();
            }

            @Override
            public void commitEdit(Project project) {
                if (project != null) {
                    projectService.updateProject(project);
                    setItem(project);
                }
                super.commitEdit(project);
                cancelEdit();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                handleProject(getItem());
                projectTree.setEditable(false);
            }

            private void handleEmpty() {
                setText(null);
                setContextMenu(BLANK_CONTEXT_MENU);
                setGraphic(null);
                setOnContextMenuRequested(event -> {
                    projectTree.getSelectionModel().clearSelection();
                });
            }

            private void handleProject(Project item) {
                Parent parent = FXMLUtils.loadFXML(PROJECT_ITEM_FXML);
                ProjectItemController controller = FXMLUtils.getController(parent);
                controller.showProject(item);
                setContextMenu(PROJECT_CONTEXT_MENU);
                setGraphic(parent);
                setOnContextMenuRequested(event -> {
                });
                setOnMouseClicked(event -> {
                    if (ProjectType.FILE.equals(item.getProjectType()) &&
                            MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2) {
                        ObservableList<Tab> tabs = projectEditorWorkspace.getTabs();
                        for (Tab tab : tabs) {
                            if (Objects.equals(tab.getUserData(), item)) {
                                projectEditorWorkspace.getSelectionModel().select(tab);
                                return;
                            }
                        }
                        Tab tab = new Tab(item.getSimpleProjectName());
                        tab.setId(String.valueOf(item.getProjectId()));
                        Parent projectEditor = FXMLUtils.loadFXML(PROJECT_EDITOR_FXML);
                        ProjectEditorController projectEditorController = FXMLUtils.getController(projectEditor);
                        projectEditorController.setProject(item);
                        tab.setContent(projectEditor);
                        tabs.add(tab);
                    }
                });
            }

            private void handleProjectEditing() {
                Parent parent = FXMLUtils.loadFXML(PROJECT_ITEM_EDITING_FXML);
                ProjectItemEditingController controller = FXMLUtils.getController(parent);
                Project item = getItem();
                if (item != null) {
                    controller.showProject(item);
                }
                controller.getProjectNameEditor().setOnKeyReleased(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        commitEdit(Optional.ofNullable(getItem())
                                .orElseGet(Project::new)
                                .rename(controller.getProjectNameEditor().getText()));
                    } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
                controller.getProjectNameEditor().focusedProperty().addListener((change, oldValue, newValue) -> {
                    if (BooleanUtils.isTrue(oldValue) && BooleanUtils.isFalse(newValue) && isEditing()) {
                        commitEdit(Optional.ofNullable(getItem())
                                .orElseGet(Project::new)
                                .rename(controller.getProjectNameEditor().getText()));
                    }
                });
                setContextMenu(null);
                setGraphic(parent);
                controller.getProjectNameEditor().requestFocus();
            }
        };
    }
}
