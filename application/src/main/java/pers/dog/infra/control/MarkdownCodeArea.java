package pers.dog.infra.control;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.IndexRange;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.EditableStyledDocument;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.model.StyledDocument;

/**
 * @author 废柴 2023/3/30 20:12
 */
public class MarkdownCodeArea extends CodeArea {
    public interface TextInsertionListener {
        void codeInserted(int start, int end, String text);
    }

    public static class TreeStyleSpansBuilder {
        private int start;
        private int end;
        private List<String> styles = new ArrayList<>();
        private List<TreeStyleSpansBuilder> children = new LinkedList<>();

        @SafeVarargs
        public TreeStyleSpansBuilder(int start, int end, Collection<String>... styles) {
            this.start = start;
            this.end = end;
            if (styles != null) {
                for (Collection<String> style : styles) {
                    this.styles.addAll(style);
                }
            }
        }

        public void addChild(int start, int end, Collection<String> styles) {
            if (children.isEmpty()) {
                children.add(new TreeStyleSpansBuilder(Math.max(start, this.start), Math.min(end, this.end), styles));
                return;
            }
            for (int i = 0; i < children.size(); i++) {
                TreeStyleSpansBuilder child = children.get(i);
                if (child.getStart() > start) {
                    children.add(i, new TreeStyleSpansBuilder(start, Math.min(child.getStart(), end), styles));
                    if (end > child.getStart()) {
                        addChild(child.getStart(), end, styles);
                    }
                    return;
                } else if (child.getEnd() > start) {
                    int childEnd = child.getEnd();
                    if (child.getStart() < start) {
                        child.setEnd(start);
                        children.add(++i, new TreeStyleSpansBuilder(start, Math.min(end, childEnd), child.getStyles(), styles));
                    }
                    if (end < childEnd) {
                        children.add(i + 1, new TreeStyleSpansBuilder(end, childEnd, child.getStyles()));
                    } else if (end > childEnd) {
                        addChild(childEnd, end, styles);
                    }
                    return;
                }
            }
            if (children.get(children.size() - 1).getEnd() <= start) {
                children.add(new TreeStyleSpansBuilder(start, end, styles));
            }
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public List<String> getStyles() {
            return styles;
        }

        public List<TreeStyleSpansBuilder> getChildren() {
            return children;
        }

        public TreeStyleSpansBuilder setStart(int start) {
            this.start = start;
            return this;
        }

        public TreeStyleSpansBuilder setEnd(int end) {
            this.end = end;
            return this;
        }

        public TreeStyleSpansBuilder setStyles(List<String> styles) {
            this.styles = styles;
            return this;
        }

        public TreeStyleSpansBuilder setChildren(List<TreeStyleSpansBuilder> children) {
            this.children = children;
            return this;
        }

        public StyleSpans<Collection<String>> create() {
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            if (children.isEmpty()) {
                spansBuilder.add(styles, end - start);
                return spansBuilder.create();
            }
            int index = 0;
            for (TreeStyleSpansBuilder child : children) {
                if (index < child.getStart()) {
                    spansBuilder.add(styles, child.getStart() - index);
                }
                spansBuilder.add(child.getStyles(), child.getEnd() - child.getStart());
                index = child.getEnd();
            }
            if (index < end) {
                spansBuilder.add(styles, end - index);
            }
            return spansBuilder.create();
        }
    }

    private static final Map<String, String> BRACKET_PAIRS = Map.of(
            "<", ">",
            "[", "]",
            "(", ")");
    private static final List<String> BRACKET_MATCH_STYLE = Collections.singletonList("bracket-match");
    private static final Collection<String> RESULT_CANDIDATE_STYLE_CLASS = Collections.singletonList("file-internal-search-candidate");
    private final ObservableList<IndexRange> resultIndexRange = FXCollections.observableArrayList();
    private final List<TextInsertionListener> insertionListeners;
    private ExecutorService executor;

    /**
     * Creates an area with no text.
     */
    public MarkdownCodeArea() {
        super();
        this.insertionListeners = new ArrayList<>();
        init(this);
    }

    public MarkdownCodeArea(String text) {
        super(text);
        this.insertionListeners = new ArrayList<>();
        init(this);
    }

    public MarkdownCodeArea(EditableStyledDocument<Collection<String>, String, Collection<String>> document) {
        super(document);
        this.insertionListeners = new ArrayList<>();
        init(this);
    }

    private void init(MarkdownCodeArea codeArea) {
        // 行号
        setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        // 代码高亮
        executor = Executors.newSingleThreadExecutor();
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .retainLatestUntilLater(executor)
                .supplyTask(codeArea::computeHighlightingAsync)
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(codeArea::applyHighlighting);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = this.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        this.setStyleSpans(0, highlighting);
    }

    public void addTextInsertionListener(TextInsertionListener listener) {
        insertionListeners.add(listener);
    }

    public void removeTextInsertionListener(TextInsertionListener listener) {
        insertionListeners.remove(listener);
    }

    @Override
    public void replace(int start, int end, StyledDocument<Collection<String>, String, Collection<String>> replacement) {
        // notify all listeners
        for (TextInsertionListener listener : insertionListeners) {
            listener.codeInserted(start, end, replacement.getText());
        }

        super.replace(start, end, replacement);
    }


    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        // 默认样式
        TreeStyleSpansBuilder treeStyleSpansBuilder = new TreeStyleSpansBuilder(0, text.length());
        // 搜索高亮
        for (IndexRange indexRange : resultIndexRange) {
            treeStyleSpansBuilder.addChild(indexRange.getStart(), indexRange.getEnd(), RESULT_CANDIDATE_STYLE_CLASS);
        }
        return treeStyleSpansBuilder.create();
    }

    public void search(String searchText) {
        Platform.runLater(() -> {
            resultIndexRange.clear();
            char[] textArray = getText().toCharArray();
            char[] searchArray = searchText.toCharArray();
            int start = -1;
            int offset = 0;
            for (int i = 0; i < textArray.length; i++) {
                if (textArray[i] == searchArray[offset]) {
                    if (start == -1) {
                        start = i;
                    }
                    offset++;
                } else {
                    start = -1;
                    offset = 0;
                }
                if (offset == searchArray.length) {
                    resultIndexRange.add(new IndexRange(start, i + 1));
                    start = -1;
                    offset = 0;
                }
            }
        });
    }
}
