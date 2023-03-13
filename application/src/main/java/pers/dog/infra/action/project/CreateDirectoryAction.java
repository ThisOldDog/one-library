package pers.dog.infra.action.project;

import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class CreateDirectoryAction extends Action {
    private final ProjectService projectService;

    private CreateDirectoryAction(ProjectService projectService) {
        super(I18nMessageSource.getResource("info.project.create.directory"));
        this.projectService = projectService;
        super.setEventHandler(this::createMarkdownField);
    }

    public void createMarkdownField(ActionEvent event) {
        projectService.createFile(ProjectType.DIRECTORY, null);
    }
}
