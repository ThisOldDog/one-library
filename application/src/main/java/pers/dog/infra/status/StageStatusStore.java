package pers.dog.infra.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.cache.status.StatusStore;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.component.setting.SettingService;
import pers.dog.domain.entity.Project;

/**
 * Window 状态存储和回复
 *
 * @author 废柴 2021/6/21 19:36
 */
@Component
public class StageStatusStore implements StatusStore<BorderPane, StageStatusStore.StageStatus> {
    public static class ViewStatus {
        private boolean showSidebar = true;

        public boolean isShowSidebar() {
            return showSidebar;
        }

        public ViewStatus setShowSidebar(boolean showSidebar) {
            this.showSidebar = showSidebar;
            return this;
        }
    }

    public static class StageStatus {

        private boolean maximized;
        private double height;
        private double width;
        private double x;
        private double y;

        private double[] dividers;
        private long[] openProjectIds;

        private String latestExportDirectory;
        private String latestSettingOption;
        private ViewStatus viewStatus = new ViewStatus();


        public boolean isMaximized() {
            return maximized;
        }

        public StageStatus setMaximized(boolean maximized) {
            this.maximized = maximized;
            return this;
        }

        public double getHeight() {
            return height;
        }

        public StageStatus setHeight(double height) {
            this.height = height;
            return this;
        }

        public double getWidth() {
            return width;
        }

        public StageStatus setWidth(double width) {
            this.width = width;
            return this;
        }

        public double getX() {
            return x;
        }

        public StageStatus setX(double x) {
            this.x = x;
            return this;
        }

        public double getY() {
            return y;
        }

        public StageStatus setY(double y) {
            this.y = y;
            return this;
        }

        public double[] getDividers() {
            return dividers;
        }

        public StageStatus setDividers(double[] dividers) {
            this.dividers = dividers;
            return this;
        }

        public long[] getOpenProjectIds() {
            return openProjectIds;
        }

        public StageStatus setOpenProjectIds(long[] openProjectIds) {
            this.openProjectIds = openProjectIds;
            return this;
        }

        public String getLatestExportDirectory() {
            return latestExportDirectory;
        }

        public StageStatus setLatestExportDirectory(String latestExportDirectory) {
            this.latestExportDirectory = latestExportDirectory;
            return this;
        }

        public String getLatestSettingOption() {
            return latestSettingOption;
        }

        public StageStatus setLatestSettingOption(String latestSettingOption) {
            this.latestSettingOption = latestSettingOption;
            return this;
        }

        public ViewStatus getViewStatus() {
            return viewStatus;
        }

        public StageStatus setViewStatus(ViewStatus viewStatus) {
            this.viewStatus = viewStatus;
            return this;
        }
    }

    @FXMLControl(controller = OneLibraryController.class)
    private SplitPane projectSplitPane;
    @FXMLControl(controller = OneLibraryController.class)
    private TreeView<Project> projectTree;
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;

    private final ProjectService projectService;
    private final SettingService settingService;
    private StageStatus stageStatus;
    private List<Consumer<StageStatus>> afterReadListener = new ArrayList<>();
    private AtomicBoolean ready = new AtomicBoolean(false);
    @Autowired
    public StageStatusStore(ProjectService projectService, SettingService settingService) {
        this.projectService = projectService;
        this.settingService = settingService;
    }

    @Override
    public String getFxId() {
        return "oneLibraryWorkspace";
    }

    @Override
    public StageStatus storeStatus(BorderPane oneLibraryWorkspace) {
        Window window = oneLibraryWorkspace.getScene().getWindow();
        StageStatus stageStatus = Optional.ofNullable(this.stageStatus)
                .orElseGet(StageStatus::new);
        stageStatus.setLatestSettingOption(settingService.getLatestSettingOption());
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            stageStatus.setMaximized(stage.isMaximized());
            if (stage.getScene() != null) {
                stageStatus.setHeight(stage.getScene().getHeight())
                        .setWidth(stage.getScene().getWidth());
            } else {
                stageStatus.setHeight(stage.getHeight())
                        .setWidth(stage.getWidth());
            }
            return stageStatus.setX(stage.getX())
                    .setY(stage.getY())
                    .setDividers(projectSplitPane.getDividerPositions())
                    .setOpenProjectIds(projectEditorWorkspace.getTabs()
                            .stream()
                            .mapToLong(tab -> ((ProjectEditorController) tab.getUserData()).getProject().getProjectId())

                            .toArray());
        }
        return null;
    }

    @Override
    public void readStatus(BorderPane oneLibraryWorkspace, StageStatus stageStatus) {
        if (stageStatus == null) {
            this.stageStatus = new StageStatus();
        } else {
            this.stageStatus = stageStatus;
            ready.set(true);
            Window window = oneLibraryWorkspace.getScene().getWindow();
            if (window instanceof Stage) {
                Stage stage = (Stage) window;
                stage.setMaximized(stageStatus.isMaximized());
                stage.setHeight(stageStatus.getHeight());
                stage.setWidth(stageStatus.getWidth());
                stage.setX(stageStatus.getX());
                stage.setY(stageStatus.getY());

                projectSplitPane.setDividerPositions(stageStatus.dividers);
                if (!ObjectUtils.isEmpty(stageStatus.getOpenProjectIds())) {
                    for (long projectId : stageStatus.getOpenProjectIds()) {
                        openProject(projectId);
                    }
                }
            }
            settingService.setLatestSettingOption(stageStatus.getLatestSettingOption());
            afterReadListener.forEach(listener -> listener.accept(this.stageStatus));
        }
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
                openProject(child, projectId);
            }
        }
    }

    public StageStatus getStageStatus() {
        return stageStatus;
    }

    public StageStatusStore setStageStatus(StageStatus stageStatus) {
        this.stageStatus = stageStatus;
        return this;
    }

    public void addAfterReadListener(Consumer<StageStatus> listener) {
        if (ready.get()) {
            listener.accept(getStageStatus());
        } else {
            afterReadListener.add(listener);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
