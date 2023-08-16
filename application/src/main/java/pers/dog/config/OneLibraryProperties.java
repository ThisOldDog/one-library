package pers.dog.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/16 11:24
 */
@ConfigurationProperties("one-library")
public class OneLibraryProperties {
    @NestedConfigurationProperty
    private List<SettingGroup> setting;

    public List<SettingGroup> getSetting() {
        return setting;
    }

    public OneLibraryProperties setSetting(List<SettingGroup> setting) {
        this.setting = setting;
        return this;
    }
}
