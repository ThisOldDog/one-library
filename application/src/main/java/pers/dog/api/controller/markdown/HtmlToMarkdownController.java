package pers.dog.api.controller.markdown;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.controlsfx.control.PrefixSelectionComboBox;
import pers.dog.boot.infra.dto.ValueMeaning;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.infra.control.MarkdownCodeArea;

/**
 * @author 废柴 2023/8/21 19:53
 */
public class HtmlToMarkdownController implements Initializable {
    private static final ObservableList<ValueMeaning> INSERT_POSITION_ALL = FXCollections.observableArrayList(
            new ValueMeaning().setValue("CURSOR").setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.cursor")),
            new ValueMeaning().setValue("START").setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.start")),
            new ValueMeaning().setValue("END").setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.end"))
    );

    @FXML
    public TextField url;
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> insertPosition;
    @FXML
    public WebView contentPreview;
    @FXML
    public MarkdownCodeArea markdownPreview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        insertPosition.setItems(INSERT_POSITION_ALL);
        insertPosition.setValue(INSERT_POSITION_ALL.get(0));
    }

    public TextField getUrl() {
        return url;
    }

    public PrefixSelectionComboBox<ValueMeaning> getInsertPosition() {
        return insertPosition;
    }

    public WebView getContentPreview() {
        return contentPreview;
    }

    public MarkdownCodeArea getMarkdownPreview() {
        return markdownPreview;
    }

    public boolean preview() {
        contentPreview.getEngine().load(url.getText());
        return true;
    }

    public String getMarkdown() {
        return null;
    }
}
