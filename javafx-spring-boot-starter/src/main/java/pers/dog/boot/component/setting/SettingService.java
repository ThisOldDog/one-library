package pers.dog.boot.component.setting;

import java.util.Map;

import javafx.scene.control.TreeItem;
import pers.dog.boot.component.setting.SettingGroup;

/**
 * @author 废柴 2023/8/21 15:00
 */
public interface SettingService {
    String getLatestSettingOption();
    void setLatestSettingOption(String latestSettingOption);

    TreeItem<SettingGroup> buildSettingGroupTree();

    void saveSetting(Map<String, Object> optionMap);

    <T> T getOption(String settingCode);

    void publishSettingChangeEvent(String settingCode, Object option);

    void onSettingChange(String settingCode, SettingChangeListener listener);
}
