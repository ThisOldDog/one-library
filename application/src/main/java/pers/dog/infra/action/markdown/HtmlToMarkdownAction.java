package pers.dog.infra.action.markdown;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TabPane;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.api.controller.markdown.HtmlToMarkdownController;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
public class HtmlToMarkdownAction extends Action {
    private static final String VIEW = "markdown/html-to-markdown";
    @FXMLControl(controller = OneLibraryController.class)
    private final ControlProvider<TabPane> projectEditorWorkspace = new ControlProvider<>();

    private HtmlToMarkdownAction() {
        super(I18nMessageSource.getResource("info.project.html-to-markdown"));
        super.setEventHandler(this::onAction);
        projectEditorWorkspace.afterAssignment(tabPane -> Platform.runLater(() -> {
            tabPane.getSelectionModel().selectedItemProperty().addListener((change, oldValue, newValue) -> setDisabled(newValue == null));
            setDisabled(tabPane.getSelectionModel().isEmpty());
        }));
    }

    public void onAction(ActionEvent event) {
        Parent parent = FXMLUtils.loadFXML(VIEW);
        HtmlToMarkdownController controller = FXMLUtils.getController(parent);
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(getText());
        dialog.setResizable(true);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.getButtonTypes().addAll(ButtonType.NEXT, ButtonType.OK, ButtonType.CANCEL);

        Node previewButton = dialogPane.lookupButton(ButtonType.NEXT);
        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        previewButton.setDisable(true);
        okButton.setDisable(true);
        previewButton.addEventFilter(ActionEvent.ACTION, previewAction -> {
            controller.preview();
            previewAction.consume();
        });

        controller.getUrl().textProperty().addListener((observable, oldValue, newValue) -> previewButton.setDisable(ObjectUtils.isEmpty(newValue)));
        controller.getMarkdownPreview().textProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(ObjectUtils.isEmpty(newValue)));

        dialog.setResultConverter(buttonType -> {
            if (ButtonType.CANCEL.equals(buttonType)) {
                return null;
            }
            return controller.getMarkdown();
        });
        dialog.showAndWait().ifPresent(markdown ->
                ((ProjectEditorController) projectEditorWorkspace.get().getSelectionModel().getSelectedItem().getUserData()).replaceSelection(markdown)
        );
    }
}
