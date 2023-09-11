package pers.dog.boot.component.setting;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/11 20:38
 */
@FunctionalInterface
public interface SettingChangeListener {

    void settingChanged(Object option);
}
