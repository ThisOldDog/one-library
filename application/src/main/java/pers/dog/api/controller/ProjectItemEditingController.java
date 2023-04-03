package pers.dog.api.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import pers.dog.boot.infra.util.ImageUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2023/3/6 14:23
 */
public class ProjectItemEditingController {
    @FXML
    private ImageView projectIcon;
    @FXML
    public TextField projectNameEditor;


    public void showProject(Project project) {
        // 设置图标
        if (ProjectType.DIRECTORY.equals(project.getProjectType())) {
            projectIcon.setImage(ImageUtils.getImage(ProjectType.DIRECTORY.getIcon()));
        } else {
            projectIcon.setImage(ImageUtils.getImage(project.getFileType().getIcon()));
        }
        // 设置名称
        projectNameEditor.setText(project.getSimpleProjectName());
    }

    public ImageView getProjectIcon() {
        return projectIcon;
    }

    public ProjectItemEditingController setProjectIcon(ImageView projectIcon) {
        this.projectIcon = projectIcon;
        return this;
    }

    public TextField getProjectNameEditor() {
        return projectNameEditor;
    }

    public ProjectItemEditingController setProjectNameEditor(TextField projectNameEditor) {
        this.projectNameEditor = projectNameEditor;
        return this;
    }
}
