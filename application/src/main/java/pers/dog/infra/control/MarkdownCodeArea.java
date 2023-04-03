package pers.dog.infra.control;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.concurrent.Task;
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

    /**
     * Class representing a pair of matching bracket indices
     */
    static class BracketPair {

        private final int start;
        private final int end;

        public BracketPair(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "BracketPair{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }

    }

    static class BracketHighlighter {
        // constants
        private static final List<String> CLEAR_STYLE = Collections.emptyList();
        private static final List<String> MATCH_STYLE = Collections.singletonList("match");
        private static final String BRACKET_PAIRS = "<>()[]";
        // the code area
        private final MarkdownCodeArea codeArea;
        // the list of highlighted bracket pairs
        private final List<BracketPair> bracketPairs;

        /**
         * Parameterized constructor
         *
         * @param codeArea the code area
         */
        public BracketHighlighter(MarkdownCodeArea codeArea) {
            this.codeArea = codeArea;

            this.bracketPairs = new ArrayList<>();

            // listen for changes in text or caret position
            this.codeArea.addTextInsertionListener((start, end, text) -> clearBracket());
            this.codeArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> highlightBracket(newVal)));
        }

        /**
         * Highlight the matching bracket at current caret position
         */
        public void highlightBracket() {
            this.highlightBracket(codeArea.getCaretPosition());
        }

        /**
         * Highlight the matching bracket at new caret position
         *
         * @param newVal the new caret position
         */
        private void highlightBracket(int newVal) {
            // first clear existing bracket highlights
            this.clearBracket();

            // detect caret position both before and after bracket
            String prevChar = (newVal > 0 && newVal <= codeArea.getLength()) ? codeArea.getText(newVal - 1, newVal) : "";
            if (BRACKET_PAIRS.contains(prevChar)) newVal--;

            // get other half of matching bracket
            Integer other = getMatchingBracket(newVal);

            if (other != null) {
                // other half exists
                BracketPair pair = new BracketPair(newVal, other);

                // highlight pair
                styleBrackets(pair, MATCH_STYLE);

                // add bracket pair to list
                this.bracketPairs.add(pair);
            }
        }

        /**
         * Find the matching bracket location
         *
         * @param index to start searching from
         * @return null or position of matching bracket
         */
        private Integer getMatchingBracket(int index) {
            if (index < 0 || index >= codeArea.getLength()) return null;

            char initialBracket = codeArea.getText(index, index + 1).charAt(0);
            int bracketTypePosition = BRACKET_PAIRS.indexOf(initialBracket); // "(){}[]<>"
            if (bracketTypePosition < 0) return null;

            // even numbered bracketTypePositions are opening brackets, and odd positions are closing
            // if even (opening bracket) then step forwards, otherwise step backwards
            int stepDirection = (bracketTypePosition % 2 == 0) ? +1 : -1;

            // the matching bracket to look for, the opposite of initialBracket
            char match = BRACKET_PAIRS.charAt(bracketTypePosition + stepDirection);

            index += stepDirection;
            int bracketCount = 1;

            while (index > -1 && index < codeArea.getLength()) {
                char code = codeArea.getText(index, index + 1).charAt(0);
                if (code == initialBracket) bracketCount++;
                else if (code == match) bracketCount--;
                if (bracketCount == 0) return index;
                else index += stepDirection;
            }

            return null;
        }

        /**
         * Clear the existing highlighted bracket styles
         */
        public void clearBracket() {
            // get iterator of bracket pairs
            Iterator<BracketPair> iterator = this.bracketPairs.iterator();

            // loop through bracket pairs and clear all
            while (iterator.hasNext()) {
                // get next bracket pair
                BracketPair pair = iterator.next();

                // clear pair
                styleBrackets(pair, CLEAR_STYLE);

                // remove bracket pair from list
                iterator.remove();
            }
        }

        /**
         * Set a list of styles to a pair of brackets
         *
         * @param pair   pair of brackets
         * @param styles the style list to set
         */
        private void styleBrackets(BracketPair pair, List<String> styles) {
            styleBracket(pair.start, styles);
            styleBracket(pair.end, styles);
        }

        /**
         * Set a list of styles for a position
         *
         * @param pos    the position
         * @param styles the style list to set
         */
        private void styleBracket(int pos, List<String> styles) {
            if (pos < codeArea.getLength()) {
                String text = codeArea.getText(pos, pos + 1);
                if (BRACKET_PAIRS.contains(text)) {
                    codeArea.setStyle(pos, pos + 1, styles);
                }
            }
        }
    }

    private static final String HEADING_PATTERN = "^#{1,6}[ \\t]+[ \\S\\t]*$";
    private static final String BOLD_ITALIC_PATTERN = "(\\*\\*\\*.+\\*\\*\\*)|(___.+___)";
    private static final String BOLD_PATTERN = "(\\*\\*.+\\*\\*)|(__.+__)";
    private static final String ITALIC_PATTERN = "(\\*.+\\*)|(_.+_)";
    private static final String REFERENCE_PATTERN = "^>{1,2}[ \\t]+[ \\S\\t]*$";
    private static final String ORDERED_LIST_PATTERN = "\\s*\\d+\\.[ \\t]+[ \\S\\t]*$";
    private static final String UNORDERED_LIST_PATTERN = "\\s*((\\-)|(\\+)|(\\*))[ \\t]+[ \\S\\t]*$";
    private static final String CODE_PATTERN = "`[^\\n`]+`";
    private static final String FENCED_CODE_PATTERN = "^```\\w+\\n[\\s\\S]*\\n```\\n$";


    private static final Pattern PATTERN = Pattern.compile(
            "(?<HEADING>" + HEADING_PATTERN + ")"
                    + "|(?<BOLD0ITALIC>" + BOLD_ITALIC_PATTERN + ")"
                    + "|(?<BOLD>" + BOLD_PATTERN + ")"
                    + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                    + "|(?<REFERENCE>" + REFERENCE_PATTERN + ")"
                    + "|(?<ORDERED0LIST>" + ORDERED_LIST_PATTERN + ")"
                    + "|(?<UNORDERED0LIST>" + UNORDERED_LIST_PATTERN + ")"
                    + "|(?<CODE>" + CODE_PATTERN + ")"
                    + "|(?<FENCED0CODE>" + FENCED_CODE_PATTERN + ")"
    );

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
        // 括号高亮
        new BracketHighlighter(codeArea);
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
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
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
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("HEADING") != null ? "heading" :
                            matcher.group("BOLD0ITALIC") != null ? "bold-italic" :
                                    matcher.group("BOLD") != null ? "bold" :
                                            matcher.group("ITALIC") != null ? "italic" :
                                                    matcher.group("REFERENCE") != null ? "reference" :
                                                            matcher.group("ORDERED0LIST") != null ? "ordered-list" :
                                                                    matcher.group("UNORDERED0LIST") != null ? "unordered-list" :
                                                                            matcher.group("CODE") != null ? "code" :
                                                                                    matcher.group("FENCED0CODE") != null ? "fenced-code" :
                                                                                            null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
