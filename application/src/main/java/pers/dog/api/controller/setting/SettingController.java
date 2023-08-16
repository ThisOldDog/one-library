package pers.dog.api.controller.setting;

import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.controlsfx.control.MasterDetailPane;
import org.springframework.util.CollectionUtils;
import pers.dog.api.callback.SettingGroupTreeCallback;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.config.OneLibraryProperties;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/15 21:49
 */
public class SettingController implements Initializable {
    private final OneLibraryProperties oneLibraryProperties;
    @FXML
    public MasterDetailPane settingWorkspace;
    @FXML
    public TreeView<SettingGroup> settingGroupTree;

    private ApplicationDirFileOperationHandler handler;
    private Map<String, Map<String, String>> settingLocalMap;

    public SettingController(OneLibraryProperties oneLibraryProperties) {
        this.oneLibraryProperties = oneLibraryProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/conf"));
        settingLocalMap = Optional.ofNullable(handler.read("setting.json", new TypeReference<Map<String, Map<String, String>>>() {
        })).orElseGet(HashMap::new);
        List<SettingGroup> settingGroupList = oneLibraryProperties.getSetting();
        TreeItem<SettingGroup> root = new TreeItem<>();
        buildSettingGroupTree(root, settingGroupList);
        settingGroupTree.setRoot(root);
        settingGroupTree.setCellFactory(new SettingGroupTreeCallback(this));
    }

    private void buildSettingGroupTree(TreeItem<SettingGroup> root, List<SettingGroup> settingGroupList) {
        if (CollectionUtils.isEmpty(settingGroupList)) {
            return;
        }
        for (SettingGroup settingGroup : settingGroupList) {
            if (settingGroup.getCode() != null) {
                settingGroup.setOptions(setOption(settingGroup.getOptions(), settingLocalMap.get(settingGroup.getCode())));
            }
            TreeItem<SettingGroup> node = new TreeItem<>(settingGroup);
            root.getChildren().add(node);
            buildSettingGroupTree(node, settingGroup.getChildren());
        }
    }

    private Map<String, String> setOption(Map<String, String> defaultOptions, Map<String, String> options) {
        if (defaultOptions == null) {
            defaultOptions = new HashMap<>();
        }
        if (!(defaultOptions instanceof HashMap<String, String>)) {
            defaultOptions = new HashMap<>(defaultOptions);
        }
        if (options != null) {
            defaultOptions.putAll(options);
        }
        return defaultOptions;
    }

    public MasterDetailPane getSettingWorkspace() {
        return settingWorkspace;
    }
}
