package pers.dog.app.service;

import java.nio.file.Path;
import java.util.List;

import javafx.scene.control.TreeItem;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2022/8/19 15:59
 */
public interface ProjectService {
    TreeItem<Project> ROOT = new TreeItem<>(new Project().setProjectName(I18nMessageSource.getResource("info.project.root")).setProjectType(ProjectType.DIRECTORY));

    TreeItem<Project> tree();

    TreeItem<Project> createFile(ProjectType projectType, FileType fileType);

    TreeItem<Project> createFile(ProjectType projectType, FileType fileType, String fileName, String markdown);

    TreeItem<Project> createFile(ProjectType projectType, FileType fileType, String projectName, byte[] content, String[] paths);

    void updateProject(Project project);

    boolean move(TreeItem<Project> project, TreeItem<Project> parent);

    void syncLocal();

    void reordered(List<TreeItem<Project>> children);

    void openEditProject();

    void deleteProject();

    void openFile();

    void openFile(Project project);

    TreeItem<Project> currentDirectory();

    TreeItem<Project> currentProject();

    List<Project> dirtyProject();

    void saveAll();

    Path documentDir();

    void openInExplorer();
}
