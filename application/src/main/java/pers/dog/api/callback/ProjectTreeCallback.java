package pers.dog.api.callback;

import java.util.Arrays;

import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.controlsfx.control.action.ActionUtils;
import org.springframework.stereotype.Component;
import pers.dog.api.item.ProjectItemController;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.action.project.CreateDirectoryAction;
import pers.dog.infra.action.project.CreateMarkdownAction;
import pers.dog.infra.action.project.DeleteProjectAction;

/**
 * @author 废柴 2023/2/21 20:37
 */
@Component
public class ProjectTreeCallback implements Callback<TreeView<Project>, TreeCell<Project>> {
    private static final String PROJECT_ITEM_FXML = "project-item";
    private final ContextMenu BLANK_CONTEXT_MENU;
    private final ContextMenu PROJECT_CONTEXT_MENU;

    public ProjectTreeCallback(CreateMarkdownAction createMarkdownAction,
                               CreateDirectoryAction createDirectoryAction,
                               DeleteProjectAction deleteProjectAction) {
        BLANK_CONTEXT_MENU = ActionUtils.createContextMenu(Arrays.asList(
                createMarkdownAction,
                createDirectoryAction
        ));
        PROJECT_CONTEXT_MENU = ActionUtils.createContextMenu(Arrays.asList(
                createMarkdownAction,
                createDirectoryAction,
                deleteProjectAction
        ));
    }

    @Override
    public TreeCell<Project> call(TreeView<Project> projectTree) {
        return new TreeCell<>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setContextMenu(BLANK_CONTEXT_MENU);
                    setGraphic(null);
                } else {
                    Parent parent = FXMLUtils.loadFXML(PROJECT_ITEM_FXML);
                    ProjectItemController projectItemController = FXMLUtils.getController(parent);
                    projectItemController.setProject(item, isEditing());
                    setContextMenu(PROJECT_CONTEXT_MENU);
                    setGraphic(parent);
                }
            }
        };
    }
}
