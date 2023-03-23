package pers.dog.app.service.impl;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.api.controller.ProjectEditorLineController;
import pers.dog.app.service.ProjectEditorService;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/23 22:26
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProjectEditorServiceImpl implements ProjectEditorService {
    private static final String PROJECT_EDITOR_LINE_FXML = "project-editor-line";
    private final ObjectProperty<Project> projectProperty = new ReadOnlyObjectWrapper<>();
    private final ObservableList<String> textList = FXCollections.observableList(new LinkedList<>());
    private final ObservableList<Pair<Parent, Object>> lineControllerList = FXCollections.observableArrayList();

    private final ProjectRepository projectRepository;
    private FileOperationHandler fileOperationHandler;
    @FXMLControl(controller = ProjectEditorController.class)
    private VBox editorWorkspace;

    public ProjectEditorServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void show(Project project) {
        setProjectProperty(project);
        setFileOperationHandler(project);
        setTextList();
    }

    private void setProjectProperty(Project project) {
        projectProperty.set(project);
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

    private void setTextList() {
        textList.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    ObservableList<Node> lineList = editorWorkspace.getChildren();
                    if (lineList.size() < change.getFrom()) {
                        for (int i = lineList.size(); i < change.getFrom(); i++) {
                            addLine("");
                        }
                    }
                    List<? extends String> addedSubList = change.getAddedSubList();
                    for (String lineText : addedSubList) {
                        addLine(lineText);
                    }
                }
            }
        });
        List<String> textValueList = fileOperationHandler.readAllLines(projectProperty.get().getProjectName());
        if (textList.isEmpty()) {
            textList.add("");
        } else {
            textList.addAll(textValueList);
        }
    }

    private void addLine(String text) {
        ObservableList<Node> lineList = editorWorkspace.getChildren();
        Parent parent = FXMLUtils.loadFXML(PROJECT_EDITOR_LINE_FXML);
        ProjectEditorLineController controller = FXMLUtils.getController(parent);
        lineList.add(parent);
        controller.setLineNumber(lineList.size());
        controller.setLineText(text);
    }
}
