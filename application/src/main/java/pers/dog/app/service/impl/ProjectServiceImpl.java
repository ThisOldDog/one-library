package pers.dog.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.control.PropertySheetDialog;
import pers.dog.boot.infra.control.PropertySheetDialogResult;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.domain.entity.Project;
import pers.dog.domain.entity.Recycle;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.domain.repository.RecycleRepository;
import pers.dog.infra.constant.DeleteType;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;
import pers.dog.infra.property.NameProperty;

/**
 * @author 废柴 2022/8/19 15:59
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    private static final String PROJECT_EDITOR_FXML = "project-editor";
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final RecycleRepository recycleRepository;
    private final FileOperationHandler fileHandler;
    @SuppressWarnings("unused")
    @FXMLControl(controller = OneLibraryController.class)
    private TreeView<Project> projectTree;
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;

    public ProjectServiceImpl(ProjectRepository projectRepository, RecycleRepository recycleRepository) {
        this.projectRepository = projectRepository;
        this.recycleRepository = recycleRepository;
        this.fileHandler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/document"));
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
        return createFile(projectType, fileType, null);
    }

    public TreeItem<Project> createFile(ProjectType projectType, FileType fileType, String fileName) {
        TreeItem<Project> parent = currentDirectory();
        return createFile(projectType, fileType, fileName, parent);
    }

    private TreeItem<Project> createFile(ProjectType projectType, FileType fileType, String fileName, TreeItem<Project> parent) {
        Assert.isTrue(ProjectType.DIRECTORY.equals(projectType) || fileType != null, "[Project] When the project type is file, the type of the file cannot be null.");
        NameProperty nameProperty = new NameProperty();
        nameProperty.setName(fileName != null ? fileName : buildFileName(parent, projectType, fileType));
        PropertySheetDialogResult<NameProperty> result = new PropertySheetDialog<>(nameProperty, ButtonType.OK)
                .showAndWait()
                .orElse(null);
        nameProperty = result == null ? null : result.getResult();
        if (nameProperty == null || ObjectUtils.isEmpty(nameProperty.getName())) {
            if (result != null && ButtonType.OK.equals(result.getType())) {
                alertNameEmpty();
            }
            return null;
        }
        if (fileType != null && !nameProperty.getName().endsWith(fileType.getSuffix())) {
            nameProperty.setName(nameProperty.getName() + fileType.getSuffix());
        }
        Project project = new Project()
                .setProjectName(nameProperty.getName())
                .setProjectType(projectType)
                .setFileType(fileType)
                .setParentProjectId(parent.getValue().getProjectId())
                .setSortIndex(parent.getChildren().size() + 1);
        if (projectRepository.findByParentProjectId(project.getParentProjectId())
                .stream()
                .anyMatch(bro -> Objects.equals(bro.getProjectName(), project.getProjectName()))) {
            alertNameDuplicate();
            return null;
        }
        if (ProjectType.DIRECTORY.equals(projectType)) {
            fileHandler.createDirectory(project.getProjectName(), getRelativePath(parent));
        } else {
            fileHandler.createFile(project.getProjectName(), getRelativePath(parent));
        }
        projectRepository.save(project);
        TreeItem<Project> treeItem = new TreeItem<>(project);
        parent.getChildren().add(treeItem);
        expandDirectory(parent);
        projectTree.layout();
        projectTree.getSelectionModel().clearSelection();
        projectTree.getSelectionModel().select(treeItem);
        return treeItem;
    }

    @Override
    public TreeItem<Project> createFile(ProjectType projectType, FileType fileType, String fileName, String markdown) {
        TreeItem<Project> projectTreeItem = createFile(projectType, fileType, fileName);
        if (projectTreeItem != null && !ObjectUtils.isEmpty(markdown)) {
            fileHandler.write(projectTreeItem.getValue().getProjectName(), markdown, getRelativePath(currentDirectory()));
            openFile(projectTreeItem.getValue());
        }
        return projectTreeItem;
    }

    @Override
    public TreeItem<Project> createFile(ProjectType projectType, FileType fileType, String projectName, byte[] content, String[] paths) {
        TreeItem<Project> directory = createDirectory(paths);
        if (directory == null) {
            return null;
        }
        TreeItem<Project> projectTreeItem = createFile(projectType, fileType, projectName, directory);
        if (projectTreeItem != null && content != null && content.length > 0) {
            fileHandler.write(projectTreeItem.getValue().getProjectName(), content, getRelativePath(currentDirectory()));
            openFile(projectTreeItem.getValue());
        }
        return projectTreeItem;
    }

    private TreeItem<Project> createDirectory(String[] paths) {
        return createDirectory(ROOT, paths, 0);
    }

    private TreeItem<Project> createDirectory(TreeItem<Project> parent, String[] paths, int index) {
        if (parent == null || paths == null || index >= paths.length) {
            return parent;
        }
        if (!CollectionUtils.isEmpty(parent.getChildren())) {
            for (TreeItem<Project> child : parent.getChildren()) {
                if (Objects.equals(paths[index], child.getValue().getProjectName())) {
                    return createDirectory(child, paths, index + 1);
                }
            }
        }
        TreeItem<Project> child = createFile(ProjectType.DIRECTORY, null, paths[index], parent);
        return createDirectory(child, paths, index + 1);
    }

    @Override
    public void updateProject(Project project) {
        if (ObjectUtils.isEmpty(project.getNewProjectName()) || Objects.equals(project.getNewProjectName(), project.getProjectName())) {
            return;
        }
        TreeItem<Project> parent = currentParent();
        boolean renamed = fileHandler.rename(project.getProjectName(), project.getNewProjectName(), getRelativePath(parent));
        if (renamed) {
            projectRepository.save(project.setProjectName(project.getNewProjectName()));
        } else {
            Alert renameFiled = new Alert(Alert.AlertType.ERROR);
            renameFiled.setHeaderText(I18nMessageSource.getResource("error"));
            renameFiled.setContentText(I18nMessageSource.getResource("error.project.rename.name_duplicate"));
            renameFiled.showAndWait();
        }
    }

    @Override
    public boolean move(TreeItem<Project> project, TreeItem<Project> parent) {
        Project projectValue = project.getValue();
        if (Objects.equals(projectValue.getParentProjectId(), parent.getValue().getProjectId())) {
            return true;
        }
        if (fileHandler.exists(projectValue.getProjectName(), getRelativePath(parent))) {
            alertNameDuplicate();
            return false;
        } else {
            fileHandler.move(projectValue.getProjectName(), getRelativePath(project.getParent()), getRelativePath(parent));
            projectValue.setParentProjectId(parent.getValue().getProjectId());
            projectRepository.save(projectValue);
            return true;
        }
    }

    @Override
    public void syncLocal() {
        List<Project> projectList = projectRepository.findAll();
        List<Project> matchedList = new ArrayList<>();
        logger.info("[One Library] Start sync local file.");
        AtomicReference<Project> parent = new AtomicReference<>();
        AtomicBoolean root = new AtomicBoolean(false);
        fileHandler.walkFileTree(new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!root.get()) {
                    root.set(true);
                } else {
                    logger.info("[One Library] Visit directory: {}", dir);
                    String dirName = dir.toFile().getName();
                    Project current = null;
                    for (Project project : projectList) {
                        if (!ProjectType.DIRECTORY.equals(project.getProjectType())) {
                            continue;
                        }
                        if ((project.getParentProjectId() == null && parent.get() == null)
                                || (parent.get() != null && Objects.equals(project.getParentProjectId(), parent.get().getProjectId()))) {
                            if (Objects.equals(project.getProjectName(), dirName)) {
                                current = project;
                                break;
                            }
                        }
                    }
                    if (current == null) {
                        current = new Project()
                                .setProjectName(dirName)
                                .setProjectType(ProjectType.DIRECTORY)
                                .setParentProjectId(parent.get() == null ? null : parent.get().getProjectId());
                        current.setSortIndex(getSortIndex(projectList, parent));
                        logger.warn("[One Library] The directory accessed has no records and a record is created: {}", dir);
                        projectRepository.save(current);
                    }
                    matchedList.add(current);
                    parent.set(current.setParent(parent.get()));
                }
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (FileType.UNKNOWN.equals(FileType.identify(file.getFileName().toString()))) {
                    return super.visitFile(file, attrs);
                }
                String dirName = file.toFile().getName();
                Project current = null;
                for (Project project : projectList) {
                    if (!ProjectType.FILE.equals(project.getProjectType())) {
                        continue;
                    }
                    if ((project.getParentProjectId() == null && parent.get() == null)
                            || (parent.get() != null && Objects.equals(project.getParentProjectId(), parent.get().getProjectId()))) {
                        if (Objects.equals(project.getProjectName(), dirName)) {
                            current = project;
                            break;
                        }
                    }
                }
                if (current == null) {
                    current = new Project()
                            .setProjectName(dirName)
                            .setProjectType(ProjectType.FILE)
                            .setFileType(FileType.identify(dirName))
                            .setParentProjectId(parent.get() == null ? null : parent.get().getProjectId());
                    current.setSortIndex(getSortIndex(projectList, parent));
                    logger.warn("[One Library] The file accessed has no records and a record is created: {}", file);
                    projectRepository.save(current);
                }
                matchedList.add(current);
                logger.info("[One Library] Visit file: {}", file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (parent.get() != null) {
                    parent.set(parent.get().getParent());
                }
                return super.postVisitDirectory(dir, exc);
            }
        });
        projectList.removeAll(matchedList);
        Map<Long, Project> parentMap = matchedList.stream().collect(Collectors.toMap(Project::getProjectId, Function.identity()));
        if (!projectList.isEmpty()) {
            logger.warn("[One Library] If no file or directory is found for these records, the records will be deleted: {}",
                    StringUtils.collectionToCommaDelimitedString(projectList.stream().map(project -> getPath(parentMap, project)).collect(Collectors.toList())));
            projectRepository.deleteAll(projectList);
        }
    }

    private String getPath(Map<Long, Project> parentMap, Project project) {
        String path = project.getProjectName();
        Project parent = project;
        while (parent != null) {
            parent = parentMap.get(parent.getParentProjectId());
            if (parent != null) {
                path = parent.getProjectName() + File.separator + path;
            }
        }
        return path;
    }

    private static int getSortIndex(List<Project> projectList, AtomicReference<Project> parent) {
        return projectList.stream().filter(project ->
                        (project.getParentProjectId() == null && parent.get() == null)
                                || (parent.get() != null && Objects.equals(project.getParentProjectId(), parent.get().getProjectId())))
                .mapToInt(Project::getSortIndex).max().orElse(0) + 1;
    }

    @Override
    public void reordered(List<TreeItem<Project>> children) {
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        List<Project> projectList = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            projectList.add(children.get(i).getValue().setSortIndex(i + 1));
        }
        projectRepository.saveAll(projectList);
    }

    @Override
    public void openEditProject() {
        TreeItem<Project> selected = projectTree.getSelectionModel().getSelectedItem();
        Project selectedProject = selected != null && !ROOT.equals(selected) ? selected.getValue() : null;
        if (selectedProject == null) {
            return;
        }
        projectTree.setEditable(true);
        projectTree.edit(selected);
    }

    @Override
    public void deleteProject() {
        List<TreeItem<Project>> selected = projectTree.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(I18nMessageSource.getResource("confirmation"));
        if (selected.size() == 1) {
            if (ProjectType.DIRECTORY.equals(selected.get(0).getValue().getProjectType())) {
                confirmation.setHeaderText(I18nMessageSource.getResource("confirmation.project.delete.project.confirmation.dir", selected.get(0).getValue().getProjectName()));
            } else {
                confirmation.setHeaderText(I18nMessageSource.getResource("confirmation.project.delete.project.confirmation.file", selected.get(0).getValue().getProjectName()));
            }
        } else {
            confirmation.setHeaderText(I18nMessageSource.getResource("confirmation.project.delete.project.confirmation.multi"));
        }
        confirmation.setContentText(I18nMessageSource.getResource("confirmation.project.delete.project.confirmation.prompt"));
        confirmation.getButtonTypes().add(ButtonType.APPLY);
        ((Button) confirmation.getDialogPane().lookupButton(ButtonType.APPLY)).setText(I18nMessageSource.getResource("confirmation.project.delete.project.confirmation.move-recycle"));
        confirmation.showAndWait().ifPresent(buttonType -> {
            if (ButtonType.OK.equals(buttonType) || ButtonType.APPLY.equals(buttonType)) {
                List<TreeItem<Project>> tmp = new ArrayList<>(selected);
                for (TreeItem<Project> projectTreeItem : tmp) {
                    delete(projectTreeItem, ButtonType.OK.equals(buttonType) ? DeleteType.DELETE : DeleteType.RECYCLE);
                }
            }
        });
    }

    @Override
    public void openFile() {
        Project item = currentProjectValue();
        if (item == null) {
            return;
        }
        openFile(item);
    }

    @Override
    public void openFile(Project project) {

        ObservableList<Tab> tabs = projectEditorWorkspace.getTabs();
        for (Tab tab : tabs) {
            if (Objects.equals(((ProjectEditorController) tab.getUserData()).getProject(), project)) {
                Platform.runLater(() -> projectEditorWorkspace.getSelectionModel().select(tab));
                return;
            }
        }
        Parent projectEditor = FXMLUtils.loadFXML(PROJECT_EDITOR_FXML);
        ProjectEditorController projectEditorController = FXMLUtils.getController(projectEditor);
        projectEditorController.initialize(null, null);
        Tab tab = new Tab(project.getSimpleProjectName(), projectEditor);
        tab.setUserData(projectEditorController);
        tab.setId(String.valueOf(project.getProjectId()));
        tab.setOnClosed(event -> projectEditorController.close());
        projectEditorController.dirtyProperty().addListener((change, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                tab.setText("* " + project.getSimpleProjectName());
            } else {
                tab.setText(project.getSimpleProjectName());
            }
        });
        tab.setOnCloseRequest(tabCloseEvent -> {
            if (projectEditorController.getDirty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(I18nMessageSource.getResource("confirmation"));
                alert.setHeaderText(I18nMessageSource.getResource("confirmation.project.close.dirty"));
                alert.setContentText(I18nMessageSource.getResource("confirmation.project.close.prompt"));
                alert.showAndWait()
                        .ifPresent(buttonType -> {
                            if (ButtonType.CANCEL.equals(buttonType)) {
                                tabCloseEvent.consume();
                            }
                        });
            }
        });
        Platform.runLater(() -> {
            tabs.add(tab);
            projectEditorWorkspace.getSelectionModel().select(tab);
            projectEditorController.show(project);
        });
    }

    @Override
    public TreeItem<Project> currentDirectory() {
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

    @Override
    public TreeItem<Project> currentProject() {
        Tab selectedItem = projectEditorWorkspace.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return null;
        }
        Project project = ((ProjectEditorController) selectedItem.getUserData()).getProject();
        return find(projectTree.getRoot(), project);
    }

    private TreeItem<Project> find(TreeItem<Project> root, Project project) {
        if (root == null || CollectionUtils.isEmpty(root.getChildren())) {
            return null;
        }
        for (TreeItem<Project> child : root.getChildren()) {
            if (Objects.equals(child.getValue().getProjectId(), project.getProjectId())) {
                return child;
            }
            TreeItem<Project> projectTreeItem = find(child, project);
            if (projectTreeItem != null) {
                return projectTreeItem;
            }
        }
        return null;
    }

    @Override
    public List<Project> dirtyProject() {
        return projectEditorWorkspace.getTabs()
                .stream()
                .map(tab -> (ProjectEditorController) tab.getUserData())
                .filter(ProjectEditorController::getDirty)
                .map(ProjectEditorController::getProject)
                .toList();
    }

    @Override
    public void saveAll() {
        projectEditorWorkspace.getTabs()
                .stream()
                .map(tab -> (ProjectEditorController) tab.getUserData())
                .filter(ProjectEditorController::getDirty)
                .forEach(ProjectEditorController::save);
    }

    @Override
    public Path documentDir() {
        return fileHandler.directory();
    }

    @Override
    public void openInExplorer() {
        TreeItem<Project> projectTreeItem = currentParent();
        String[] relativePath = getRelativePath(projectTreeItem);
        Path target = fileHandler.directory();
        for (String itemPath : relativePath) {
            target = target.resolve(itemPath);
        }
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + target);
        } catch (IOException e) {
            logger.error("[Project] Open in explorer failed: " + target, e);
        }
    }

    public Project currentProjectValue() {
        TreeItem<Project> selectedItem = projectTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            return null;
        }
        return selectedItem.getValue();
    }

    private TreeItem<Project> currentParent() {
        TreeItem<Project> parentNode = projectTree.getSelectionModel().getSelectedItem();
        if (parentNode == null || parentNode.getValue() == null) {
            return ROOT;
        }
        return parentNode.getParent();
    }

    private void delete(TreeItem<Project> selected, DeleteType deleteType) {
        TreeItem<Project> parent = selected.getParent();
        deleteFile(selected, deleteType);
        deleteData(selected);
        deleteView(selected);
        ObservableList<TreeItem<Project>> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            TreeItem<Project> child = children.get(i);
            child.getValue().setSortIndex(i + 1);
            projectRepository.save(child.getValue());
        }
    }

    private void deleteFile(TreeItem<Project> selected, DeleteType deleteType) {
        for (TreeItem<Project> child : selected.getChildren()) {
            deleteFile(child, deleteType);
        }
        Project project = selected.getValue();
        String[] relativePath = getRelativePath(selected.getParent());
        if (DeleteType.RECYCLE.equals(deleteType) && ProjectType.FILE.equals(project.getProjectType())) {
            recycleRepository.save(new Recycle()
                    .setProjectName(project.getProjectName())
                    .setFileType(project.getFileType())
                    .setLocation(String.join(File.separator, relativePath))
                    .setDeleteDateTime(ZonedDateTime.now())
                    .setContent(fileHandler.read(project.getProjectName(), relativePath))
            );
        }
        fileHandler.delete(project.getProjectName(), relativePath);
    }

    private void deleteData(TreeItem<Project> selected) {
        projectRepository.deleteById(selected.getValue().getProjectId());
        for (TreeItem<Project> child : selected.getChildren()) {
            deleteData(child);
        }
    }

    private void deleteView(TreeItem<Project> selected) {
        for (TreeItem<Project> child : selected.getChildren()) {
            closeView(child);
        }
        selected.getParent().getChildren().remove(selected);
    }

    private void closeView(TreeItem<Project> selected) {
        for (Tab tab : projectEditorWorkspace.getTabs()) {
            if (Objects.equals(((ProjectEditorController) tab.getUserData()).getProject().getProjectId(), selected.getValue().getProjectId())) {
                projectEditorWorkspace.getTabs().remove(tab);
                break;
            }
        }
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
                    projectName = I18nMessageSource.getResource("info.project.default_file_name", fileType.getName());
                } else {
                    projectName = I18nMessageSource.getResource("info.project.default_file_name_duplicate", fileType.getName(), fileIndex);
                }
            } else {
                if (fileIndex == 0) {
                    projectName = I18nMessageSource.getResource("info.project.default_directory_name");
                } else {
                    projectName = I18nMessageSource.getResource("info.project.default_directory_name_duplicate", fileIndex);
                }
            }
            if (fileHandler.exists(fileType != null ? (projectName + fileType.getSuffix()) : projectName, getRelativePath(parent))) {
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

    private void alertNameEmpty() {
        Alert nameDuplicateAlert = new Alert(Alert.AlertType.ERROR);
        nameDuplicateAlert.setHeaderText(I18nMessageSource.getResource("error"));
        nameDuplicateAlert.setContentText(I18nMessageSource.getResource("error.project.name.empty"));
        nameDuplicateAlert.show();
    }

    private static void alertNameDuplicate() {
        Alert nameDuplicateAlert = new Alert(Alert.AlertType.ERROR);
        nameDuplicateAlert.setHeaderText(I18nMessageSource.getResource("error"));
        nameDuplicateAlert.setContentText(I18nMessageSource.getResource("error.project.name.duplicate"));
        nameDuplicateAlert.show();
    }
}
