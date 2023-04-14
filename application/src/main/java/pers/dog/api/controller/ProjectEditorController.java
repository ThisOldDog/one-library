package pers.dog.api.controller;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.infra.control.MarkdownCodeArea;
import pers.dog.infra.control.MarkdownPreviewArea;

/**
 * @author 废柴 2023/3/23 23:06
 */
public class ProjectEditorController implements Initializable {

    private final ObjectProperty<Project> projectProperty = new SimpleObjectProperty<>();
    private final ProjectRepository projectRepository;
    private final ObjectProperty<Boolean> dirty = new SimpleObjectProperty<>(false);

    @FXML
    public MarkdownCodeArea codeArea;
    @FXML
    public MarkdownPreviewArea previewArea;
    private FileOperationHandler fileOperationHandler;
    private String localText;

    public ProjectEditorController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void show(Project project) {
        setProjectProperty(project);
        setFileOperationHandler(project);
        Platform.runLater(() -> {
            bindPreview();
            setText(project);
            setChangeListener();
        });
    }

    private void bindPreview() {
        Bindings.bindContent(codeArea.getParagraphs(), previewArea.getRowContent());
    }

    private void setChangeListener() {
        codeArea.textProperty().addListener((change, oldValue, newValue) -> {
            if (!dirty.get()) {
                dirty.setValue(!Objects.equals(newValue, localText));
            }
        });
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
}
