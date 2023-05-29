package pers.dog.api.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.action.Action;

public class FileInternalSearchController implements Initializable {
    /* *************************************************************
     *
     * Search
     *
     * *************************************************************/
    @FXML
    public TitledPane searchBox;
    @FXML
    public TextField searchTextField;
    @FXML
    public TextField currentIndex;
    @FXML
    public Label sumText;

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
    public TextArea replaceTextField;
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
        searchBox.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ((HBox)searchBox.getGraphic()).setPrefWidth(newValue.doubleValue());
            }
        });
    }

    /* *************************************************************
     *
     * Search
     *
     * *************************************************************/
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

    public void replace(ActionEvent event) {
        replaceAction.get().handle(event);
    }

    public void replaceAll(ActionEvent event) {
        replaceAllAction.get().handle(event);
    }

    public TextArea getReplaceTextField() {
        return replaceTextField;
    }

    public ObjectProperty<Action> replaceActionProperty() {
        return replaceAction;
    }

    public ObjectProperty<Action> replaceAllActionProperty() {
        return replaceAllAction;
    }
}
