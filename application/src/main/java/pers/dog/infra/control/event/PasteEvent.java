package pers.dog.infra.control.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.InputEvent;

public class PasteEvent extends InputEvent {
    public static final EventType<PasteEvent> PASTE_EVENT = new EventType<>(Event.ANY, "PASTE");

    public PasteEvent(Clipboard source, EventTarget target) {
        super(source, target, PASTE_EVENT);
    }

    public Clipboard getClipboard() {
        return (Clipboard) super.getSource();
    }
}
