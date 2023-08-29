package pers.dog.api.controller.setting;

import java.util.Map;
import java.util.Set;

import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/16 15:14
 */
public interface SettingOptionController {
    boolean changed();

    Map<String, Object> getOption();
    Set<String> optionKeys();
    void loadOption(SettingGroup settingGroup);
    void setOption(Map<String, Object> option);

    void apply();
}
