package pers.dog.boot.infra.dialog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.Assert;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.ReflectUtils;

public class FormDialog<T> extends Dialog<T> {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FormProperty {
        boolean ignore() default false;
        String name();

        long max() default Long.MAX_VALUE;
        long min() default Long.MIN_VALUE;
    }

    static class FormPropertyHolder {
        private final String name;
        private final Field field;
        private final Node control;
        private final Supplier<Object> valueGetter;

        public FormPropertyHolder(String name, Field field , Node control, Supplier<Object> valueGetter) {
            this.name = name;
            this.field = field;
            this.control = control;
            this.valueGetter = valueGetter;
        }

        public String getName() {
            return name;
        }

        public Field getField() {
            return field;
        }

        public Node getControl() {
            return control;
        }

        public Supplier<Object> getValueGetter() {
            return valueGetter;
        }
    }

    private Class<?> resultType;

    public FormDialog() {
        setGraphic(buildFormPane());
    }

    private Node buildFormPane() {
        GridPane gridPane = new GridPane();
        getField();
        return gridPane;
    }

    private List<FormPropertyHolder> getField() {
        this.resultType = ReflectUtils.getClassGenericType(this.getClass(), 0);
        Assert.notNull(this.resultType, String.format("[FormDialog] The return value type for %s could not be found", this.getClass()));
        Field[] fields = FieldUtils.getAllFields(resultType);
        List<FormPropertyHolder> formPropertyHolderList = new ArrayList<>(fields.length);
        for (Field field : fields) {
            String name;
            FormProperty formProperty = field.getAnnotation(FormProperty.class);
            name = formProperty == null ? field.getName() : I18nMessageSource.getResource(formProperty.name());
            Class<?> type = field.getType();
            if (Long.TYPE.isAssignableFrom(type)) {
                new TextField()
            }
            formPropertyHolderList.add(new FormPropertyHolder(name, field, ))
        }
        return formPropertyHolderList;
    }
}
