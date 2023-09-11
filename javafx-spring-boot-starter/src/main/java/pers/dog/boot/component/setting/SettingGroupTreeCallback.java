package pers.dog.boot.component.setting;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.springframework.data.util.Pair;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

/**
 * @author 废柴 2023/8/16 11:04
 */
public class SettingGroupTreeCallback implements Callback<TreeView<SettingGroup>, TreeCell<SettingGroup>> {
    private final SettingController controller;
    private final SettingService settingService;

    private final Map<String, Pair<AbstractSettingOptionController<?>, Parent>> optionMap;

    public SettingGroupTreeCallback(SettingController controller, SettingService settingService) {
        this.controller = controller;
        this.settingService = settingService;
        this.optionMap = new HashMap<>();
    }

    @Override
    public TreeCell<SettingGroup> call(TreeView<SettingGroup> param) {
        return new TreeCell<>() {
            @Override
            protected void updateItem(SettingGroup settingGroup, boolean empty) {
                super.updateItem(settingGroup, empty);
                if (empty || settingGroup == null) {
                    handleEmpty();
                } else {
                    handleSettingGroup(settingGroup);
                }
            }

            private void handleEmpty() {
                setText(null);
                setGraphic(null);
            }

            private void handleSettingGroup(SettingGroup settingGroup) {
                setText(I18nMessageSource.getResource(settingGroup.getGroupName()));
                setOnMouseClicked(event -> openSetting(settingGroup));
            }
        };
    }

    public void openSetting(SettingGroup settingGroup) {
        if (settingGroup.getCode() != null) {
            Pair<AbstractSettingOptionController<?>, Parent> option = optionMap.get(settingGroup.getCode());
            if (option == null) {
                Parent setting = FXMLUtils.loadFXML(settingGroup.getSceneName());
                AbstractSettingOptionController<?> optionController = FXMLUtils.getController(setting);
                option = Pair.of(optionController, setting);
                optionController.initOption(settingService.getOption(settingGroup.getCode()));
                optionMap.put(settingGroup.getCode(), Pair.of(optionController, setting));
            }
            settingService.setLatestSettingOption(settingGroup.getCode());
            controller.getSettingWorkspace().setDetailNode(option.getSecond());
        }
    }

    public Map<String, Object> applyOption() {
        Map<String, Object> changedOption = new HashMap<>();
        optionMap.forEach((k, v) -> {
            AbstractSettingOptionController<?> settingOptionController = v.getFirst();
            if (settingOptionController.changed()) {
                Object option = settingOptionController.getOption();
                changedOption.put(k, option);
                settingOptionController.apply();
                settingService.publishSettingChangeEvent(settingOptionController.getSettingCode(), settingOptionController.getOption());
            }
        });
        return changedOption;
    }
}
