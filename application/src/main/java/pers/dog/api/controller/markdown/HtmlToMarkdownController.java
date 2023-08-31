package pers.dog.api.controller.markdown;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.PrefixSelectionComboBox;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.infra.dto.ValueMeaning;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.AlertUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.control.MarkdownCodeArea;

/**
 * @author 废柴 2023/8/21 19:53
 */
public class HtmlToMarkdownController implements Initializable {
    private static final ObservableList<ValueMeaning> INSERT_POSITION_ALL = FXCollections.observableArrayList(
            new ValueMeaning()
                    .setValue("NEW_PROJECT")
                    .setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.new_project")),
            new ValueMeaning()
                    .setValue("CURSOR")
                    .setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.cursor"))
    );

    @FXML
    public TextArea url;
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> insertPosition;
    @FXML
    public WebView contentPreview;
    @FXML
    public MarkdownCodeArea markdownPreview;
    @FXML
    public MaskerPane masker;
    @FXML
    public TextField directory;
    @FXML
    public TextField projectName;
    private final FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();

    private final ProjectService projectService;

    public HtmlToMarkdownController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentPreview.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                try {
                    String markdown = converter.convert(String.valueOf(contentPreview.getEngine().executeScript("document.documentElement.outerHTML")));
                    markdownPreview.replaceText(markdown);
                } finally {
                    masker.setVisible(false);
                }
            } else if (newValue == Worker.State.FAILED) {
                masker.setVisible(false);
                AlertUtils.showException("info.project.html-to-markdown.html.error.title",
                        "info.project.html-to-markdown.html.error.header_text",
                        "info.project.html-to-markdown.html.error.content_text",
                        "info.project.html-to-markdown.html.error.exception_stacktrace",
                        contentPreview.getEngine().getLoadWorker().getException());
            }
        });
    }

    public void saveToDirectory() {
        insertPosition.setItems(INSERT_POSITION_ALL);
        insertPosition.setValue(INSERT_POSITION_ALL.get(0));
        directory.setText(getProjectTreeDirectory());
        projectName.setDisable(false);
    }

    public void saveToProject() {
        insertPosition.setItems(INSERT_POSITION_ALL);
        insertPosition.setValue(INSERT_POSITION_ALL.get(1));
        directory.setText(getProjectTreeDirectory());
        projectName.setText(projectService.currentProject().getValue().getSimpleProjectName());
    }


    private String getProjectTreeDirectory() {
        TreeItem<Project> projectTreeItem = projectService.currentDirectory();
        if (ProjectService.ROOT == projectTreeItem) {
            return projectTreeItem.getValue().getProjectName();
        }
        return getProjectTreeDirectory(null, projectTreeItem);
    }

    private String getProjectTreeDirectory(String path, TreeItem<Project> node) {
        if (node == null || ProjectService.ROOT == node) {
            return path;
        }
        if (node.getValue() != null) {
            path = path == null ? node.getValue().getProjectName() : (node.getValue().getProjectName() + File.separator + path);
        }
        return getProjectTreeDirectory(path, node.getParent());
    }

    public TextArea getUrl() {
        return url;
    }

    public PrefixSelectionComboBox<ValueMeaning> getInsertPosition() {
        return insertPosition;
    }

    public WebView getContentPreview() {
        return contentPreview;
    }

    public MarkdownCodeArea getMarkdownPreview() {
        return markdownPreview;
    }

    public void preview() {
        masker.setVisible(true);
        markdownPreview.clear();
        contentPreview.getEngine().load(url.getText());
    }

    public String getMarkdown() {
        return markdownPreview.getText();
    }
}
