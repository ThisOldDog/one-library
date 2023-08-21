package pers.dog.app.service.impl;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.scene.control.TreeItem;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pers.dog.app.service.SettingService;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.component.file.WriteOption;
import pers.dog.config.OneLibraryProperties;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/21 15:00
 */
@Service
public class SettingServiceImpl implements SettingService {
    private static final String SETTING_FILE_NAME = "setting.json";

    private final OneLibraryProperties oneLibraryProperties;

    private final ApplicationDirFileOperationHandler handler;
    private final Map<String, Map<String, String>> settingLocalMap;

    public SettingServiceImpl(OneLibraryProperties oneLibraryProperties) {
        this.oneLibraryProperties = oneLibraryProperties;

        handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/conf"));
        settingLocalMap = Optional.ofNullable(handler.read(SETTING_FILE_NAME, new TypeReference<Map<String, Map<String, String>>>() {
                }))
                .map(HashMap::new)
                .orElseGet(HashMap::new);
    }


    @Override
    public TreeItem<SettingGroup> buildSettingGroupTree() {
        List<SettingGroup> settingGroupList = oneLibraryProperties.getSetting();
        TreeItem<SettingGroup> root = new TreeItem<>();
        buildSettingGroupTree(root, settingGroupList);
        return root;
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

    public void saveSetting(Map<String, Map<String, String>> optionMap) {
        settingLocalMap.putAll(optionMap);
        handler.write(WriteOption.CREATE_NEW, SETTING_FILE_NAME, settingLocalMap);
    }

    @Override
    public String getOption(String settingCode, String optionCode) {
        return settingLocalMap.getOrDefault(settingCode, Collections.emptyMap()).get(optionCode);
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
}
