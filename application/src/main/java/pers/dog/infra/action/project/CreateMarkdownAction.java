package pers.dog.infra.action.project;

import javafx.event.ActionEvent;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class CreateMarkdownAction extends Action {
    private final ProjectService projectService;

    private CreateMarkdownAction(ProjectService projectService) {
        super(I18nMessageSource.getResource("info.project.create.file.markdown"));
        this.projectService = projectService;
        super.setEventHandler(this::createMarkdownField);
    }

    public void createMarkdownField(ActionEvent event) {
        projectService.createFile(ProjectType.FILE, FileType.MARKDOWN);
    }
}
