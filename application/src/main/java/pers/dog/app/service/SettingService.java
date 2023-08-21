package pers.dog.app.service;

import java.util.Map;

import javafx.scene.control.TreeItem;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/21 15:00
 */
public interface SettingService {

    TreeItem<SettingGroup> buildSettingGroupTree();

    void saveSetting(Map<String, Map<String, String>> optionMap);

    String getOption(String settingCode, String optionCode);
}
