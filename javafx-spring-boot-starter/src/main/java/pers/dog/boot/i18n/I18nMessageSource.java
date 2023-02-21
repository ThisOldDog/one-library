package pers.dog.boot.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * @author 废柴 2020/8/2 15:09
 */
public class I18nMessageSource {
    private static volatile MessageSource messageSource;

    public static void setMessageSource(MessageSource messageSource) {
        I18nMessageSource.messageSource = messageSource;
    }

    public static String getResource(String resourceCode, Object... args) {
        return getResource(resourceCode, LocaleResolver.getLocale(), args);
    }

    public static String getResource(String resourceCode, Locale locale, Object... args) {
        if (messageSource == null) {
            return resourceCode;
        }
        return messageSource.getMessage(resourceCode, args, locale);
    }
}
