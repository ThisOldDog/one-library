package pers.dog.api.controller;

import java.net.URL;
import java.util.*;
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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.apache.commons.lang3.BooleanUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.dialog.PropertySheetDialog;
import pers.dog.boot.infra.util.PlatformUtils;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.infra.control.MarkdownCodeArea;
import pers.dog.infra.property.HeaderProperty;
import pers.dog.infra.property.ImageProperty;
import pers.dog.infra.property.LinkProperty;
import pers.dog.infra.property.TableProperty;

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
    public SplitPane projectEditorWorkspace;
    @FXML
    public VirtualizedScrollPane<MarkdownCodeArea> codeAreaWorkspace;
    @FXML
    public MarkdownCodeArea codeArea;
    @FXML
    public WebView previewArea;
    @FXML
    public Button saveButton;
    @FXML
    public Button onlyEditorButton;
    @FXML
    public Button onlyPreviewButton;
    @FXML
    public Button editorAndPreviewButton;

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
            if (BooleanUtils.isTrue(newValue)) {
                saveButton.getStyleClass().add(STYLE_CLASS_BUTTON_SAVE_DIRTY);
            } else {
                saveButton.getStyleClass().remove(STYLE_CLASS_BUTTON_SAVE_DIRTY);
            }
        });
        codeArea.textProperty().addListener((change, oldValue, newValue) -> {
            if (loaded.get() && BooleanUtils.isFalse(dirty.get())) {
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
    public void quash() {
        codeArea.undo();
    }

    public void redo() {
        codeArea.redo();
    }

    public void save() {
        String editorText = codeArea.getText();
        fileOperationHandler.write(projectProperty.get().getProjectName(), editorText);
        dirtyProperty().set(false);
    }

    private void wrapSelection(String selectedText, IndexRange selection, String leftSymbol, String rightSymbol) {
        Platform.runLater(() -> {
            String text;
            int offset = leftSymbol.length();
            if (selectedText == null) {
                text = leftSymbol + rightSymbol;
            } else {
                text = leftSymbol + selectedText + rightSymbol;
            }
            codeArea.replaceText(selection, text);
            codeArea.requestFocus();
            codeArea.moveTo(selection.getEnd() + offset);
            codeArea.selectRange(selection.getStart() + offset, selection.getEnd() + offset);
        });
    }

    private void startWith(int paragraph, String text, Set<Character> trimLeft) {
        Platform.runLater(() -> {
            String row = text + trim(codeArea.getText(paragraph), trimLeft);
            codeArea.replaceText(paragraph, 0, paragraph, codeArea.getParagraphLength(paragraph), row);
            codeArea.requestFocus();
        });
    }


    private void appendNewLine(int paragraph, String text) {
        Platform.runLater(() -> {
            String row = codeArea.getText(paragraph);
            int offset = paragraph;
            if (row.length() > 0) {
                offset += 1;
            }
            if (offset >= codeArea.getParagraphs().size()) {
                codeArea.appendText("\n" + text);
            } else {
                codeArea.insertText(offset, 0, text);
            }
            codeArea.requestFocus();
        });
    }

    private String buildMarkdownTable(TableProperty property) {
        if (property.getColumn() <= 0 || property.getRow() <= 0) {
            return "";
        }
        StringBuilder tableBuilder = new StringBuilder();
        for (int i = 0; i < property.getRow() + 2; i++) {
            for (int j = 0; j < property.getColumn() + 1; j++) {
                if (j == property.getColumn()) {
                    tableBuilder.append("|");
                } else if (i == 1) {
                    tableBuilder.append("| :---: ");
                } else {
                    tableBuilder.append("|       ");
                }
            }
            tableBuilder.append("\n");
        }
        return tableBuilder.toString();
    }

    private String trim(String text, Set<Character> trimLeft) {
        int startIndex = 0;
        char[] charArray = text.toCharArray();
        for (; startIndex < charArray.length; startIndex++) {
            if (!trimLeft.contains(charArray[startIndex])) {
                break;
            }
        }
        return text.substring(startIndex);
    }

    @SuppressWarnings("SameParameterValue")
    private String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return "";
        } else {
            char[] buf = new char[repeat];
            Arrays.fill(buf, ch);
            return new String(buf);
        }
    }

    public void bold() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "**", "**");
    }

    public void italic() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "*", "*");
    }

    public void strikethrough() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "~~", "~~");
    }

    public void header() {
        int currentParagraph = codeArea.getCurrentParagraph();
        new PropertySheetDialog<>(new HeaderProperty())
                .showAndWait()
                .ifPresent(headerProperty -> startWith(currentParagraph, repeat('#', headerProperty.getLevel().getLevel()) + " ", Set.of('#', ' ')));
    }

    public void blockQuote() {
        startWith(codeArea.getCurrentParagraph(), "> ", Set.of('>', ' '));
    }

    public void unorderedList() {
        startWith(codeArea.getCurrentParagraph(), "- ", Set.of('-', '+', ' '));
    }

    public void orderedList() {
        int index = 1;
        if (codeArea.getCurrentParagraph() > 0) {
            String previousLine = codeArea.getText(codeArea.getCurrentParagraph() - 1);
            char[] previousLineCharArray = previousLine.toCharArray();
            int i = 0;
            for (; i < previousLineCharArray.length; i++) {
                if (previousLineCharArray[i] < '0' || previousLineCharArray[i] > '9') {
                    break;
                }
            }
            if (i > 0) {
                index = Integer.parseInt(previousLine.substring(0, i)) + 1;
            }
        }
        startWith(codeArea.getCurrentParagraph(), index + ". ", Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ' '));
    }

    public void table() {
        int currentParagraph = codeArea.getCurrentParagraph();
        new PropertySheetDialog<>(new TableProperty())
                .showAndWait()
                .ifPresent(property -> appendNewLine(currentParagraph, buildMarkdownTable(property)));
    }

    public void dividingLine() {
        appendNewLine(codeArea.getCurrentParagraph(), "---");
    }

    public void link() {
        IndexRange selection = codeArea.getSelection();
        new PropertySheetDialog<>(new LinkProperty())
                .showAndWait()
                .ifPresent(property -> wrapSelection(null, new IndexRange(selection.getEnd(), selection.getEnd()),
                        String.format("[%s](%s)", property.getTitle(), property.getUrl()), ""));
    }


    public void image() {
        IndexRange selection = codeArea.getSelection();
        new PropertySheetDialog<>(new ImageProperty())
                .showAndWait()
                .ifPresent(property -> wrapSelection(null, new IndexRange(selection.getEnd(), selection.getEnd()),
                        String.format("![%s](%s \"%s\")", property.getName(), property.getUrl(), property.getTitle()), ""));
    }

    public void codeInline() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "`", "`");
    }

    public void codeBlock() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "\n```", "\n```");
    }

    public void search() {

    }

    public void onlyEditor() {
        ObservableList<Node> items = projectEditorWorkspace.getItems();
        items.clear();
        items.add(codeAreaWorkspace);
        onlyEditorButton.setDisable(true);
        onlyPreviewButton.setDisable(false);
        editorAndPreviewButton.setDisable(false);
    }

    public void onlyPreview() {
        ObservableList<Node> items = projectEditorWorkspace.getItems();
        items.clear();
        items.add(previewArea);
        onlyEditorButton.setDisable(false);
        onlyPreviewButton.setDisable(true);
        editorAndPreviewButton.setDisable(false);
    }

    public void editorAndPreview() {
        ObservableList<Node> items = projectEditorWorkspace.getItems();
        items.clear();
        items.addAll(codeAreaWorkspace, previewArea);
        onlyEditorButton.setDisable(false);
        onlyPreviewButton.setDisable(false);
        editorAndPreviewButton.setDisable(true);
    }
}
