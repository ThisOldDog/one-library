package pers.dog.infra.action.editor;

import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TabPane;
import org.controlsfx.control.action.Action;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;

public abstract class BaseEditorAction extends Action {
    @FXMLControl(controller = OneLibraryController.class)
    private final ControlProvider<TabPane> projectEditorWorkspace = new ControlProvider<>();
    private final Consumer<ActionEvent> actionEventConsumer;

    protected BaseEditorAction(String text, Consumer<ActionEvent> actionEventConsumer) {
        super(I18nMessageSource.getResource(text));
        super.setEventHandler(this::doAction);
        this.actionEventConsumer = actionEventConsumer;
        projectEditorWorkspace.afterAssignment(tabPane -> Platform.runLater(() -> {
            tabPane.getSelectionModel().selectedItemProperty().addListener((change, oldValue, newValue) -> setDisabled(newValue == null));
            setDisabled(tabPane.getSelectionModel().isEmpty());
        }));
    }

    private void doAction(ActionEvent actionEvent) {
        actionEventConsumer.accept(actionEvent);
    }
}
