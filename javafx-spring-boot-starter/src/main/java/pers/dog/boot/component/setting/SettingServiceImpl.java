package pers.dog.boot.component.setting;

import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.component.file.WriteOption;
import pers.dog.boot.context.property.ApplicationProperties;
import pers.dog.boot.infra.util.ValueConverterUtils;

/**
 * @author 废柴 2023/8/21 15:00
 */
@Service
public class SettingServiceImpl implements SettingService {
    private static final String SETTING_FILE_NAME = "application-setting.json";

    private final ApplicationProperties applicationProperties;

    private final ApplicationDirFileOperationHandler handler;
    private final Map<String, Object> settingLocalMap;
    private String latestSettingOption;
    private Map<String, List<SettingChangeListener>> settingChangeListenerMap = new HashMap<>();

    public SettingServiceImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;

        FileOperationOption.ApplicationDirOption applicationDirOption = new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/conf");
        applicationDirOption.setWithType(true);
        handler = new ApplicationDirFileOperationHandler(applicationDirOption);
        settingLocalMap = Optional.ofNullable(handler.read(SETTING_FILE_NAME, new TypeReference<Map<String, Object>>() {
                }))
                .map(HashMap::new)
                .orElseGet(HashMap::new);
        setDefaultValue(applicationProperties.getSetting());
    }

    private void setDefaultValue(List<SettingGroup> setting) {
        if (setting == null) {
            return;
        }
        for (SettingGroup settingGroup : setting) {
            if (settingGroup.getCode() != null && settingGroup.getOptions() != null) {
                settingLocalMap.computeIfAbsent(settingGroup.getCode(), key -> createDefaultOption(settingGroup));
            }
            setDefaultValue(settingGroup.getChildren());
        }
    }

    private Object createDefaultOption(SettingGroup settingGroup) {
        if (CollectionUtils.isEmpty(settingGroup.getOptions())) {
            return null;
        }
        Object option;
        try {
            option = settingGroup.getSettingType().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable create instance of " + settingGroup.getSettingType().getName(), e);
        }
        Map<String, Field> fieldMap = SettingUtils.getOptionFieldMap(settingGroup.getSettingType());
        settingGroup.getOptions().forEach((optionCode, value) -> {
            Field field = fieldMap.get(optionCode);
            try {
                FieldUtils.writeField(field, option, ValueConverterUtils.read(value, field.getType()), true);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable set control " + field.getName() + " on controller " + this.getClass(), e);
            }
        });
        return option;
    }


    @Override
    public String getLatestSettingOption() {
        return latestSettingOption;
    }

    @Override
    public void setLatestSettingOption(String latestSettingOption) {
        this.latestSettingOption = latestSettingOption;
    }

    @Override
    public TreeItem<SettingGroup> buildSettingGroupTree() {
        List<SettingGroup> settingGroupList = applicationProperties.getSetting();
        TreeItem<SettingGroup> root = new TreeItem<>();
        buildSettingGroupTree(root, settingGroupList);
        return root;
    }

    private void buildSettingGroupTree(TreeItem<SettingGroup> root, List<SettingGroup> settingGroupList) {
        if (CollectionUtils.isEmpty(settingGroupList)) {
            return;
        }
        for (SettingGroup settingGroup : settingGroupList) {
            TreeItem<SettingGroup> node = new TreeItem<>(settingGroup);
            root.getChildren().add(node);
            buildSettingGroupTree(node, settingGroup.getChildren());
        }
    }

    public void saveSetting(Map<String, Object> optionMap) {
        settingLocalMap.putAll(optionMap);
        handler.write(WriteOption.CREATE_NEW, SETTING_FILE_NAME, settingLocalMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(String settingCode) {
        return (T) settingLocalMap.get(settingCode);
    }

    @Override
    public void publishSettingChangeEvent(String settingCode, Object option) {
        List<SettingChangeListener> settingChangeListeners = settingChangeListenerMap.get(settingCode);
        if (settingChangeListeners != null) {
            for (SettingChangeListener settingChangeListener : settingChangeListeners) {
                settingChangeListener.settingChanged(option);
            }
        }
    }

    @Override
    public void onSettingChange(String settingCode, SettingChangeListener listener) {
        settingChangeListenerMap.computeIfAbsent(settingCode, key -> new ArrayList<>()).add(listener);
    }
}
