package pers.dog.api.controller;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.dialog.PropertySheetDialog;
import pers.dog.boot.infra.util.PlatformUtils;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.infra.control.MarkdownCodeArea;
import pers.dog.infra.property.HeaderProperty;

/**
 * @author 废柴 2023/3/23 23:06
 */
public class ProjectEditorController implements Initializable {
    private static final String STYLE_CLASS_BUTTON_SAVE_DIRTY = "button-save-dirty";
    private final ObjectProperty<Project> projectProperty = new SimpleObjectProperty<>();
    private final ProjectRepository projectRepository;
    private final ObjectProperty<Boolean> dirty = new SimpleObjectProperty<>(false);
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final DataHolder markdownParserOptions = PegdownOptionsAdapter.flexmarkOptions(PegdownExtensions.ALL);
    private final Parser parser = Parser.builder(markdownParserOptions).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder(markdownParserOptions).build();

    @FXML
    public MarkdownCodeArea codeArea;
    @FXML
    public WebView previewArea;
    @FXML
    public Button saveButton;

    private FileOperationHandler fileOperationHandler;
    private String localText;
    private WebEngine engine;

    public ProjectEditorController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.engine = previewArea.getEngine();
    }

    public void show(Project project) {
        setProjectProperty(project);
        setFileOperationHandler(project);
        Platform.runLater(() -> {
            setChangeListener();
            setText(project);
            loaded.set(true);
        });
    }

    private void setChangeListener() {
        dirty.addListener((change, oldValue, newValue) -> {
            if (newValue) {
                saveButton.getStyleClass().add(STYLE_CLASS_BUTTON_SAVE_DIRTY);
            } else {
                saveButton.getStyleClass().remove(STYLE_CLASS_BUTTON_SAVE_DIRTY);
            }
        });
        codeArea.textProperty().addListener((change, oldValue, newValue) -> {
            if (loaded.get() && !dirty.get()) {
                dirty.setValue(!Objects.equals(newValue, localText));
            }
            refreshPreview(newValue);
        });
    }

    private void refreshPreview(String newValue) {
        PlatformUtils.runLater("RefreshPreview", Duration.seconds(1), () ->
                engine.loadContent(toHtml(newValue))
        );
    }

    private String toHtml(String markdownContent) {
        return renderer.render(parser.parse(markdownContent));
    }

    private void setProjectProperty(Project project) {
        projectProperty.set(project);
    }

    private void setText(Project project) {
        String text = fileOperationHandler.read(project.getProjectName(), String.class);
        if (text != null) {
            codeArea.appendText(text);
            localText = codeArea.getText();
        }
    }

    private void setFileOperationHandler(Project project) {
        Map<Long, Project> projectMap = projectRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Project::getProjectId, Function.identity()));
        StringBuilder pathPrefix = new StringBuilder(".data/document");
        Project parent = projectMap.get(project.getParentProjectId());
        ArrayDeque<String> path = new ArrayDeque<>();
        while (parent != null) {
            path.addFirst(parent.getProjectName());
            parent = projectMap.get(parent.getParentProjectId());
        }
        if (!path.isEmpty()) {
            for (String item : path) {
                pathPrefix.append("/").append(item);
            }
        }
        fileOperationHandler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(pathPrefix.toString()));
    }

    public boolean getDirty() {
        return dirty.get();
    }

    public ObjectProperty<Boolean> dirtyProperty() {
        return dirty;
    }

    public Project getProject() {
        return projectProperty.get();
    }

    // ToolBar
    public void quash(ActionEvent actionEvent) {
        codeArea.undo();
    }

    public void redo(ActionEvent actionEvent) {
        codeArea.redo();
    }

    public void save() {
        String localText = codeArea.getText();
        fileOperationHandler.write(projectProperty.get().getProjectName(), localText);
        dirtyProperty().set(false);
    }

    private void wrapSelection(String leftSymbol, String rightSymbol) {
        Platform.runLater(() -> {
            String selectedText = codeArea.getSelectedText();
            IndexRange selection = codeArea.getSelection();
            int offset = leftSymbol.length();
            if (selectedText == null) {
                selectedText = leftSymbol + rightSymbol;
            } else {
                selectedText = leftSymbol + selectedText + rightSymbol;
            }
            codeArea.replaceText(selection, selectedText);
            codeArea.requestFocus();
            codeArea.moveTo(selection.getEnd() + offset);
            codeArea.selectRange(selection.getStart() + offset, selection.getEnd() + offset);
        });
    }

    public void bold(ActionEvent actionEvent) {
        wrapSelection("**", "**");
    }

    public void italic(ActionEvent actionEvent) {
        wrapSelection("*", "*");
    }

    public void strikethrough(ActionEvent actionEvent) {
        wrapSelection("~~", "~~");
    }

    public void header(ActionEvent actionEvent) {
        new PropertySheetDialog<>(new HeaderProperty())
                .showAndWait()
                .ifPresent(headerProperty -> {
                    System.out.println(headerProperty.getLevel());
                });
    }
}
