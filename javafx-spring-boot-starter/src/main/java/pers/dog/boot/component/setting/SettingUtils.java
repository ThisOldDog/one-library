package pers.dog.boot.component.setting;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import pers.dog.boot.infra.util.WordUtils;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/11 15:31
 */
public class SettingUtils {

    public static Map<String, Field> getOptionFieldMap(Class<?> settingType) {
        Map<String, Field> optionFieldMap = new HashMap<>();
        FieldUtils.getAllFieldsList(settingType).forEach(field -> {
            String optionCode;
            if (field.isAnnotationPresent(SettingOption.class)) {
                optionCode = field.getAnnotation(SettingOption.class).value();
            } else {
                optionCode = buildSettingOptionCode(field);
            }
            optionFieldMap.put(optionCode, field);
        });
        return optionFieldMap;
    }

    private static String buildSettingOptionCode(Field field) {
        return WordUtils.camelCaseToLowerKebabCase(field.getName());
    }
}
