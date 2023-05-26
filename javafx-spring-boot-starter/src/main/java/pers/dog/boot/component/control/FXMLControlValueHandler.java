package pers.dog.boot.component.control;

import java.lang.reflect.Field;
import java.util.*;

import javafx.fxml.FXML;
import javafx.util.Pair;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/6 20:09
 */
@Component
public class FXMLControlValueHandler implements BeanPostProcessor {
    private static final Map<Class<?>, Map<String, List<Pair<Object, Field>>>> FXML_CONTROL_CACHE = new HashMap<>();
    private static final Set<Object> CONSUMED_CONTROLLER = new HashSet<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        while (beanClass != null && !Object.class.equals(beanClass)) {
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FXMLControl.class)) {
                    produce(bean, field, field.getAnnotation(FXMLControl.class));
                }
            }
            beanClass = beanClass.getSuperclass();
        }
        return bean;
    }

    private static void produce(Object bean, Field field, FXMLControl fxmlControl) {
        if (ControlProvider.class.equals(field.getType())) {
            try {
                Object o = FieldUtils.readField(field, bean, true);
                if (o == null) {
                    FieldUtils.writeField(field, bean, new ControlProvider<>(), true);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Unable to set control value: %s#%s", bean.getClass(), field.getType()), e);
            }
        }
        FXML_CONTROL_CACHE.computeIfAbsent(fxmlControl.controller(), key -> new HashMap<>())
                .computeIfAbsent(ObjectUtils.isEmpty(fxmlControl.id()) ? field.getName() : fxmlControl.id(), key -> new ArrayList<>())
                .add(new Pair<>(bean, field));
        consumer(bean, field);
    }

    public static void consumer(Object bean, Field target) {
        for (Object controller : CONSUMED_CONTROLLER) {
            Field[] fields = controller.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FXML.class) && field.getName().equals(target.getName())) {
                    consumer(bean, target, controller, field);
                }
            }
        }
    }

    public static void consumer(Object controller) {
        if (controller == null) {
            return;
        }
        CONSUMED_CONTROLLER.add(controller);
        Map<String, List<Pair<Object, Field>>> idBeanMap = FXML_CONTROL_CACHE.get(controller.getClass());
        if (CollectionUtils.isEmpty(idBeanMap)) {
            return;
        }
        Field[] fields = controller.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FXML.class)) {
                List<Pair<Object, Field>> beans = idBeanMap.get(field.getName());
                if (beans == null) {
                    continue;
                }
                for (Pair<Object, Field> bean : beans) {
                    consumer(bean.getKey(), bean.getValue(), controller, field);
                }
            }
        }
    }

    private static void consumer(Object bean, Field beanField, Object controller, Field controllerField) {
        try {
            Object control = FieldUtils.readField(controllerField, controller, true);
            if (ControlProvider.class.equals(beanField.getType())) {
                ControlProvider<?> controlProvider = (ControlProvider<?>) FieldUtils.readField(beanField, bean, true);
                if (controlProvider == null) {
                    controlProvider = new ControlProvider<>();
                }
                controlProvider.set(control);
            } else {
                FieldUtils.writeField(beanField, bean, control, true);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("[FXMLControl] Unable set control " + controllerField.getName() + " to bean " + bean.getClass(), e);
        }
    }
}
