package pers.dog.infra.control;

import java.util.Collection;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveArrayList;
import org.reactfx.collection.LiveList;
import org.springframework.util.Assert;

/**
 * Markdown 预览
 *
 * @author 废柴
 */
public class MarkdownPreviewArea extends ScrollPane {
    /* Properties */
    /**
     * Markdown 内容
     */
    private LiveList<Paragraph<Collection<String>, String, Collection<String>>> rowContent = new LiveArrayList<>();

    public LiveList<Paragraph<Collection<String>, String, Collection<String>>> getRowContent() {
        return rowContent;
    }

    /**
     * 预览内容
     */
    private LiveList<Node> previewContent = new LiveArrayList<>();

    public void setRowContent(LiveList<Paragraph<Collection<String>, String, Collection<String>>> rowContent) {
        this.rowContent = rowContent;
    }
    /* Constructor */

    public MarkdownPreviewArea() {
        this(new VBox());
    }

    private MarkdownPreviewArea(Node content) {
        super(content);
        Assert.isTrue(content instanceof Pane, "[MarkdownPreviewArea] content mast be pane.");
        Bindings.bindContent(previewContent, ((Pane) content).getChildren());
        rowContent.addListener((ListChangeListener<Paragraph<Collection<String>, String, Collection<String>>>) change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        updateRow(i, change.getList(), change.getList().get(i));
                    }
                }
            }
        });
    }

    private void updateRow(int rowIndex,
                           ObservableList<? extends Paragraph<Collection<String>, String, Collection<String>>> paragraphList,
                           Paragraph<Collection<String>, String, Collection<String>> paragraph) {

    }
}
