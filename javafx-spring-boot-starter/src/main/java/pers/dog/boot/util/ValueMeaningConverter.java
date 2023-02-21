package pers.dog.boot.util;

import javafx.util.StringConverter;

/**
 * @author 废柴 2021/7/7 20:10
 */
public class ValueMeaningConverter extends StringConverter<ValueMeaning<?>> {
    @Override
    public String toString(ValueMeaning<?> object) {
        return object.getMeaning();
    }

    @Override
    public ValueMeaning<?> fromString(String string) {
        return null;
    }
}
