package pers.dog.boot.infra.control;

import java.beans.*;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanProperty;
import org.springframework.util.ObjectUtils;
import pers.dog.boot.infra.i18n.I18nMessageSource;

public class PropertySheetDialog<T> extends Dialog<PropertySheetDialogResult<T>> {
    public static class I18nBeanProperty extends BeanProperty {

        private final String name;
        private final String description;
        public I18nBeanProperty(Object bean, PropertyDescriptor propertyDescriptor) {
            super(bean, propertyDescriptor);
            I18nProperty i18nProperty = propertyDescriptor.getReadMethod().getAnnotation(I18nProperty.class);
            if (i18nProperty == null) {
                name = null;
                description = null;
            } else {
                name = I18nMessageSource.getResource(i18nProperty.name());
                description = ObjectUtils.isEmpty(i18nProperty.description())
                        ? null
                        : I18nMessageSource.getResource(i18nProperty.description());
            }
        }

        @Override
        public String getName() {
            return ObjectUtils.isEmpty(name) ? super.getName() : name;
        }

        @Override
        public String getDescription() {
            return ObjectUtils.isEmpty(description) ? super.getName() : description;
        }

    }
    private T value;
    public PropertySheetDialog(T value) {
        this(value, 360D, 60D, 1200D, 1000D);
    }

    public PropertySheetDialog(T value, ButtonType... buttonTypes) {
        this(value, 360D, 60D, 1200D, 1000D, buttonTypes);
    }

    public PropertySheetDialog(T value, double minWidth, double minHeight) {
        this(value, minWidth, minHeight, 1200D, 1000D);
    }

    public PropertySheetDialog(T value, double minWidth, double minHeight, ButtonType... buttonTypes) {
        this(value, minWidth, minHeight, 1200D, 1000D, buttonTypes);
    }

    public PropertySheetDialog(T value, double minWidth, double minHeight, double maxWidth, double maxHeight, ButtonType... buttonTypes) {
        this.value = value;
        DialogPane dialogPane = getDialogPane();
        PropertySheet propertySheet = buildFormPane(value);
        dialogPane.setContent(propertySheet);

        Class<?> propertyType = value.getClass();
        I18nProperty propertyTypeI18n = propertyType.getAnnotation(I18nProperty.class);
        if (propertyTypeI18n != null) {
            setTitle(I18nMessageSource.getResource(propertyTypeI18n.name()));
            if (!ObjectUtils.isEmpty(propertyTypeI18n.description())) {
                setHeaderText(I18nMessageSource.getResource(propertyTypeI18n.description()));
            }
        }

        setResizable(true);
        propertySheet.setMinWidth(minWidth);
        propertySheet.setMinHeight(minHeight);
        propertySheet.setMaxWidth(maxWidth);
        propertySheet.setMaxHeight(maxHeight);
        propertySheet.requestFocus();
        dialogPane.requestLayout();
        dialogPane.autosize();

        if (buttonTypes != null && buttonTypes.length > 0) {
            dialogPane.getButtonTypes().addAll(buttonTypes);
        } else {
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        }
        setResultConverter(buttonType ->
                ButtonType.OK.equals(buttonType)
                        ? new PropertySheetDialogResult<T>().setType(buttonType).setResult(this.value)
                        : new PropertySheetDialogResult<T>().setType(buttonType).setResult(null)
        );

    }

    public PropertySheetDialog<T> headerText(String headerText) {
        setHeaderText(I18nMessageSource.getResource(headerText));
        return this;
    }

    private PropertySheet buildFormPane(T value) {
        PropertySheet propertySheet = new PropertySheet();
        propertySheet.modeSwitcherVisibleProperty().set(false);
        propertySheet.searchBoxVisibleProperty().set(false);
        propertySheet.getItems().setAll(getProperties(value));
        if (!propertySheet.getItems().isEmpty()) {
            // padding + gap + line height
            propertySheet.setPrefHeight(30D + ((propertySheet.getItems().size() - 1) * 5D) + propertySheet.getItems().size() * 24D);
        }
        return propertySheet;
    }

    private List<PropertySheet.Item> getProperties(T value) {
        List<PropertySheet.Item> itemList = new ArrayList<>();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(value.getClass(), Object.class);
            for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
                boolean ignore = p.getReadMethod().isAnnotationPresent(Transient.class);
                if (ignore) {
                    continue;
                }
                itemList.add(new I18nBeanProperty(value, p));
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return itemList;
    }
}
