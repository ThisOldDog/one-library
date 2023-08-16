package pers.dog.api.callback;

import java.util.List;

import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import pers.dog.api.controller.setting.SettingController;
import pers.dog.api.controller.setting.SettingOptionController;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/16 11:04
 */
public class SettingGroupTreeCallback implements Callback<TreeView<SettingGroup>, TreeCell<SettingGroup>> {
    private final SettingController controller;

    private List<Object> controllerList;

    public SettingGroupTreeCallback(SettingController controller) {
        this.controller = controller;
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
                setOnMouseClicked(event -> {
                    if (settingGroup.getCode() != null) {
                        Parent setting = FXMLUtils.loadFXML(settingGroup.getSceneName());
                        if (previousController != null && previousController instanceof SettingOptionController) {

                        }
                    }
                });
            }
        };
    }
}
