package pers.dog.component.translate;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.setting.SettingToolTranslateController;
import pers.dog.api.dto.ToolTranslate;
import pers.dog.boot.component.setting.SettingService;
import pers.dog.infra.constant.TranslateServiceType;

/**
 * @author 废柴 2023/9/12 16:49
 */
@Component
public class TranslateServiceFactory {
    private final SettingService settingService;
    private final ObjectProperty<TranslateService> translateService = new SimpleObjectProperty<>();
    private final List<ChangeListener<? super TranslateService>> serviceChangeListenerList = new ArrayList<>();

    public TranslateServiceFactory(SettingService settingService) {
        this.settingService = settingService;
        settingService.onSettingChange(SettingToolTranslateController.SETTING_CODE, option -> getService());
        translateService.addListener((observable, oldValue, newValue) -> {
            for (ChangeListener<? super TranslateService> changeListener : serviceChangeListenerList) {
                changeListener.changed(observable, oldValue, newValue);
            }
        });
    }

    public TranslateService getService() {
        TranslateService service = null;
        ToolTranslate option = settingService.getOption(SettingToolTranslateController.SETTING_CODE);
        if (option != null
                && TranslateServiceType.AZURE_AI_TRANSLATE.equals(option.getServiceType())
                && !ObjectUtils.isEmpty(option.getApiKey())
                && !ObjectUtils.isEmpty(option.getRegion())
                && !ObjectUtils.isEmpty(option.getTextTranslateEndpoint())) {
            service = new AzureTranslateService(option.getApiKey(), option.getRegion(), option.getTextTranslateEndpoint());
        }
        translateService.setValue(service);
        return translateService.getValue();
    }

    public void onServiceChange(ChangeListener<? super TranslateService> listener) {
        serviceChangeListenerList.add(listener);
    }
}