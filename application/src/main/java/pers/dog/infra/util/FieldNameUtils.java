package pers.dog.infra.util;

import org.springframework.util.ObjectUtils;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/10 15:53
 */
public class FieldNameUtils {
    public static String toLowerKebabCase(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return value;
        }
        char[] charArray = value.toCharArray();
        StringBuilder lowerKebabCaseBuilder = new StringBuilder();
        boolean validChar = false;
        for (char item : charArray) {
            validChar |= (item != '-');
            if (!validChar) {
                continue;
            }
            if (Character.isUpperCase(item)) {
                if (!lowerKebabCaseBuilder.isEmpty() && lowerKebabCaseBuilder.charAt(lowerKebabCaseBuilder.length() - 1) != '-') {
                    lowerKebabCaseBuilder.append('-');
                }
                lowerKebabCaseBuilder.append(Character.toLowerCase(item));
            } else if (item == '_' || item == '-') {
                if (!lowerKebabCaseBuilder.isEmpty() && lowerKebabCaseBuilder.charAt(lowerKebabCaseBuilder.length() - 1) != '-') {
                    lowerKebabCaseBuilder.append('-');
                }
            } else {
                lowerKebabCaseBuilder.append(item);
            }
        }
        for (int i = lowerKebabCaseBuilder.length() - 1; i >= 0; i--) {
            char item = lowerKebabCaseBuilder.charAt(i);
            if (item != '-') {
                break;
            }
            lowerKebabCaseBuilder.deleteCharAt(i);
        }
        return lowerKebabCaseBuilder.toString();
    }
}
