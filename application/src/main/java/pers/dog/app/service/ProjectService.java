package pers.dog.app.service;

import java.util.List;

import javafx.scene.control.TreeItem;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2022/8/19 15:59
 */
public interface ProjectService {
    TreeItem<Project> tree();

    TreeItem<Project> createFile(ProjectType projectType, FileType fileType);

    void updateProject(Project project);

    boolean move(TreeItem<Project> project, TreeItem<Project> parent);

    void syncLocal();

    void reordered(List<TreeItem<Project>> children);

    void openEditProject();

    void deleteProject();
}
