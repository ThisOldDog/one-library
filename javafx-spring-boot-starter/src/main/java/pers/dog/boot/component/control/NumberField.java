package pers.dog.boot.component.control;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import org.springframework.util.ObjectUtils;
import pers.dog.boot.infra.constant.NumberType;
import pers.dog.boot.infra.util.ValueConverterUtils;

/**
 * @author 废柴 2023/9/21 11:17
 */
public class NumberField<T> extends TextField {
    private final StringProperty format = new SimpleStringProperty("\\d*");
    private final ObjectProperty<BigDecimal> max = new SimpleObjectProperty<>(BigDecimal.valueOf(Integer.MAX_VALUE));
    private final ObjectProperty<BigDecimal> min = new SimpleObjectProperty<>(BigDecimal.valueOf(0));
    private Pattern numberPattern = Pattern.compile(format.get());
    private ObjectProperty<T> value = new SimpleObjectProperty<>();
    private NumberType numberType = NumberType.INTEGER;

    public NumberField() {
        this("");
    }

    @SuppressWarnings("unchecked")
    public NumberField(String text) {
        super(text);
        format.addListener((observable, oldValue, newValue) -> numberPattern = Pattern.compile(newValue));
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (!numberPattern.matcher(newValue).matches()) {
                setText(oldValue);
            } else if (!ObjectUtils.isEmpty(newValue)) {
                BigDecimal tmpValue = "-".equals(newValue) ? BigDecimal.ZERO : new BigDecimal(newValue);
                if (max.getValue().compareTo(tmpValue) < 0 || min.getValue().compareTo(tmpValue) > 0) {
                    setText(oldValue);
                } else {
                    value.setValue((T) ValueConverterUtils.read(newValue, numberType.getValueType()));
                }
            }
        });
        value.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                setText(newValue == null ? "" : String.valueOf(newValue));
            }
        });
    }

    public String getFormat() {
        return format.get();
    }

    public StringProperty formatProperty() {
        return format;
    }

    public void setFormat(String format) {
        this.format.set(format);
    }

    public Pattern getNumberPattern() {
        return numberPattern;
    }

    public NumberField setNumberPattern(Pattern numberPattern) {
        this.numberPattern = numberPattern;
        return this;
    }

    public BigDecimal getMax() {
        return max.get();
    }

    public ObjectProperty<BigDecimal> maxProperty() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max.set(max);
    }

    public BigDecimal getMin() {
        return min.get();
    }

    public ObjectProperty<BigDecimal> minProperty() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min.set(min);
    }

    public T getValue() {
        return value.get();
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public void setValue(T value) {
        this.value.set(value);
    }
}
