package pers.dog.boot.component.setting;

/**
 * @author 废柴 2023/9/11 20:38
 */
@FunctionalInterface
public interface SettingChangeListener {

    void settingChanged(Object option);
}
