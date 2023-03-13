package pers.dog.api.item;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import pers.dog.boot.infra.util.ImageUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.ProjectType;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/6 14:23
 */
public class ProjectItemController {
    @FXML
    public HBox projectItemNode;
    @FXML
    private ImageView projectIcon;
    @FXML
    public Label projectName;
    @FXML
    public TextField projectNameEditor;
    public void setProject(Project project, boolean editing) {
        // 设置图标
        if (ProjectType.DIRECTORY.equals(project.getProjectType())) {
            projectIcon.setImage(ImageUtils.getImage(ProjectType.DIRECTORY.getIcon()));
        } else {
            projectIcon.setImage(ImageUtils.getImage(project.getFileType().getIcon()));
        }
        // 设置名称
        projectNameEditor.setText(project.getSimpleProjectName());
        projectName.setText(project.getSimpleProjectName());
        projectItemNode.getChildren().remove(1, projectItemNode.getChildren().size());
        if (editing) {
            projectItemNode.setPrefHeight(24);
            projectItemNode.getChildren().add(projectNameEditor);
        } else {
            projectItemNode.setPrefHeight(16);
            projectItemNode.getChildren().add(projectName);
        }
    }
}
