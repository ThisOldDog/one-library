package pers.dog.infra.action.project;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TabPane;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class ExportProjectToHtmlAction extends Action {
    @FXMLControl(controller = OneLibraryController.class)
    private final ControlProvider<TabPane> projectEditorWorkspace = new ControlProvider<>();

    private ExportProjectToHtmlAction() {
        super(I18nMessageSource.getResource("info.project.export.to-html"));
        super.setEventHandler(this::saveProject);
        projectEditorWorkspace.afterAssignment(tabPane -> Platform.runLater(() -> {
            tabPane.getSelectionModel().selectedItemProperty().addListener((change, oldValue, newValue) -> setDisabled(newValue == null));
            setDisabled(tabPane.getSelectionModel().isEmpty());
        }));
    }

    public void saveProject(ActionEvent event) {
        ((ProjectEditorController) projectEditorWorkspace.get().getSelectionModel().getSelectedItem().getUserData()).exportToHtml();
    }
}
