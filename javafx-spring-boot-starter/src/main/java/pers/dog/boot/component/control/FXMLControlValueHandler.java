package pers.dog.boot.component.control;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final Map<Class<?>, Map<String, Pair<Object, Field>>> FXML_CONTROL_CACHE = new HashMap<>();
    private static final Set<Object> CONSUMED_CONTROLLER = new HashSet<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FXMLControl.class)) {
                produce(bean, field, field.getAnnotation(FXMLControl.class));
            }
        }
        return bean;
    }

    private static void produce(Object bean, Field field, FXMLControl fxmlControl) {
        FXML_CONTROL_CACHE.computeIfAbsent(fxmlControl.controller(), key -> new HashMap<>())
                .put(ObjectUtils.isEmpty(fxmlControl.id()) ? field.getName() : fxmlControl.id(), new Pair<>(bean, field));
        consumer(bean, field);
    }

    public static void consumer(Object bean, Field target) {
        for (Object controller : CONSUMED_CONTROLLER) {
            Field[] fields = controller.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FXML.class) && field.getName().equals(target.getName())) {
                    try {
                        FieldUtils.writeField(target, bean, FieldUtils.readField(field, controller, true), true);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("[FXMLControl] Unable set control " + field.getName() + " to bean " + bean.getClass(), e);
                    }
                }
            }
        }
    }

    public static void consumer(Object controller) {
        if (controller == null) {
            return;
        }
        CONSUMED_CONTROLLER.add(controller);
        Map<String, Pair<Object, Field>> idBeanMap = FXML_CONTROL_CACHE.get(controller.getClass());
        if (CollectionUtils.isEmpty(idBeanMap)) {
            return;
        }
        Field[] fields = controller.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FXML.class)) {
                Pair<Object, Field> bean = idBeanMap.get(field.getName());
                if (bean == null) {
                    continue;
                }
                try {
                    FieldUtils.writeField(bean.getValue(), bean.getKey(), FieldUtils.readField(field, controller, true), true);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("[FXMLControl] Unable set control " + field.getName() + " to bean " + bean.getKey().getClass(), e);
                }
            }
        }
    }
}
