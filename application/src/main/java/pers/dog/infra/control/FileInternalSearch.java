package pers.dog.infra.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.controlsfx.control.action.Action;

public class FileInternalSearch extends Control {
    private final ObjectProperty<String> searchText;
    private final ObjectProperty<Integer> currentIndex;


    private final ObjectProperty<Action> searchAction;
    private final ObjectProperty<Action> previousOccurrenceAction;
    private final ObjectProperty<Action> nextOccurrenceAction;
    private final ObjectProperty<Action> moveToAction;
    private final ObjectProperty<Boolean> requestForce;
    private final ObjectProperty<Integer> searchCandidateCount;

    public FileInternalSearch() {
        this.searchText = new SimpleObjectProperty<>();
        this.currentIndex = new SimpleObjectProperty<>();

        this.searchAction = new SimpleObjectProperty<>();
        this.previousOccurrenceAction = new SimpleObjectProperty<>();
        this.nextOccurrenceAction = new SimpleObjectProperty<>();
        this.moveToAction = new SimpleObjectProperty<>();
        this.requestForce = new SimpleObjectProperty<>(false);
        this.searchCandidateCount = new SimpleObjectProperty<>(0);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FileInternalSearchSkin(this);
    }

    public String getSearchText() {
        return searchText.get();
    }

    public ObjectProperty<String> searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public Integer getCurrentIndex() {
        return currentIndex.get();
    }

    public ObjectProperty<Integer> currentIndexProperty() {
        return currentIndex;
    }

    public void setCurrentIndex(Integer currentIndex) {
        this.currentIndex.set(currentIndex);
    }

    public Action getSearchAction() {
        return searchAction.get();
    }

    public ObjectProperty<Action> searchActionProperty() {
        return searchAction;
    }

    public void setSearchAction(Action searchAction) {
        this.searchAction.set(searchAction);
    }

    public Action getPreviousOccurrenceAction() {
        return previousOccurrenceAction.get();
    }

    public ObjectProperty<Action> previousOccurrenceActionProperty() {
        return previousOccurrenceAction;
    }

    public void setPreviousOccurrenceAction(Action previousOccurrenceAction) {
        this.previousOccurrenceAction.set(previousOccurrenceAction);
    }

    public Action getNextOccurrenceAction() {
        return nextOccurrenceAction.get();
    }

    public ObjectProperty<Action> nextOccurrenceActionProperty() {
        return nextOccurrenceAction;
    }

    public void setNextOccurrenceAction(Action nextOccurrenceAction) {
        this.nextOccurrenceAction.set(nextOccurrenceAction);
    }

    public void setMoveToAction(Action action) {
        this.moveToAction.set(action);
    }

    ObjectProperty<Boolean> requestForceProperty() {
        return requestForce;
    }

    public Integer getSearchCandidateCount() {
        return searchCandidateCount.get();
    }

    public ObjectProperty<Integer> searchCandidateCountProperty() {
        return searchCandidateCount;
    }

    @Override
    public void requestFocus() {
        requestForce.set(true);
    }

}
