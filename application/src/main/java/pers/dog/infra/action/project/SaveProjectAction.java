package pers.dog.infra.action.project;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class SaveProjectAction extends Action {
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;

    private SaveProjectAction() {
        super(I18nMessageSource.getResource("info.project.save.project"));
        super.setEventHandler(this::saveProject);
    }

    public void saveProject(ActionEvent event) {
        for (Tab tab : projectEditorWorkspace.getTabs()) {
            ((ProjectEditorController) tab.getUserData()).save();
        }

    }
}
