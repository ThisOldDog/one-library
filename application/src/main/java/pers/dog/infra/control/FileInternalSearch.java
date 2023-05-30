package pers.dog.infra.control;

import java.util.Collection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.controlsfx.control.action.Action;
import org.fxmisc.richtext.model.StyledDocument;

public class FileInternalSearch extends Control {
    private final ObjectProperty<String> searchText;
    private final ObjectProperty<String> replaceText;
    private final ObjectProperty<Integer> currentIndex;


    private final ObjectProperty<Action> searchAction;
    private final ObjectProperty<Action> previousOccurrenceAction;
    private final ObjectProperty<Action> nextOccurrenceAction;
    private final ObjectProperty<Action> moveToAction;
    private final ObjectProperty<Action> closeAction;
    private final ObjectProperty<Boolean> requestForce;
    private final ObjectProperty<Integer> searchCandidateCount;

    private final ObjectProperty<Boolean> showReplace;

    private final ObjectProperty<Action> replaceAction;
    private final ObjectProperty<Action> replaceAllAction;

    public FileInternalSearch() {
        this.searchText = new SimpleObjectProperty<>();
        this.replaceText = new SimpleObjectProperty<>();
        this.currentIndex = new SimpleObjectProperty<>();

        this.searchAction = new SimpleObjectProperty<>();
        this.previousOccurrenceAction = new SimpleObjectProperty<>();
        this.nextOccurrenceAction = new SimpleObjectProperty<>();
        this.moveToAction = new SimpleObjectProperty<>();
        this.closeAction = new SimpleObjectProperty<>();
        this.requestForce = new SimpleObjectProperty<>(false);
        this.searchCandidateCount = new SimpleObjectProperty<>(0);
        this.showReplace = new SimpleObjectProperty<>(false);
        this.replaceAction = new SimpleObjectProperty<>();
        this.replaceAllAction = new SimpleObjectProperty<>();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FileInternalSearchSkin(this);
    }

    public String getSearchText() {
        return searchText.get();
    }

    public String getReplaceText() {
        return replaceText.get();
    }

    public ObjectProperty<String> searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public ObjectProperty<String> replaceTextProperty() {
        return replaceText;
    }

    public void setReplaceText(String replaceText) {
        this.replaceText.set(replaceText);
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

    public Action getMoveToAction() {
        return moveToAction.get();
    }

    public ObjectProperty<Action> moveToActionProperty() {
        return moveToAction;
    }

    public void setMoveToAction(Action action) {
        this.moveToAction.set(action);
    }

    public Action getCloseAction() {
        return closeAction.get();
    }

    public ObjectProperty<Action> closeActionProperty() {
        return closeAction;
    }

    public void setCloseAction(Action closeAction) {
        this.closeAction.set(closeAction);
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

    public void showReplace(boolean value) {
        showReplace.set(value);
    }

    public ObjectProperty<Boolean> showReplaceProperty() {
        return showReplace;
    }

    public Action getReplaceAction() {
        return replaceAction.get();
    }

    public ObjectProperty<Action> replaceActionProperty() {
        return replaceAction;
    }

    public void setReplaceAction(Action replaceAction) {
        this.replaceAction.set(replaceAction);
    }

    public Action getReplaceAllAction() {
        return replaceAllAction.get();
    }

    public ObjectProperty<Action> replaceAllActionProperty() {
        return replaceAllAction;
    }

    public void setReplaceAllAction(Action replaceAllAction) {
        this.replaceAllAction.set(replaceAllAction);
    }
}
