package pers.dog.api.controller.setting;

import java.util.Map;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/16 15:14
 */
public interface SettingOptionController {
    boolean changed();

    Map<String, String> getOptionMap();

    Map<String, String> set
}
