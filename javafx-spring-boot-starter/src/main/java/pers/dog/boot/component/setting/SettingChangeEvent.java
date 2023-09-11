package pers.dog.boot.component.setting;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/11 20:36
 */
public class SettingChangeEvent {
    private final String settingCode;
    private final Object option;

    public SettingChangeEvent(String settingCode, Object option) {
        this.settingCode = settingCode;
        this.option = option;
    }

    public String getSettingCode() {
        return settingCode;
    }

    public Object getOption() {
        return option;
    }
}
