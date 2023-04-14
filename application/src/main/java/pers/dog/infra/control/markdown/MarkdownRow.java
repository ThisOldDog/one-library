package pers.dog.infra.control.markdown;

import java.util.Collection;
import java.util.List;

import javafx.collections.ObservableList;
import org.fxmisc.richtext.model.Paragraph;
import org.w3c.dom.Node;

public abstract class MarkdownRow<T extends Node> {
    protected final int rowIndex;
    protected final ObservableList<Paragraph<Collection<String>, String, Collection<String>>> paragraphList;
    protected final Paragraph<Collection<String>, String, Collection<String>> paragraph;

    protected final List<Node> previewList;
    protected int previewIndex;
    protected T preview;

    public MarkdownRow(int rowIndex,
                       ObservableList<Paragraph<Collection<String>, String, Collection<String>>> paragraphList,
                       Paragraph<Collection<String>, String, Collection<String>> paragraph,
                       List<Node> previewList) {
        this.rowIndex = rowIndex;
        this.paragraphList = paragraphList;
        this.paragraph = paragraph;
        this.previewList = previewList;

    }

    public int getRowIndex() {
        return rowIndex;
    }

    public ObservableList<Paragraph<Collection<String>, String, Collection<String>>> getParagraphList() {
        return paragraphList;
    }

    public Paragraph<Collection<String>, String, Collection<String>> getParagraph() {
        return paragraph;
    }

    public int getPreviewIndex() {
        return previewIndex;
    }

    public List<Node> getPreviewList() {
        return previewList;
    }

    public T getPreview() {
        return preview;
    }
}
