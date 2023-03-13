package pers.dog.boot.infra.util;

import org.springframework.util.StringUtils;

/**
 * @author 废柴 2021/6/15 20:20
 */
public class WordUtils {
    /**
     * 分隔符转首字母大写转空格
     * hello-world -> Hello World
     *
     * @param value 原始内容
     * @return 首字母大写
     */
    public static String delimiterToFirstLetterCapitalized(String value) {
        if (StringUtils.hasText(value)) {
            char[] chars = value.toCharArray();
            StringBuilder sb = new StringBuilder();
            boolean firstLetter = true;
            for (char letter : chars) {
                if (firstLetter) {
                    sb.append(Character.toUpperCase(letter));
                    firstLetter = false;
                } else if (letter >= 'a' && letter <= 'z') {
                    sb.append(letter);
                } else if (letter >= 'A' && letter <= 'Z') {
                    sb.append(Character.toLowerCase(letter));
                } else {
                    sb.append(' ');
                    firstLetter = true;
                }
            }
            return sb.toString();
        }
        return value;
    }
}
