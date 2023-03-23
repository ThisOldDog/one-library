package pers.dog.api.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import pers.dog.app.service.ProjectEditorService;
import pers.dog.domain.entity.Project;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/23 21:51
 */
public class ProjectEditorController {

    @FXML
    private VBox editorWorkspace;

    private final ProjectEditorService projectEditorService;

    public ProjectEditorController(ProjectEditorService projectEditorService) {
        this.projectEditorService = projectEditorService;
    }

    public void setProject(Project project) {
        projectEditorService.show(project);
    }

}
