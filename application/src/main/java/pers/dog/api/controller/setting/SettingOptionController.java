package pers.dog.api.controller.setting;

import java.util.Map;

import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/16 15:14
 */
public interface SettingOptionController {
    boolean changed();

    Map<String, String> getOption();

    void loadOption(SettingGroup settingGroup);
    void setOption(Map<String, String> option);
}
