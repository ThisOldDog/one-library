package pers.dog.infra.listener;

import org.springframework.stereotype.Component;
import pers.dog.api.controller.setting.SettingSystemHttpProxyController;
import pers.dog.app.service.HttpProxyService;
import pers.dog.boot.component.setting.SettingLoadedListener;
import pers.dog.boot.component.setting.SettingService;

/**
 * @author 废柴 2023/9/21 15:33
 */
@Component
public class HttpProxySettingLoadedListener implements SettingLoadedListener {
    private final HttpProxyService httpProxyService;

    public HttpProxySettingLoadedListener(HttpProxyService httpProxyService) {
        this.httpProxyService = httpProxyService;
    }

    @Override
    public void onSettingLoaded(SettingService settingService) {
        httpProxyService.setProxy(settingService.getOption(SettingSystemHttpProxyController.SETTING_CODE));
    }
}
