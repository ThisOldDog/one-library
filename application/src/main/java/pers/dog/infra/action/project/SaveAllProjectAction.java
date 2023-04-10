package pers.dog.infra.action.project;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Pair;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class SaveAllProjectAction extends Action {
    @FXMLControl(controller = OneLibraryController.class)
    private final ControlProvider<TabPane> projectEditorWorkspace = new ControlProvider<>();

    private SaveAllProjectAction() {
        super(I18nMessageSource.getResource("info.project.save.project_all"));
        super.setEventHandler(this::saveProject);
        projectEditorWorkspace.afterAssignment(tabPane -> {
            tabPane.getTabs().addListener((ListChangeListener<? super Tab>) change -> {
                while (change.next()) {
                    setDisabled(CollectionUtils.isEmpty(change.getList()));
                }
            });
        });
    }

    public void saveProject(ActionEvent event) {
        for (Tab tab : projectEditorWorkspace.get().getTabs()) {
            ((ProjectEditorController) tab.getUserData()).save();
        }
    }
}
