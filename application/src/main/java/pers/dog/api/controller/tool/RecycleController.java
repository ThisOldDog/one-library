package pers.dog.api.controller.tool;

import pers.dog.app.service.ProjectService;

/**
 * @author 废柴 2023/9/22 14:51
 */
public class RecycleController {
    private final ProjectService projectService;

    public RecycleController(ProjectService projectService) {
        this.projectService = projectService;
    }
}
