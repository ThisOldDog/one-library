package pers.dog.boot.infra.constant;

import java.math.BigDecimal;

/**
 * @author 废柴 2023/9/21 17:10
 */
public enum NumberType {
    INTEGER(Integer.class),
    DECIMAL(BigDecimal.class);
    private final Class<?> valueType;

    NumberType(Class<?> valueType) {
        this.valueType = valueType;
    }

    public Class<?> getValueType() {
        return valueType;
    }
}
