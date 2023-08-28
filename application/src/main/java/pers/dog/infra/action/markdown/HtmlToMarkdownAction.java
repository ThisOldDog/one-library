package pers.dog.infra.action.markdown;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.controlsfx.control.action.Action;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.api.controller.markdown.HtmlToMarkdownController;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2022/6/2 22:40
 */
@Component
@Scope("prototype")
public class HtmlToMarkdownAction extends Action {
    private static final String VIEW = "markdown/html-to-markdown";
    @FXMLControl(controller = OneLibraryController.class)
    private final ControlProvider<TabPane> projectEditorWorkspace = new ControlProvider<>();

    private final ProjectService projectService;

    private boolean saveToProject;

    private HtmlToMarkdownAction(ProjectService projectService) {
        super(I18nMessageSource.getResource("info.project.html-to-markdown"));
        this.projectService = projectService;
        super.setEventHandler(this::onAction);
        projectEditorWorkspace.afterAssignment(tabPane -> Platform.runLater(() -> {
            tabPane.getSelectionModel().selectedItemProperty().addListener((change, oldValue, newValue) -> setDisabled(isDisable()));
            setDisabled(isDisable());
        }));
    }

    private boolean isDisable() {
        if (saveToProject) {
            if (projectEditorWorkspace.get() == null) {
                return true;
            }
            return projectEditorWorkspace.get().getSelectionModel().isEmpty();
        }
        return false;
    }

    public void onAction(ActionEvent event) {
        Parent parent = FXMLUtils.loadFXML(VIEW);
        HtmlToMarkdownController controller = FXMLUtils.getController(parent);
        if (saveToProject) {
            controller.saveToProject();
        } else {
            controller.saveToDirectory();
        }


        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(getText());
        dialog.setResizable(true);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.getButtonTypes().addAll(ButtonType.NEXT, ButtonType.OK, ButtonType.CANCEL);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        Button previewButton = (Button) dialogPane.lookupButton(ButtonType.NEXT);
        okButton.setDisable(true);
        okButton.addEventFilter(ActionEvent.ACTION, okAction -> {
            String markdown = controller.getMarkdown();
            if (saveToProject) {
                ((ProjectEditorController) projectEditorWorkspace.get().getSelectionModel().getSelectedItem().getUserData()).replaceSelection(markdown);
            } else {
                String fileName = controller.projectName.getText();
                if (fileName != null && !fileName.endsWith(FileType.MARKDOWN.getSuffix())) {
                    fileName += FileType.MARKDOWN.getSuffix();
                }
                if (projectService.createFile(ProjectType.FILE, FileType.MARKDOWN, fileName, markdown) == null) {
                    okAction.consume();
                }
            }
        });
        previewButton.setDisable(true);
        previewButton.setText(I18nMessageSource.getResource("info.project.html-to-markdown.preview"));
        previewButton.addEventFilter(ActionEvent.ACTION, previewAction -> {
            controller.preview();
            previewAction.consume();
        });

        controller.getUrl().textProperty().addListener((observable, oldValue, newValue) -> previewButton.setDisable(ObjectUtils.isEmpty(newValue)));
        controller.getMarkdownPreview().textProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(ObjectUtils.isEmpty(newValue)));

        dialog.showAndWait();
    }

    public ControlProvider<TabPane> getProjectEditorWorkspace() {
        return projectEditorWorkspace;
    }

    public boolean isSaveToProject() {
        return saveToProject;
    }

    public HtmlToMarkdownAction setSaveToProject(boolean saveToProject) {
        this.saveToProject = saveToProject;
        return this;
    }
}
