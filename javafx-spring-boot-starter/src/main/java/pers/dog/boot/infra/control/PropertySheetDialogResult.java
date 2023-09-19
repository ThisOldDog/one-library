package pers.dog.boot.infra.control;

import javafx.scene.control.ButtonType;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/19 17:25
 */
public class PropertySheetDialogResult<T> {
    private ButtonType type;
    private T result;

    public ButtonType getType() {
        return type;
    }

    public PropertySheetDialogResult<T> setType(ButtonType type) {
        this.type = type;
        return this;
    }

    public T getResult() {
        return result;
    }

    public PropertySheetDialogResult<T> setResult(T result) {
        this.result = result;
        return this;
    }
}
