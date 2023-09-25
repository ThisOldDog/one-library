package pers.dog.boot.component.control;

import java.util.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.controlsfx.control.action.Action;
import org.springframework.util.ObjectUtils;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2023/9/22 13:45
 */
public abstract class DialogAction<T> extends Action {
    private final Map<ButtonType, List<EventHandler<? super ActionEvent>>> buttonEventFilterMap = new HashMap<>();
    private final Map<ButtonType, String> buttonTextMap = new HashMap<>();
    private final List<ButtonType> buttonTypes = new ArrayList<>();
    private Dialog<Void> cache;
    private T controller;
    protected DialogAction(String text, Object... args) {
        super(I18nMessageSource.getResource(text, args));
        super.setEventHandler(this::doAction);
    }

    public void doAction(ActionEvent actionEvent) {
        Dialog<Void> dialog = getDialog();
        beforeShow(dialog, controller);
        dialog.showAndWait();
    }

    public void beforeShow(Dialog<Void> dialog, T controller) {

    }

    public void addButton(ButtonType buttonType, EventHandler<? super ActionEvent> eventFilter, String text, Object... args) {
        if (!buttonTypes.contains(buttonType)) {
            buttonTypes.add(buttonType);
        }
        if (!ObjectUtils.isEmpty(text)) {
            buttonTextMap.put(buttonType, I18nMessageSource.getResource(text, args));
        }
        if (eventFilter != null) {
            buttonEventFilterMap.computeIfAbsent(buttonType, key -> new ArrayList<>())
                    .add(eventFilter);
        }
    }

    public abstract String getView();

    public Dialog<Void> getDialog() {
        if (cache != null) {
            return cache;
        }
        Parent parent = FXMLUtils.loadFXML(getView());
        controller = FXMLUtils.getController(parent);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(getText());
        dialog.setResizable(resizeable());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.getButtonTypes().addAll(buttonTypes());
        for (ButtonType buttonType : dialogPane.getButtonTypes()) {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            buttonEventFilterMap.getOrDefault(buttonType, Collections.emptyList())
                    .forEach(eventHandler -> button.addEventFilter(ActionEvent.ACTION, eventHandler));
            button.setText(buttonTextMap.getOrDefault(buttonType, button.getText()));
        }

        dialogPane.autosize();
        cache = onDialogCreated(dialog);
        return dialog;
    }

    public Dialog<Void> onDialogCreated(Dialog<Void> dialog) {
        return dialog;
    }

    private List<ButtonType> buttonTypes() {
        return buttonTypes.isEmpty() ? Collections.singletonList(ButtonType.CANCEL) : buttonTypes;
    }

    public boolean resizeable() {
        return true;
    }

    public T getController() {
        return controller;
    }
}
