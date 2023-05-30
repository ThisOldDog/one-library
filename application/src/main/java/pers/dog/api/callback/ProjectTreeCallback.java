package pers.dog.api.callback;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.action.ActionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
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
    private static final Logger logger = LoggerFactory.getLogger(ProjectTreeCallback.class);
    private static final String PROJECT_ITEM_FXML = "project-item";
    private static final String PROJECT_ITEM_EDITING_FXML = "project-item-editing";
    private static final String CELL_DRAG_OVER_STYLE_CLASS_FILE = "project-tree-drag-over-file";
    private static final String CELL_DRAG_OVER_STYLE_CLASS_DIR = "project-tree-drag-over-dir";
    private final ContextMenu blankContextMenu;
    private final ContextMenu projectContextMenu;

    @SuppressWarnings("unused")
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;
    private final ProjectService projectService;
    private static final AtomicReference<TreeCell<Project>> dragged = new AtomicReference<>();
    private static final AtomicReference<TreeCell<Project>> previousOver = new AtomicReference<>();

    public ProjectTreeCallback(ProjectService projectService,
                               // Action
                               CreateMarkdownAction createMarkdownAction,
                               CreateDirectoryAction createDirectoryAction,
                               DeleteProjectAction deleteProjectAction,
                               OpenRenameProjectAction openRenameProjectAction) {
        this.projectService = projectService;
        blankContextMenu = ActionUtils.createContextMenu(Arrays.asList(
                createMarkdownAction,
                createDirectoryAction
        ));
        projectContextMenu = ActionUtils.createContextMenu(Arrays.asList(
                createMarkdownAction,
                createDirectoryAction,
                deleteProjectAction,
                ActionUtils.ACTION_SEPARATOR,
                openRenameProjectAction
        ));
    }

    @Override
    public TreeCell<Project> call(TreeView<Project> projectTree) {

        return new TreeCell<>() {

            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
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
                setContextMenu(blankContextMenu);
                setGraphic(null);
                setOnContextMenuRequested(event -> {
                    projectTree.getSelectionModel().clearSelection();
                });
            }

            private void handleProject(Project item) {
                Parent parent = FXMLUtils.loadFXML(PROJECT_ITEM_FXML);
                ProjectItemController controller = FXMLUtils.getController(parent);
                controller.showProject(item);
                setContextMenu(projectContextMenu);
                setGraphic(parent);
                setOnContextMenuRequested(event -> {
                });
                setOnMouseClicked(event -> {
                    if (ProjectType.FILE.equals(item.getProjectType()) &&
                            MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2) {
                        projectService.openFile();
                    }
                });

                setOnDragDetected(event -> {
                    Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
                    dragged.set(this);
                    Pane pane = (Pane) getGraphic();
                    WritableImage writableImage = new WritableImage((int) pane.getWidth(), (int) pane.getHeight());
                    parent.snapshot(new SnapshotParameters(), writableImage);
                    getTreeItem().setExpanded(false);
                    dragboard.setDragView(writableImage);
                    dragboard.setContent(Collections.singletonMap(DataFormat.PLAIN_TEXT, item.getProjectName()));
                });
                setOnDragOver(event -> {
                    event.acceptTransferModes(TransferMode.MOVE);
                    if (previousOver.get() != null) {
                        previousOver.get().getStyleClass().removeAll(CELL_DRAG_OVER_STYLE_CLASS_FILE, CELL_DRAG_OVER_STYLE_CLASS_DIR);
                    }
                    previousOver.set(this);
                    if (ProjectType.DIRECTORY.equals(getItem().getProjectType())) {
                        getStyleClass().add(CELL_DRAG_OVER_STYLE_CLASS_DIR);
                    } else {
                        getStyleClass().add(CELL_DRAG_OVER_STYLE_CLASS_FILE);
                    }
                });
                setOnDragDropped(event -> {
                    TreeCell<Project> source = dragged.get();
                    TreeCell<Project> target = previousOver.get();
                    dragged.set(null);
                    if (target != null) {
                        target.getStyleClass().removeAll(CELL_DRAG_OVER_STYLE_CLASS_FILE, CELL_DRAG_OVER_STYLE_CLASS_DIR);
                        previousOver.set(null);
                        if (source == target) {
                            return;
                        }
                        // 先把 source 移除
                        TreeItem<Project> sourceParent = source.getTreeItem().getParent();
                        TreeItem<Project> targetParent;
                        int targetIndex;
                        if (ProjectType.DIRECTORY.equals(target.getItem().getProjectType())) {
                            targetParent = target.getTreeItem();
                        } else {
                            targetParent = target.getTreeItem().getParent();
                        }
                        if (!projectService.move(source.getTreeItem(), targetParent)) {
                            return;
                        }
                        sourceParent.getChildren().remove(source.getTreeItem());
                        projectService.reordered(sourceParent.getChildren());
                        targetIndex = Math.min(0, targetParent.getChildren().indexOf(target.getTreeItem()));
                        if (targetIndex + 1 < targetParent.getChildren().size()) {
                            targetParent.getChildren().add(targetIndex + 1, source.getTreeItem());
                        } else {
                            targetParent.getChildren().add(source.getTreeItem());
                        }
                        targetParent.setExpanded(true);
                        sourceParent.setExpanded(true);
                        source.getTreeItem().setExpanded(true);
                        projectService.reordered(targetParent.getChildren());
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
