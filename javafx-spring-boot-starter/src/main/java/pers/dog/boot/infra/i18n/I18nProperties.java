package pers.dog.boot.infra.i18n;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 废柴 2020/8/2 15:23
 */
@ConfigurationProperties("javafx.application.i18n")
public class I18nProperties {
    private boolean enable = true;
    private Set<String> location = new HashSet<>();

    public boolean isEnable() {
        return enable;
    }

    public I18nProperties setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public Set<String> getLocation() {
        return location;
    }

    public I18nProperties setLocation(Set<String> location) {
        this.location = location;
        return this;
    }
}
