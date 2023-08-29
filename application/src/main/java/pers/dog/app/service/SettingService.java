package pers.dog.app.service;

import java.util.Map;
import java.util.Objects;

import javafx.scene.control.TreeItem;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/21 15:00
 */
public interface SettingService {

    TreeItem<SettingGroup> buildSettingGroupTree();

    void saveSetting(Map<String, Map<String, Object>> optionMap);

    Object getOption(String settingCode, String optionCode);
}
