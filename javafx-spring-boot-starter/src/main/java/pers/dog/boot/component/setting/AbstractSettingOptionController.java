package pers.dog.boot.component.setting;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import pers.dog.boot.component.control.NumberField;
import pers.dog.boot.infra.util.ReflectUtils;
import pers.dog.boot.infra.util.ValueConverterUtils;
import pers.dog.boot.infra.util.WordUtils;

/**
 * @author 废柴 2023/8/16 15:14
 */
public abstract class AbstractSettingOptionController<T> implements Initializable {
    private final String settingCode;
    private final Map<String, Field> optionFieldMap;
    private final Map<String, WritableValue<?>> optionMap = FXCollections.observableHashMap();
    private final Map<String, Pair<Function<Object, Object>, Function<Object, Object>>> optionValueConverter = new HashMap<>();
    private final T option;
    private BooleanProperty initialized = new SimpleBooleanProperty(false);
    private BooleanProperty changed = new SimpleBooleanProperty(false);

    @SuppressWarnings("unchecked")
    protected AbstractSettingOptionController() {
        Class<T> optionClass = (Class<T>) ReflectUtils.getClassExtendGenericType(this.getClass(), 0);
        Assert.notNull(optionClass, "Setting option controller no generic specified: " + this.getClass());
        if (optionClass.isAnnotationPresent(SettingEntity.class)) {
            settingCode = optionClass.getAnnotation(SettingEntity.class).value();
        } else {
            settingCode = buildSettingCode(optionClass);
        }
        optionFieldMap = SettingUtils.getOptionFieldMap(optionClass);
        try {
            option = optionClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable create instance of " + optionClass.getName(), e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (Field field : FieldUtils.getFieldsListWithAnnotation(this.getClass(), FXML.class)) {
            try {
                Object value = FieldUtils.readField(field, this, true);
                String optionCode;
                if (field.isAnnotationPresent(SettingControl.class)) {
                    optionCode = field.getAnnotation(SettingControl.class).value();
                } else {
                    optionCode = WordUtils.camelCaseToLowerKebabCase(field.getName());
                }
                if (!optionFieldMap.containsKey(optionCode)) {
                    continue;
                }
                optionMap.put(optionCode, setControlListener( optionCode, field, value));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable get control " + field.getName() + " on controller " + this.getClass(), e);
            }
        }
    }

    public void addOptionValueConverter(String optionCode, Function<Object, Object> toLocalValue, Function<Object, Object> toControlValue) {
        optionValueConverter.put(optionCode, Pair.of(toLocalValue, toControlValue));
    }

    protected WritableValue<?> setControlListener(String optionCode, Field field, Object control) {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            // 修改状态
            changed.setValue(initialized.getValue());
            // 实体类赋值
            try {
                Field propertyField = optionFieldMap.get(optionCode);
                FieldUtils.writeField(propertyField, option, toLocalValue(optionCode, newValue, propertyField), true);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable set control " + field.getName() + " on controller " + this.getClass(), e);
            }
        };
        if (control instanceof NumberField) {
            ObjectProperty<BigDecimal> property = ((NumberField) control).valueProperty();
            property.addListener(changeListener);
            return property;
        } else if (control instanceof TextInputControl) {
            StringProperty property = ((TextInputControl) control).textProperty();
            property.addListener(changeListener);
            return property;
        } else if (control instanceof ComboBoxBase) {
            ObjectProperty<?> property = ((ComboBoxBase<?>) control).valueProperty();
            property.addListener(changeListener);
            return property;
        } else if (control instanceof CheckBox) {
            BooleanProperty property = ((CheckBox) control).selectedProperty();
            property.addListener(changeListener);
            return property;
        } else if (control instanceof ToggleButton)  {
            BooleanProperty property = ((ToggleButton) control).selectedProperty();
            property.addListener(changeListener);
            return property;
        }
        throw new UnsupportedOperationException("Unsupported control type " + control.getClass() + " in setting.");
    }

    public Object toLocalValue(String optionCode, Object value, Field field) {
        Pair<Function<Object, Object>, Function<Object, Object>> valueConverter = optionValueConverter.get(optionCode);
        if (valueConverter != null && valueConverter.getLeft() != null) {
            return valueConverter.getLeft().apply(value);
        }
        return ValueConverterUtils.read(value, field.getType());
    }

    private String buildSettingCode(Class<?> optionClass) {
        String simpleName = optionClass.getSimpleName();
        if (simpleName.startsWith("Setting")) {
            simpleName = simpleName.substring(7);
        }
        if (simpleName.endsWith("Controller")) {
            simpleName = simpleName.substring(0, simpleName.length() - 10);
        }
        simpleName = WordUtils.delimiterToFirstLetterCapitalized(simpleName);
        return simpleName;
    }

    public String getSettingCode() {
        return settingCode;
    }

    public boolean changed() {
        return changed.get();
    }

    public void setChanged(boolean changed) {
        this.changed.setValue(changed);
    }

    public BooleanProperty changedProperty() {
        return changed;
    }

    public T getOption() {
        return option;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initOption(T option) {
        if (option == null) {
            return;
        }
        optionFieldMap.forEach((optionCode, field) -> {
            try {
                Object value = FieldUtils.readField(field, option, true);
                WritableValue observableValue = optionMap.get(optionCode);
                if (observableValue != null && value != null) {
                    observableValue.setValue(toControlValue(optionCode, value, field));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unable get option " + field.getName() + " on class " + this.getClass(), e);
            }
        });
        initialized.setValue(true);
    }

    public Object toControlValue(String optionCode, Object value, Field field) {
        Pair<Function<Object, Object>, Function<Object, Object>> valueConverter = optionValueConverter.get(optionCode);
        if (valueConverter != null && valueConverter.getRight() != null) {
            return valueConverter.getRight().apply(value);
        }
        return ValueConverterUtils.read(value, field.getType());
    }

    public void apply() {
        changed.setValue(false);
    }
}
