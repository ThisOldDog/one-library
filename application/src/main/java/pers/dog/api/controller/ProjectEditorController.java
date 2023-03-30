package pers.dog.api.controller;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/23 23:06
 */
public class ProjectEditorController implements Initializable {
    private final ObjectProperty<Project> projectProperty = new SimpleObjectProperty<>();
    private final ProjectRepository projectRepository;
    @FXML
    public CodeArea codeArea;
    private FileOperationHandler fileOperationHandler;

    public ProjectEditorController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    }

    public void show(Project project) {
        setProjectProperty(project);
        setFileOperationHandler(project);
        setText(project);
    }

    private void setProjectProperty(Project project) {
        projectProperty.set(project);
    }

    private void setText(Project project) {
        String text = fileOperationHandler.read(projectProperty.get().getProjectName(), String.class);
        if (text != null) {
            codeArea.replaceText(0, 0, text);
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
}
