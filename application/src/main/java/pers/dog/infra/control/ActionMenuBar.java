package pers.dog.infra.control;

import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

/**
 * @author 废柴 2022/6/4 23:39
 */
@DefaultProperty("actionMenuGroups")
public class ActionMenuBar extends MenuBar {
    private final ObservableList<Action> actionMenuGroups = FXCollections.observableArrayList();

    public ActionMenuBar() {
        actionMenuGroups.addListener((ListChangeListener<? super Action>) action -> ActionUtils.updateMenuBar(this, action.getList()));
    }

    public ObservableList<Action> getActionMenuGroups() {
        return actionMenuGroups;
    }

}
