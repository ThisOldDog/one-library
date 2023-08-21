package pers.dog.boot.infra.dto;

/**
 * @author 废柴 2023/8/17 20:08
 */
public class ValueMeaning {
    private String meaning;
    private String value;

    public String getMeaning() {
        return meaning;
    }

    public ValueMeaning setMeaning(String meaning) {
        this.meaning = meaning;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ValueMeaning setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return meaning;
    }
}
