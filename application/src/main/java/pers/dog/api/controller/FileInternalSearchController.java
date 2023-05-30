package pers.dog.api.controller;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class FileInternalSearchController implements Initializable {
    /* *************************************************************
     *
     * Search
     *
     * *************************************************************/
    public static final Glyph REPLACE_COLLAPSE = new Glyph("FontAwesome", FontAwesome.Glyph.CARET_RIGHT);
    public static final Glyph REPLACE_EXPAND = new Glyph("FontAwesome", FontAwesome.Glyph.CARET_DOWN);
    @FXML
    public VBox searchBox;
    @FXML
    public TextField searchTextField;
    @FXML
    public TextField currentIndex;
    @FXML
    public Label sumText;
    @FXML
    public Button replaceExpandButton;

    private final ObjectProperty<Action> searchAction;
    private final ObjectProperty<Action> previousOccurrenceAction;
    private final ObjectProperty<Action> nextOccurrenceAction;
    private final ObjectProperty<Action> moveToOccurrenceAction;
    private final ObjectProperty<Action> closeAction;
    private final ObjectProperty<Boolean> autoNextFlag = new SimpleObjectProperty<>(false);


    /* *************************************************************
     *
     * Replace
     *
     * *************************************************************/

    @FXML
    public HBox replaceBox;
    @FXML
    public TextField replaceTextField;
    @FXML
    public Button replaceButton;
    @FXML
    public Button replaceAllButton;
    private final ObjectProperty<Action> replaceAction;
    private final ObjectProperty<Action> replaceAllAction;

    public FileInternalSearchController() {
        this.searchAction = new SimpleObjectProperty<>();
        this.previousOccurrenceAction = new SimpleObjectProperty<>();
        this.nextOccurrenceAction = new SimpleObjectProperty<>();
        this.moveToOccurrenceAction = new SimpleObjectProperty<>();
        this.closeAction = new SimpleObjectProperty<>();
        this.replaceAction = new SimpleObjectProperty<>();
        this.replaceAllAction = new SimpleObjectProperty<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentIndex.textProperty().addListener(change ->
                Platform.runLater(() -> {
                    Text text = new Text(currentIndex.getText());
                    text.setWrappingWidth(0);
                    currentIndex.setPrefWidth(text.getBoundsInLocal().getWidth() + 16);
                    replaceButton.setDisable(ObjectUtils.isEmpty(text.getText()) || "0".equals(text.getText()));
                    replaceAllButton.setDisable(ObjectUtils.isEmpty(text.getText()) || "0".equals(text.getText()));
                })
        );
        searchTextField.setOnKeyReleased(event -> {
            if (KeyCode.ENTER.equals(event.getCode())) {
                if (BooleanUtils.isTrue(autoNextFlag.get())) {
                    nextOccurrence(new ActionEvent().copyFor(event.getSource(), event.getTarget()));
                } else {
                    search(new ActionEvent().copyFor(event.getSource(), event.getTarget()));
                }
            } else if (KeyCode.ESCAPE.equals(event.getCode())) {
                close(new ActionEvent().copyFor(event.getSource(), event.getTarget()));
            }
        });
        searchTextField.textProperty().addListener(observable -> autoNextFlag.set(false));
        currentIndex.setOnKeyReleased(event -> {
            if (KeyCode.ENTER.equals(event.getCode())) {
                moveToOccurrence(new ActionEvent().copyFor(event.getSource(), event.getTarget()));
            }
        });
        searchBox.getChildren().addListener((InvalidationListener) observable ->
                Platform.runLater(() -> {
                    if (searchBox.getChildren().size() == 1) {
                        replaceExpandButton.setGraphic(REPLACE_COLLAPSE);
                    } else {
                        replaceExpandButton.setGraphic(REPLACE_EXPAND);
                    }
                })
        );
        
    }

    /* *************************************************************
     *
     * Search
     *
     * *************************************************************/
    public void showSearch() {
        if (searchBox.getChildren().size() > 1) {
            searchBox.getChildren().remove(1, searchBox.getChildren().size());
        }
    }

    public void search(ActionEvent actionEvent) {
        searchAction.get().handle(actionEvent);
        autoNextFlag.set(true);
    }

    public void previousOccurrence(ActionEvent actionEvent) {
        previousOccurrenceAction.get().handle(actionEvent);
    }

    public void nextOccurrence(ActionEvent actionEvent) {
        nextOccurrenceAction.get().handle(actionEvent);
    }

    public void moveToOccurrence(ActionEvent actionEvent) {
        moveToOccurrenceAction.get().handle(actionEvent);
    }

    public void close(ActionEvent actionEvent) {
        closeAction.get().handle(actionEvent);
        autoNextFlag.set(false);
    }

    public TextField getSearchTextField() {
        return searchTextField;
    }

    public TextField getCurrentIndex() {
        return currentIndex;
    }

    public void setSearchTextField(TextField searchTextField) {
        this.searchTextField = searchTextField;
    }

    public ObjectProperty<Action> searchActionProperty() {
        return searchAction;
    }

    public ObjectProperty<Action> previousOccurrenceActionProperty() {
        return previousOccurrenceAction;
    }

    public ObjectProperty<Action> nextOccurrenceActionProperty() {
        return nextOccurrenceAction;
    }

    public ObjectProperty<Action> moveToOccurrenceActionProperty() {
        return moveToOccurrenceAction;
    }

    public ObjectProperty<Action> closeActionProperty() {
        return closeAction;
    }

    /* *************************************************************
     *
     * Replace
     *
     * *************************************************************/
    public void showReplace() {
        if (searchBox.getChildren().size() == 1) {
            searchBox.getChildren().add(replaceBox);
        }
    }

    public void switchShowReplace() {
        if (searchBox.getChildren().size() == 1) {
            showReplace();
        } else {
            showSearch();
        }
    }

    public void replace(ActionEvent event) {
        replaceAction.get().handle(event);
    }

    public void replaceAll(ActionEvent event) {
        replaceAllAction.get().handle(event);
    }

    public TextField getReplaceTextField() {
        return replaceTextField;
    }

    public ObjectProperty<Action> replaceActionProperty() {
        return replaceAction;
    }

    public ObjectProperty<Action> replaceAllActionProperty() {
        return replaceAllAction;
    }
}
