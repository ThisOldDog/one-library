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
public class OpenInExplorerProjectAction extends Action {
    private final ProjectService projectService;

    private OpenInExplorerProjectAction(ProjectService projectService) {
        super(I18nMessageSource.getResource("info.project.open-in.explorer"));
        this.projectService = projectService;
        super.setEventHandler(this::doAction);
    }

    public void doAction(ActionEvent event) {
        projectService.openInExplorer();
    }
}
