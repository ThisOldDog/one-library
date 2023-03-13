package pers.dog.app.service.impl;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2022/8/19 15:59
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    private static final TreeItem<Project> ROOT = new TreeItem<>(new Project().setProjectName("ROOT").setProjectType(ProjectType.DIRECTORY));
    private final ProjectRepository projectRepository;
    private final FileOperationHandler fileOperationHandler;
    @SuppressWarnings("unused")
    @FXMLControl(controller = OneLibraryController.class, id = "projectTree")
    private TreeView<Project> projectTree;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.fileOperationHandler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/document"));
    }

    @Override
    public TreeItem<Project> tree() {
        List<Project> projectList = projectRepository.findAll(Sort.by(Project.FIELD_SORT_INDEX));
        if (!projectList.isEmpty()) {
            buildTree(projectList, ROOT, null, new BitSet(projectList.size()));
        }
        return ROOT;
    }

    @Override
    public TreeItem<Project> createFile(ProjectType projectType, FileType fileType) {
        TreeItem<Project> parent = currentParentDirectory();
        Assert.isTrue(ProjectType.DIRECTORY.equals(projectType) || fileType != null, "[Project] When the project type is file, the type of the file cannot be null.");
        Project project = new Project()
                .setProjectName(buildFileName(parent, projectType, fileType))
                .setProjectType(projectType)
                .setFileType(fileType)
                .setParentProjectId(parent.getValue().getProjectId())
                .setSortIndex(parent.getChildren().size() + 1);
        if (ProjectType.DIRECTORY.equals(projectType)) {
            fileOperationHandler.createDirectory(project.getProjectName(), getRelativePath(parent));
        } else {
            fileOperationHandler.createFile(project.getProjectName(), getRelativePath(parent));
        }
        projectRepository.save(project);
        TreeItem<Project> treeItem = new TreeItem<>(project);
        parent.getChildren().add(treeItem);
        expandDirectory(parent);
        return treeItem;
    }

    @Override
    public void deleteProject() {
        TreeItem<Project> selected = projectTree.getSelectionModel().getSelectedItem();
        Project selectedProject = selected != null && !ROOT.equals(selected) ? selected.getValue() : null;
        if (selectedProject == null) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(I18nMessageSource.getResource("info.project.delete.project"));
        if (ProjectType.DIRECTORY.equals(selectedProject.getProjectType())) {
            confirmation.setContentText(I18nMessageSource.getResource("info.project.delete.project.confirmation.dir", selectedProject.getProjectName()));
        } else {
            confirmation.setContentText(I18nMessageSource.getResource("info.project.delete.project.confirmation.file", selectedProject.getProjectName()));
        }
        confirmation.showAndWait().ifPresent(buttonType -> {
            if (ButtonType.OK.equals(buttonType)) {
                delete(selected);
            }
        });
    }

    private TreeItem<Project> currentParentDirectory() {
        TreeItem<Project> parentNode = projectTree.getSelectionModel().getSelectedItem();
        if (parentNode == null || parentNode.getValue() == null) {
            return ROOT;
        }
        Project parentValue = parentNode.getValue();
        if (ProjectType.DIRECTORY.equals(parentValue.getProjectType())) {
            return parentNode;
        } else {
            return parentNode.getParent();
        }
    }

    private void delete(TreeItem<Project> selected) {
        TreeItem<Project> parent = selected.getParent();
        deleteFile(selected);
        deleteData(selected);
        deleteView(selected);
        ObservableList<TreeItem<Project>> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            TreeItem<Project> child = children.get(i);
            child.getValue().setSortIndex(i + 1);
            projectRepository.save(child.getValue());
        }
    }

    private void deleteFile(TreeItem<Project> selected) {
        fileOperationHandler.delete(selected.getValue().getProjectName(), getRelativePath(selected.getParent()));
    }

    private void deleteData(TreeItem<Project> selected) {
        projectRepository.deleteById(selected.getValue().getProjectId());
        for (TreeItem<Project> child : selected.getChildren()) {
            deleteData(child);
        }
    }

    private void deleteView(TreeItem<Project> selected) {
        selected.getParent().getChildren().remove(selected);
    }

    private String[] getRelativePath(TreeItem<Project> parent) {
        ArrayDeque<String> relativePath = new ArrayDeque<>();
        while (!ROOT.equals(parent)) {
            relativePath.addFirst(parent.getValue().getProjectName());
            parent = parent.getParent();
        }
        return relativePath.toArray(new String[]{});
    }

    private String buildFileName(TreeItem<Project> parent, ProjectType projectType, FileType fileType) {
        int fileIndex = 0;
        String projectName = null;
        while (projectName == null) {
            if (ProjectType.FILE.equals(projectType)) {
                if (fileIndex == 0) {
                    projectName = I18nMessageSource.getResource("info.project.default_file_name", fileType.getName(), fileType.getSuffix());
                } else {
                    projectName = I18nMessageSource.getResource("info.project.default_file_name_duplicate", fileType.getName(), fileIndex, fileType.getSuffix());
                }
            } else {
                if (fileIndex == 0) {
                    projectName = I18nMessageSource.getResource("info.project.default_directory_name");
                } else {
                    projectName = I18nMessageSource.getResource("info.project.default_directory_name_duplicate", fileIndex);
                }
            }
            if (fileOperationHandler.exists(projectName, getRelativePath(parent))) {
                projectName = null;
            }
            fileIndex++;
        }
        return projectName;
    }

    private void buildTree(List<Project> projectList, TreeItem<Project> root, Long parentId, BitSet skip) {
        for (int i = 0; i < projectList.size(); i++) {
            if (skip.get(i)) {
                continue;
            }
            Project project = projectList.get(i);
            if (Objects.equals(project.getParentProjectId(), parentId)) {
                TreeItem<Project> node = new TreeItem<>(project);
                root.getChildren().add(node);
                skip.set(i);
                buildTree(projectList, node, project.getProjectId(), skip);
            }
        }
    }

    private void expandDirectory(TreeItem<Project> directory) {
        if (!directory.isExpanded()) {
            directory.setExpanded(true);
        }
    }
}
