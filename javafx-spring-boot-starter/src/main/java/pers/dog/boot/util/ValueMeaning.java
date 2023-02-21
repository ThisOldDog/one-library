package pers.dog.boot.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.WritableObjectValue;

/**
 * @author 废柴 2021/6/30 17:32
 */
public abstract class ValueMeaning<V> {
    private final WritableObjectValue<V> value;
    private final SimpleStringProperty meaning;


    public ValueMeaning(WritableObjectValue<V> value) {
        this.value = value;
        this.meaning = new SimpleStringProperty();
    }

    public V getValue() {
        return value.get();
    }

    public void setValue(V value) {
        this.value.set(value);
    }

    public String getMeaning() {
        return meaning.get();
    }

    public void setMeaning(String meaning) {
        this.meaning.set(meaning);
    }
}
