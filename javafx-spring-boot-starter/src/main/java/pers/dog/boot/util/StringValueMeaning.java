package pers.dog.boot.util;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author 废柴 2021/7/7 19:09
 */
public class StringValueMeaning extends ValueMeaning<String> {
    public StringValueMeaning() {
        super(new SimpleStringProperty());
    }


}
