package pers.dog.api.callback;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.springframework.data.util.Pair;
import pers.dog.api.controller.setting.SettingController;
import pers.dog.api.controller.setting.SettingOptionController;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.domain.entity.SettingGroup;
import pers.dog.infra.status.StageStatusStore;

/**
 * @author 废柴 2023/8/16 11:04
 */
public class SettingGroupTreeCallback implements Callback<TreeView<SettingGroup>, TreeCell<SettingGroup>> {
    private final SettingController controller;
    private final StageStatusStore stageStatusStore;

    private final Map<String, Pair<SettingOptionController, Parent>> optionMap;

    public SettingGroupTreeCallback(SettingController controller, StageStatusStore stageStatusStore) {
        this.controller = controller;
        this.stageStatusStore = stageStatusStore;
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
            Pair<SettingOptionController, Parent> option = optionMap.get(settingGroup.getCode());
            if (option == null) {
                Parent setting = FXMLUtils.loadFXML(settingGroup.getSceneName());
                SettingOptionController optionController = FXMLUtils.getController(setting);
                option = Pair.of(optionController, setting);
                optionController.loadOption(settingGroup);
                optionMap.put(settingGroup.getCode(), Pair.of(optionController, setting));
            }
            stageStatusStore.getStageStatus().setLatestSettingOption(settingGroup.getCode());
            controller.getSettingWorkspace().setDetailNode(option.getSecond());
        }
    }

    public Map<String, Map<String, Object>> changedOption() {
        Map<String, Map<String, Object>> changedOption = new HashMap<>();
        optionMap.forEach((k, v) -> {
            SettingOptionController settingOptionController = v.getFirst();
            if (settingOptionController.changed()) {
                Map<String, Object> optionMap = settingOptionController.getOption();
                Set<String> keySet = settingOptionController.optionKeys();
                Map<String, Object> validOptionMap = new HashMap<>();
                optionMap.forEach((option, value) -> {
                    if (keySet.contains(option)) {
                        validOptionMap.put(option, value);
                    }
                });
                changedOption.put(k, validOptionMap);
            }
        });
        return changedOption;
    }
}
