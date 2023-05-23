package pers.dog.infra.control;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.IndexRange;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.EditableStyledDocument;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.model.StyledDocument;
import pers.dog.infra.grammar.Grammar;

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
        private static final List<String> MATCH_STYLE = Collections.singletonList("bracket-match");
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

    static class Token {
        int start;
        int end;
        String name;
    }
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
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        parserToken(text);


        // 搜索高亮
        for (IndexRange indexRange : resultIndexRange) {
            spansBuilder.add(RESULT_CANDIDATE_STYLE_CLASS, matcher.end() - matcher.start())
            setStyleClass(indexRange.getStart(), indexRange.getEnd(), );
        }

        resultIndexRange.addListener((ListChangeListener<? super IndexRange>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (IndexRange indexRange : change.getAddedSubList()) {
                        setStyleClass(indexRange.getStart(), indexRange.getEnd(), RESULT_CANDIDATE_STYLE_CLASS);
                    }
                }
                if (change.wasRemoved()) {
                    for (IndexRange indexRange : change.getRemoved()) {
                        clearStyle(indexRange.getStart(), indexRange.getEnd());
                    }
                }
            }
        });
        // 括号高亮
        new BracketHighlighter(codeArea);

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        spansBuilder.add()
        return spansBuilder.create();
    }


    private Token takeToken(char[] charArray, int i, String name, boolean containBreak, char breakChar, char... breakChars) {
        Token token = new Token();
        token.name = name;
        token.start = i;
        i++;
        while (i < charArray.length && charArray[i] != breakChar && notContain(breakChars, charArray[i])) {
            i++;
        }
        token.end = containBreak ? (Math.min(i + 1, charArray.length)) : i;
        return token;
    }

    private boolean notContain(char[] source, char target) {
        if (source == null) {
            return true;
        }
        for (char item : source) {
            if (item == target) {
                return false;
            }
        }
        return true;
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
