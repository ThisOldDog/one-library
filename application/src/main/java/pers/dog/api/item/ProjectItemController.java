package pers.dog.api.item;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import pers.dog.boot.infra.util.ImageUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.ProjectType;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/6 14:23
 */
public class ProjectItemController {
    @FXML
    private ImageView projectIcon;
    @FXML
    public Label projectName;
    public void showProject(Project project) {
        // 设置图标
        if (ProjectType.DIRECTORY.equals(project.getProjectType())) {
            projectIcon.setImage(ImageUtils.getImage(ProjectType.DIRECTORY.getIcon()));
        } else {
            projectIcon.setImage(ImageUtils.getImage(project.getFileType().getIcon()));
        }
        projectName.setText(project.getSimpleProjectName());
    }
}
