package pers.dog.boot.component.setting;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.controlsfx.control.MasterDetailPane;

/**
 * @author 废柴 2023/8/15 21:49
 */
public class SettingController implements Initializable {
    private final SettingService settingService;
    @FXML
    public MasterDetailPane settingWorkspace;
    @FXML
    public TreeView<SettingGroup> settingGroupTree;

    private SettingGroupTreeCallback settingGroupTreeCallback;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingGroupTree.setRoot(settingService.buildSettingGroupTree());
        settingGroupTreeCallback = new SettingGroupTreeCallback(this, settingService);
        settingGroupTree.setCellFactory(settingGroupTreeCallback);
        openOption(settingGroupTreeCallback, settingGroupTree.getRoot().getChildren(), settingService.getLatestSettingOption(), new AtomicBoolean(false));
    }

    private void openOption(SettingGroupTreeCallback settingGroupTreeCallback, List<TreeItem<SettingGroup>> nodeList, String latestSettingOption, AtomicBoolean breakFlag) {
        if (nodeList == null || breakFlag.get()) {
            return;
        }
        for (TreeItem<SettingGroup> node : nodeList) {
            if (node.getValue().getCode() != null && (Objects.equals(node.getValue().getCode(), latestSettingOption) || latestSettingOption == null)) {
                settingGroupTreeCallback.openSetting(node.getValue());
                expandParent(node.getParent());
                breakFlag.set(true);
                return;
            }
            openOption(settingGroupTreeCallback, node.getChildren(), latestSettingOption, breakFlag);
        }
    }

    private void expandParent(TreeItem<SettingGroup> parent) {
        if (parent != null) {
            parent.setExpanded(true);
            expandParent(parent.getParent());
        }
    }


    public MasterDetailPane getSettingWorkspace() {
        return settingWorkspace;
    }

    public void saveSetting() {
        Map<String, Object> optionMap = settingGroupTreeCallback.applyOption();
        settingService.saveSetting(optionMap);
    }
}
