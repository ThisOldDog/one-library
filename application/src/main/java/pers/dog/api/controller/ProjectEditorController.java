package pers.dog.api.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.html.Attribute;
import com.vladsch.flexmark.util.html.MutableAttributes;
import com.vladsch.flexmark.util.misc.Extension;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SplitPane;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.controlsfx.control.action.Action;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import pers.dog.api.controller.setting.SettingMarkdownPreviewController;
import pers.dog.api.dto.MarkdownPreview;
import pers.dog.app.service.MarkdownExtension;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationException;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.component.setting.SettingService;
import pers.dog.boot.context.ApplicationContextHolder;
import pers.dog.boot.infra.control.PropertySheetDialog;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.PlatformUtils;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.infra.control.FileInternalSearch;
import pers.dog.infra.control.MarkdownCodeArea;
import pers.dog.infra.property.HeaderProperty;
import pers.dog.infra.property.ImageProperty;
import pers.dog.infra.property.LinkProperty;
import pers.dog.infra.property.TableProperty;
import pers.dog.infra.status.StageStatusStore;
import pers.dog.infra.util.FieldNameUtils;

/**
 * @author 废柴 2023/3/23 23:06
 */
public class ProjectEditorController implements Initializable {
    private static class ClassAttributeProviderFactory extends IndependentAttributeProviderFactory {
        private final ProjectEditorController controller;

        private ClassAttributeProviderFactory(ProjectEditorController controller) {
            this.controller = controller;
        }

        @Override
        public @NonNull AttributeProvider apply(@NonNull LinkResolverContext linkResolverContext) {
            return new ClassAttributeProvider(controller);
        }
    }

    private record ClassAttributeProvider(ProjectEditorController controller) implements AttributeProvider {
        private static final String CLASS_ATTRIBUTE = "class";

        @Override
        public void setAttributes(@NonNull com.vladsch.flexmark.util.ast.Node node, @NonNull AttributablePart attributablePart, @NonNull MutableAttributes mutableAttributes) {
            // Set class
            String nodeType = node.getNodeName();
            if (StringUtils.endsWithIgnoreCase(nodeType, "Block")) {
                nodeType = nodeType.substring(0, nodeType.length() - 5);
            }
            if (ObjectUtils.isEmpty(nodeType)) {
                return;
            }
            String styleClass = "md-" + FieldNameUtils.toLowerKebabCase(nodeType);
            String value = mutableAttributes.getValue(CLASS_ATTRIBUTE);
            if (ObjectUtils.isEmpty(value)) {
                mutableAttributes.replaceValue(CLASS_ATTRIBUTE, styleClass);
            } else {
                mutableAttributes.replaceValue(CLASS_ATTRIBUTE, value + " " + styleClass);
            }
            if (node instanceof FencedCodeBlock fencedCodeBlock) {
                mutableAttributes.replaceValue("language", fencedCodeBlock.getInfo().toStringOrNull());
            }
            // Set src to absolute path
            Attribute src = mutableAttributes.get("src");
            if (src != null) {
                String srcValue = src.getValue();
                if (srcValue != null && srcValue.startsWith("./")) {
                    mutableAttributes.replaceValue("src", controller.path + srcValue.substring(2));
                }
            }
        }
    }

    private record ClassAttributeRenderExtension(
            ProjectEditorController controller) implements HtmlRenderer.HtmlRendererExtension {

        @Override
        public void rendererOptions(@NonNull MutableDataHolder mutableDataHolder) {

        }

        @Override
        public void extend(@NonNull HtmlRenderer.Builder builder, @NonNull String s) {
            builder.attributeProviderFactory(new ClassAttributeProviderFactory(controller));
        }

        public static ClassAttributeRenderExtension create(ProjectEditorController controller) {
            return new ClassAttributeRenderExtension(controller);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ProjectEditorController.class);
    private static final String STYLE_CLASS_BUTTON_SAVE_DIRTY = "button-save-dirty";
    private static final String DEFAULT_STYLE_NAME = "default-light";
    private final ObjectProperty<Project> projectProperty = new SimpleObjectProperty<>();
    private final ProjectRepository projectRepository;
    private final StageStatusStore stageStatusStore;
    private final SettingService settingService;
    private final MarkdownExtension markdownExtension;
    private final ObjectProperty<Boolean> dirty = new SimpleObjectProperty<>(false);
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final Consumer<List<Class<? extends Extension>>> enabledExtensionChanged = enabledExtension -> {
        DataHolder markdownParserOptions = PegdownOptionsAdapter.flexmarkOptions(PegdownExtensions.ALL, getOptions(enabledExtension));
        this.parser = Parser.builder(markdownParserOptions).build();
        this.renderer = HtmlRenderer.builder(markdownParserOptions).build();
        if (this.codeArea != null) {
            refreshPreview(this.codeArea.getText());
        }
    };
    private Parser parser;
    private HtmlRenderer renderer;

    @FXML
    public SplitPane projectEditorWorkspace;
    @FXML
    public VBox searchWorkspace;
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
    private FileInternalSearch fileInternalSearch;
    private String path;

    private String htmlWrapper;
    private String htmlWrapperWithoutStyle;
    private String html;
    private String body;

    public ProjectEditorController(ProjectRepository projectRepository,
                                   StageStatusStore stageStatusStore,
                                   SettingService settingService,
                                   MarkdownExtension markdownExtension) {
        this.projectRepository = projectRepository;
        this.stageStatusStore = stageStatusStore;
        this.settingService = settingService;
        this.markdownExtension = markdownExtension;
        markdownExtension.onExtensionChanged(enabledExtensionChanged);
        enabledExtensionChanged.accept(markdownExtension.enabledExtension());
        loadSetting();
    }

    public void loadSetting() {
        //
        try {
            htmlWrapperWithoutStyle = StreamUtils.copyToString(ProjectEditorController.class.getClassLoader().getResource("static/markdown-template-without-style.html").openStream(), StandardCharsets.UTF_8);
            String template = StreamUtils.copyToString(ProjectEditorController.class.getClassLoader().getResource("static/markdown-template.html").openStream(), StandardCharsets.UTF_8);
            // 添加 CodeMirror
            StringBuilder codeMirrorHeaderBuilder = new StringBuilder();
            Files.walkFileTree(Path.of("lib"), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".js")) {
                        codeMirrorHeaderBuilder.append("    <script src=\"file:///").append(file.toAbsolutePath()).append("\"></script>\n");
                    } else if (file.toString().endsWith(".css")) {
                        codeMirrorHeaderBuilder.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"file:///").append(file.toAbsolutePath()).append("\">\n");
                    }
                    return super.visitFile(file, attrs);
                }
            });
            String style = Optional.ofNullable((MarkdownPreview) settingService.getOption(SettingMarkdownPreviewController.SETTING_CODE))
                    .map(MarkdownPreview::getPreviewStyle)
                    .orElse(DEFAULT_STYLE_NAME) + ".css";
            Path markdownStyleDir = Path.of("style/markdown");
            if (Files.exists(markdownStyleDir) && Files.isDirectory(markdownStyleDir)) {
                File[] styles = markdownStyleDir.toFile().listFiles();
                if (styles != null) {
                    for (File file : styles) {
                        if (file.isFile() && file.exists() && file.getName().equals(style)) {
                            style = "    <link rel=\"stylesheet\" type=\"text/css\" href=\"file:///" + file.getAbsolutePath() + "\">";
                        }
                    }
                }
            }
            htmlWrapper = template.replace("{{header}}", codeMirrorHeaderBuilder.toString())
                    .replace("{{style}}", style);
            if (this.codeArea != null) {
                refreshPreview(this.codeArea.getText());
            }
        } catch (Exception e) {
            logger.error("[ProjectEditor] Unable loading resource.", e);
        }
    }

    private Extension[] getOptions(List<Class<? extends Extension>> enabledExtension) {
        List<Extension> extensions = new ArrayList<>();
        extensions.add(ClassAttributeRenderExtension.create(this));
        for (Class<? extends Extension> extensionClass : enabledExtension) {
            try {
                extensions.add((Extension) MethodUtils.invokeStaticMethod(extensionClass, "create"));
            } catch (Exception e) {
                logger.error("[Markdown Extension] Unable create extension: " + extensionClass, e);
            }
        }
        return extensions.toArray(new Extension[]{});
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.fileInternalSearch = new FileInternalSearch();
        this.fileInternalSearch.setSearchAction(new Action(actionEvent -> codeArea.search(this.fileInternalSearch.getSearchText())));
        this.fileInternalSearch.setNextOccurrenceAction(new Action(actionEvent -> codeArea.nextSearchCandidate()));
        this.fileInternalSearch.setPreviousOccurrenceAction(new Action(actionEvent -> codeArea.previousSearchCandidate()));
        this.fileInternalSearch.setMoveToAction(new Action(actionEvent -> this.fileInternalSearch.setCurrentIndex(codeArea.moveToSearchCandidate(this.fileInternalSearch.getCurrentIndex()))));
        this.fileInternalSearch.setCloseAction(new Action(actionEvent -> closeSearch()));
        this.fileInternalSearch.setReplaceAction(new Action(actionEvent -> codeArea.replaceSearch(this.fileInternalSearch.getReplaceText())));
        this.fileInternalSearch.setReplaceAllAction(new Action(actionEvent -> codeArea.replaceSearchAll(this.fileInternalSearch.getReplaceText())));
        this.previewArea.setContextMenuEnabled(false);
        this.engine = previewArea.getEngine();
        this.engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                scrollEngine();
            }
        });
        this.codeArea.getSearchCandidateList().addListener((InvalidationListener) observable -> this.fileInternalSearch.searchCandidateCountProperty().set(codeArea.getSearchCandidateList().size()));
        this.codeArea.searchCurrentIndexProperty().addListener(observable -> this.fileInternalSearch.setCurrentIndex(codeArea.getSearchCurrentIndex() + 1));
        this.codeArea.setOnPaste(event -> {
            Clipboard clipboard = event.getClipboard();
            if (clipboard.hasImage()) {
                Platform.runLater(() -> {
                    String fileName = projectProperty.get().getSimpleProjectName() + "-" + Optional.ofNullable(clipboard.getImage().getUrl()).map(imgPath -> imgPath.substring(imgPath.lastIndexOf("/") + 1)).orElseGet(() -> ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS"))) + ".png";
                    try (ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream()) {
                        ImageIO.write(SwingFXUtils.fromFXImage(clipboard.getImage(), null), "png", imageOutputStream);
                        fileOperationHandler.write(fileName, new ByteArrayInputStream(imageOutputStream.toByteArray()));
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable read image from clipboard", e);
                    }
                    codeArea.replaceSelection(String.format("![%s](%s)", fileName, "./" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20")));
                });
            } else if (clipboard.hasString()) {
                String text = clipboard.getString();
                if (text != null) {
                    codeArea.replaceSelection(text);
                }
            }
        });
        this.codeAreaWorkspace.estimatedScrollYProperty().addListener(observable -> scrollEngine());
    }

    private void scrollEngine() {
        int firstLine = this.codeArea.firstVisibleParToAllParIndex();
        int lastLine = this.codeArea.lastVisibleParToAllParIndex();
        int totalLine = this.codeArea.getParagraphs().size();
        double target = firstLine == 0 ? 0 : firstLine * 1D / (totalLine - lastLine + firstLine);
        this.engine.executeScript(String.format("if (document && document.body) window.scrollTo(0, document.body.scrollHeight * %s);", target));
    }

    public void show(Project project) {
        setProjectProperty(project);
        setFileOperationHandler(project);
        setChangeListener();
        setText(project);
        codeArea.getUndoManager().forgetHistory();
        loaded.set(true);
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
        html = htmlWrapper.replace("{{body}}", body = renderer.render(parser.parse(markdownContent)));
        return html;
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
        Map<Long, Project> projectMap = projectRepository.findAll().stream().collect(Collectors.toMap(Project::getProjectId, Function.identity()));
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
        this.path = fileOperationHandler.directory().toAbsolutePath().toUri().toString();
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
        Platform.runLater(() -> {
            codeArea.undo();
            fileInternalSearch.searchActionProperty().get().handle(new ActionEvent());
        });
    }

    public void redo() {
        Platform.runLater(() -> {
            codeArea.redo();
            fileInternalSearch.searchActionProperty().get().handle(new ActionEvent());
        });
    }

    public void save() {
        Platform.runLater(() -> {
            String editorText = codeArea.getText();
            fileOperationHandler.write(projectProperty.get().getProjectName(), editorText);
            dirtyProperty().set(false);
        });
    }

    public void exportToHtml() {
        Platform.runLater(() -> {
            String export = html;
            exportToHtmlFile(export);
        });
    }

    public void exportToHtmlWithoutStyle() {
        Platform.runLater(() -> {
            String export = htmlWrapperWithoutStyle.replace("{{body}}", body);
            exportToHtmlFile(export);
        });
    }

    private void exportToHtmlFile(String export) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nMessageSource.getResource("%info.project.save.project"));
        fileChooser.setInitialFileName(getProject().getSimpleProjectName());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html"));
        if (!ObjectUtils.isEmpty(stageStatusStore.getStageStatus().getLatestExportDirectory())) {
            File file = new File(stageStatusStore.getStageStatus().getLatestExportDirectory());
            if (file.exists()) {
                fileChooser.setInitialDirectory(file);
            }
        }
        File file = fileChooser.showSaveDialog(ApplicationContextHolder.getStage().getOwner());
        if (file != null) {
            try {
                stageStatusStore.getStageStatus().setLatestExportDirectory(file.getParent());
                Files.writeString(file.toPath(), export, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new FileOperationException(I18nMessageSource.getResource("error.project.export"), e);
            }
        }
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
        new PropertySheetDialog<>(new HeaderProperty()).showAndWait().ifPresent(result -> startWith(currentParagraph, repeat('#', result.getResult().getLevel().getLevel()) + " ", Set.of('#', ' ')));
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
        new PropertySheetDialog<>(new TableProperty()).showAndWait().ifPresent(result -> appendNewLine(currentParagraph, buildMarkdownTable(result.getResult())));
    }

    public void dividingLine() {
        appendNewLine(codeArea.getCurrentParagraph(), "---");
    }

    public void link() {
        IndexRange selection = codeArea.getSelection();
        new PropertySheetDialog<>(new LinkProperty()).showAndWait().ifPresent(result -> wrapSelection(null, new IndexRange(selection.getEnd(), selection.getEnd()), String.format("[%s](%s)", result.getResult().getTitle(), result.getResult().getUrl()), ""));
    }


    public void image() {
        IndexRange selection = codeArea.getSelection();
        new PropertySheetDialog<>(new ImageProperty()).showAndWait().ifPresent(result -> wrapSelection(null, new IndexRange(selection.getEnd(), selection.getEnd()), String.format("![%s](%s \"%s\")", result.getResult().getName(), result.getResult().getUrl(), result.getResult().getTitle()), ""));
    }

    public void codeInline() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "`", "`");
    }

    public void codeBlock() {
        wrapSelection(codeArea.getSelectedText(), codeArea.getSelection(), "\n```", "\n```");
    }

    public void search() {
        Platform.runLater(() -> {
            ObservableList<Node> children = searchWorkspace.getChildren();
            if (children.isEmpty()) {
                children.add(fileInternalSearch);
            }
            fileInternalSearch.showReplace(false);
            fileInternalSearch.requestFocus();
        });
    }

    public void replace() {
        Platform.runLater(() -> {
            ObservableList<Node> children = searchWorkspace.getChildren();
            if (children.isEmpty()) {
                children.add(fileInternalSearch);
            }
            fileInternalSearch.showReplace(true);
            fileInternalSearch.requestFocus();
        });
    }

    private void closeSearch() {
        Platform.runLater(() -> {
            searchWorkspace.getChildren().clear();
            codeArea.closeSearch();
        });

    }

    public void onlyEditor() {
        Platform.runLater(() -> {
            ObservableList<Node> items = projectEditorWorkspace.getItems();
            items.clear();
            items.add(codeAreaWorkspace);
            onlyEditorButton.setDisable(true);
            onlyPreviewButton.setDisable(false);
            editorAndPreviewButton.setDisable(false);
        });
    }

    public void onlyPreview() {
        Platform.runLater(() -> {
            ObservableList<Node> items = projectEditorWorkspace.getItems();
            items.clear();
            items.add(previewArea);
            onlyEditorButton.setDisable(false);
            onlyPreviewButton.setDisable(true);
            editorAndPreviewButton.setDisable(false);
        });
    }

    public void editorAndPreview() {
        Platform.runLater(() -> {
            ObservableList<Node> items = projectEditorWorkspace.getItems();
            items.clear();
            items.addAll(codeAreaWorkspace, previewArea);
            onlyEditorButton.setDisable(false);
            onlyPreviewButton.setDisable(false);
            editorAndPreviewButton.setDisable(true);
        });
    }

    public void requestFocus() {
        Platform.runLater(() -> codeArea.requestFocus());
    }

    public void replaceSelection(String markdown) {
        if (ObjectUtils.isEmpty(markdown)) {
            return;
        }
        codeArea.replaceSelection(markdown);
    }

    public void close() {
        markdownExtension.removeOnExtensionChanged(enabledExtensionChanged);
    }
}
