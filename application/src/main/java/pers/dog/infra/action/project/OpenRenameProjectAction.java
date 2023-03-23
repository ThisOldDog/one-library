package pers.dog.infra.action.project;

import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class OpenRenameProjectAction extends Action {
    private final ProjectService projectService;

    private OpenRenameProjectAction(ProjectService projectService) {
        super(I18nMessageSource.getResource("info.project.rename.project"));
        this.projectService = projectService;
        super.setEventHandler(this::openEditProject);
    }

    public void openEditProject(ActionEvent event) {
        projectService.openEditProject();
    }
}
