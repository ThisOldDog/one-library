package pers.dog.app.service;

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

    void openEditProject();

    void deleteProject();
}
