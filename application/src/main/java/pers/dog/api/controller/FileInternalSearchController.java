package pers.dog.api.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.controlsfx.control.action.Action;

public class FileInternalSearchController implements Initializable {
    @FXML
    public TextField searchTextField;
    @FXML
    public TextField currentIndex;
    @FXML
    public Label sumText;
    private final ObjectProperty<Action> searchAction;
    private final ObjectProperty<Action> previousOccurrenceAction;
    private final ObjectProperty<Action> nextOccurrenceAction;

    public FileInternalSearchController() {
        this.searchAction = new SimpleObjectProperty<>();
        this.previousOccurrenceAction = new SimpleObjectProperty<>();
        this.nextOccurrenceAction = new SimpleObjectProperty<>();
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
    }

    public void search(ActionEvent actionEvent) {
        searchAction.get().handle(actionEvent);
    }

    public void previousOccurrence(ActionEvent actionEvent) {
        previousOccurrenceAction.get().handle(actionEvent);
    }

    public void nextOccurrence(ActionEvent actionEvent) {
        nextOccurrenceAction.get().handle(actionEvent);
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
}
