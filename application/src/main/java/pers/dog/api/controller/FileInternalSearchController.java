package pers.dog.api.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.controlsfx.control.action.Action;

public class FileInternalSearchController {
    @FXML
    public TextArea searchTextArea;
    @FXML
    public TextField currentIndex;
    private final ObjectProperty<Action> searchAction;
    private final ObjectProperty<Action> previousOccurrenceAction;
    private final ObjectProperty<Action> nextOccurrenceAction;

    public FileInternalSearchController() {
        this.searchAction = new SimpleObjectProperty<>();
        this.previousOccurrenceAction = new SimpleObjectProperty<>();
        this.nextOccurrenceAction = new SimpleObjectProperty<>();
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

    public TextArea getSearchTextArea() {
        return searchTextArea;
    }

    public void setSearchTextArea(TextArea searchTextArea) {
        this.searchTextArea = searchTextArea;
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
