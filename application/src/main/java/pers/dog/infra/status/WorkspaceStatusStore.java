package pers.dog.infra.status;

import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.cache.status.StatusStore;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.domain.entity.Project;

/**
 * Window 状态存储和回复
 *
 * @author 废柴 2021/6/21 19:36
 */
@Component
public class WorkspaceStatusStore implements StatusStore<SplitPane, WorkspaceStatusStore.SplitPaneStatus> {
    public static class SplitPaneStatus {
        private double[] dividers;
        private long[] openProjectIds;

        public double[] getDividers() {
            return dividers;
        }

        public SplitPaneStatus setDividers(double[] dividers) {
            this.dividers = dividers;
            return this;
        }

        public long[] getOpenProjectIds() {
            return openProjectIds;
        }

        public SplitPaneStatus setOpenProjectIds(long[] openProjectIds) {
            this.openProjectIds = openProjectIds;
            return this;
        }
    }

    @FXMLControl(controller = OneLibraryController.class)
    private TreeView<Project> projectTree;
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;

    private final ProjectService projectService;

    @Autowired
    public WorkspaceStatusStore(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public SplitPaneStatus storeStatus(SplitPane splitPane) {
        return new SplitPaneStatus()
                .setDividers(splitPane.getDividerPositions())
                .setOpenProjectIds(projectEditorWorkspace.getTabs()
                        .stream()
                        .mapToLong(tab -> ((ProjectEditorController) tab.getUserData()).getProject().getProjectId())
                        .toArray());
    }

    @Override
    public void readStatus(SplitPane splitPane, SplitPaneStatus splitPaneStatus) {
        splitPane.setDividerPositions(splitPaneStatus.dividers);
        if (!ObjectUtils.isEmpty(splitPaneStatus.getOpenProjectIds())) {
            for (long projectId : splitPaneStatus.getOpenProjectIds()) {
                openProject(projectId);
            }
        }
    }

    @Override
    public int getOrder() {
        return -50;
    }

    private void openProject(long projectId) {
        TreeItem<Project> root = projectTree.getRoot();
        openProject(root, projectId);
    }

    private void openProject(TreeItem<Project> root, long projectId) {
        if (!CollectionUtils.isEmpty(root.getChildren())) {
            for (TreeItem<Project> child : root.getChildren()) {
                if (child.getValue() != null && child.getValue().getProjectId() == projectId) {
                    projectService.openFile(child.getValue());
                    return;
                }
            }
        }
    }
}
