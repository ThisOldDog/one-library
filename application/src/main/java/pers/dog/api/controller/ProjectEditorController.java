package pers.dog.api.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.emoji.Emoji;
import com.vladsch.flexmark.ext.footnotes.FootnoteBlock;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.toc.TocBlock;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
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
import com.vladsch.flexmark.util.html.MutableAttributes;
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
import javafx.util.Duration;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.action.Action;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.dialog.PropertySheetDialog;
import pers.dog.boot.infra.util.PlatformUtils;
import pers.dog.domain.entity.Project;
import pers.dog.domain.repository.ProjectRepository;
import pers.dog.infra.control.FileInternalSearch;
import pers.dog.infra.control.MarkdownCodeArea;
import pers.dog.infra.property.HeaderProperty;
import pers.dog.infra.property.ImageProperty;
import pers.dog.infra.property.LinkProperty;
import pers.dog.infra.property.TableProperty;

/**
 * @author 废柴 2023/3/23 23:06
 */
public class ProjectEditorController implements Initializable {
    private static class TyporaAttributeProviderFactory extends IndependentAttributeProviderFactory {

        @Override
        public @NonNull AttributeProvider apply(@NonNull LinkResolverContext linkResolverContext) {
            return new TyporaAttributeProvider();
        }
    }

    private static class TyporaAttributeProvider implements AttributeProvider {

        @Override
        public void setAttributes(@NonNull com.vladsch.flexmark.util.ast.Node node,
                                  @NonNull AttributablePart attributablePart,
                                  @NonNull MutableAttributes mutableAttributes) {
            NodeAttributeHandler.handle(node, mutableAttributes);
        }
    }

    private enum NodeAttributeHandler {
        /*
         * TODO:
         * - list (task)	                            ul.task-list
         * - fences (before codemirror is initialized)	pre.md-fences.mock-cm
         * - diagrams	                                pre[lang=’sequence’], pre[lang=’flow’], pre[lang=’mermaid’]
         * - def_link	                                .md-def-link
         * - def_footnote	                            .md-def-footnote
         * - math_block	                                [mdtype=”math_block”]
         * - CodeMirror
         * - plain, underline, escape, tag, del, inline_math, subscript, highlight, url, reflink, refimg
         */

        LINE(Paragraph.class.getName(), attributes -> attributes.replaceValue("class", "md-line")),
        TASK_LIST_ITEM(TaskListItem.class.getName(), attributes -> attributes.replaceValue("class", "task-list-item")),
        TOC(TocBlock.class.getName(), attributes -> attributes.replaceValue("class", "md-toc")),
        FENCES(FencedCodeBlock.class.getName(), attributes -> attributes.replaceValue("class", "md-fences")),
        YAML_FRONT_MATTER_BLOCK(YamlFrontMatterBlock.class.getName(), attributes -> attributes.replaceValue("class", "md-meta-block")),
        STRONG(StrongEmphasis.class.getName(), attributes -> attributes.replaceValue("md-inline", "strong")),
        EMPHASIS(Emphasis.class.getName(), attributes -> attributes.replaceValue("md-inline", "em")),
        CODE(Code.class.getName(), attributes -> attributes.replaceValue("md-inline", "code")),
        FOOTNOTE(FootnoteBlock.class.getName(), attributes -> attributes.replaceValue("md-inline", "sup")),
        EMOJI(Emoji.class.getName(), attributes -> attributes.replaceValue("md-inline", "span")),
        SUPERSCRIPT(Superscript.class.getName(), attributes -> attributes.replaceValue("md-inline", "sup")),
        AUTO_LINK(AutoLink.class.getName(), attributes -> attributes.replaceValue("md-inline", "a")),
        LINK(Link.class.getName(), attributes -> attributes.replaceValue("md-inline", "a")),
        IMAGE(Image.class.getName(), attributes -> attributes.replaceValue("md-inline", "img"));

        private final String nodeType;
        private final Consumer<MutableAttributes> attributeProcessor;

        NodeAttributeHandler(String nodeType, Consumer<MutableAttributes> attributeProcessor) {
            this.nodeType = nodeType;
            this.attributeProcessor = attributeProcessor;
        }

        public static void handle(com.vladsch.flexmark.util.ast.Node node, MutableAttributes mutableAttributes) {
            if (node == null) {
                return;
            }
            for (NodeAttributeHandler nodeAttributeHandler : values()) {
                if (nodeAttributeHandler.nodeType.equals(node.getClass().getName())) {
                    nodeAttributeHandler.attributeProcessor.accept(mutableAttributes);
                    break;
                }
            }
        }
    }

    private static class TyporaAttributeRenderExtension implements HtmlRenderer.HtmlRendererExtension {

        @Override
        public void rendererOptions(@NonNull MutableDataHolder mutableDataHolder) {

        }

        @Override
        public void extend(@NonNull HtmlRenderer.Builder builder, @NonNull String s) {
            builder.attributeProviderFactory(new TyporaAttributeProviderFactory());
        }

        public static TyporaAttributeRenderExtension create() {
            return new TyporaAttributeRenderExtension();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ProjectEditorController.class);
    private static final String STYLE_CLASS_BUTTON_SAVE_DIRTY = "button-save-dirty";
    private final ObjectProperty<Project> projectProperty = new SimpleObjectProperty<>();
    private final ProjectRepository projectRepository;
    private final ObjectProperty<Boolean> dirty = new SimpleObjectProperty<>(false);
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final DataHolder markdownParserOptions = PegdownOptionsAdapter.flexmarkOptions(PegdownExtensions.ALL, TocExtension.create(), TaskListExtension.create(), TyporaAttributeRenderExtension.create());
    private final Parser parser = Parser.builder(markdownParserOptions).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder(markdownParserOptions).build();

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
    private String html;

    public ProjectEditorController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.fileInternalSearch = new FileInternalSearch();
        this.fileInternalSearch.setSearchAction(new Action(actionEvent -> codeArea.search(this.fileInternalSearch.getSearchText())));
        this.fileInternalSearch.setNextOccurrenceAction(new Action(actionEvent -> codeArea.nextSearchCandidate()));
        this.fileInternalSearch.setPreviousOccurrenceAction(new Action(actionEvent -> codeArea.previousSearchCandidate()));
        this.fileInternalSearch.setMoveToAction(new Action(actionEvent ->
                this.fileInternalSearch.setCurrentIndex(codeArea.moveToSearchCandidate(this.fileInternalSearch.getCurrentIndex()))
        ));
        this.fileInternalSearch.setCloseAction(new Action(actionEvent -> closeSearch()));
        this.fileInternalSearch.setReplaceAction(new Action(actionEvent -> codeArea.replaceSearch(this.fileInternalSearch.getReplaceText())));
        this.fileInternalSearch.setReplaceAllAction(new Action(actionEvent -> codeArea.replaceSearchAll(this.fileInternalSearch.getReplaceText())));
        this.engine = previewArea.getEngine();
        this.engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                Document document = this.engine.getDocument();
                XPath xPath = XPathFactory.newInstance().newXPath();
                try {
                    NodeList imgList = (NodeList) xPath.evaluate("//*[local-name()='img']", document, XPathConstants.NODESET);
                    if (imgList != null) {
                        for (int i = 0; i < imgList.getLength(); i++) {
                            Element img = (Element) imgList.item(i);
                            String src = img.getAttribute("src");
                            if (src.startsWith(path) || src.startsWith("http")) {
                                continue;
                            }
                            img.setAttribute("src", path + src);
                        }
                    }
                } catch (XPathExpressionException e) {
                    logger.error("Unable travel document.", e);
                }
            }
        });
        this.codeArea.getSearchCandidateList().addListener((InvalidationListener) observable -> this.fileInternalSearch.searchCandidateCountProperty().set(codeArea.getSearchCandidateList().size()));
        this.codeArea.searchCurrentIndexProperty().addListener(observable -> this.fileInternalSearch.setCurrentIndex(codeArea.getSearchCurrentIndex() + 1));
        this.codeArea.setOnPaste(event -> {
            Clipboard clipboard = event.getClipboard();
            if (clipboard.hasImage()) {
                Platform.runLater(() -> {
                    String fileName = projectProperty.get().getSimpleProjectName()
                            + "-"
                            + Optional.ofNullable(clipboard.getImage().getUrl()).map(path -> path.substring(path.lastIndexOf("/") + 1)).orElseGet(() -> ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS")))
                            + ".png";
                    try (ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream()) {
                        ImageIO.write(SwingFXUtils.fromFXImage(clipboard.getImage(), null), "png", imageOutputStream);
                        fileOperationHandler.write(fileName, new ByteArrayInputStream(imageOutputStream.toByteArray()));
                    } catch (IOException e) {
                        throw new RuntimeException("Unable read image from clipboard", e);
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
        this.codeAreaWorkspace.estimatedScrollYProperty().addListener(observable -> {
            int firstLine = this.codeArea.firstVisibleParToAllParIndex();
            int lastLine = this.codeArea.lastVisibleParToAllParIndex();
            int totalLine = this.codeArea.getParagraphs().size();
            double target = firstLine == 0 ? 0 : firstLine * 1D / (totalLine - lastLine + firstLine);
            this.engine.executeScript(String.format("window.scrollTo(0, document.body.scrollHeight * %s);",
                    target));
        });
        try {
            this.htmlWrapper = Files.readString(Path.of(getClass().getClassLoader().getResource("static/markdown-template.html").toURI()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
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
                engine.loadContent(html = toHtml(newValue))
        );
    }

    private String toHtml(String markdownContent) {
        return String.format(htmlWrapper, renderer.render(parser.parse(markdownContent)));
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
        Platform.runLater(() -> {
            codeArea.requestFocus();
        });
    }
}
