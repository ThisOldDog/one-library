package pers.dog.api.controller.setting;

import java.net.URL;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import pers.dog.app.service.MarkdownExtension;
import pers.dog.api.dto.SettingGroup;

/**
 * @author 废柴 2023/8/17 19:52
 */
@SuppressWarnings("unchecked")
public class SettingMarkdownConfigController implements SettingOptionController, Initializable {
    public static final String SETTING_CODE = "markdown-config";
    public static final String OPTION_EXTENSION_ALL = "extension-all";
    public static final String OPTION_EXTENSION_ITEMS = "extension-items";
    public static final Set<String> OPTION_KEY_LIST = Set.of(OPTION_EXTENSION_ALL, OPTION_EXTENSION_ITEMS);
    private final Map<String, Object> optionMap = new HashMap<>();
    @FXML
    public CheckBox extensionAll;
    @FXML
    public TextField extensionSearch;
    @FXML
    public FlowPane extensionPane;
    @FXML
    public Button cancelAllButton;
    public final List<CheckBox> extensionCheckBoxList = FXCollections.observableArrayList();
    public final List<CheckBox> matchedExtensionCheckBoxList = FXCollections.observableArrayList();
    private SettingGroup settingGroup;
    private boolean changed = false;

    private final MarkdownExtension markdownExtension;

    public SettingMarkdownConfigController(MarkdownExtension markdownExtension) {
        this.markdownExtension = markdownExtension;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        extensionCheckBoxList.addAll(markdownExtension.listExtension()
                .stream()
                .map(extension -> {
                    CheckBox checkBox = new CheckBox(extension);
                    FlowPane.setMargin(checkBox, new Insets(8, 0, 0, 8));
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        changed = true;
                        Set<String> extensionItemValue = (Set<String>) optionMap.computeIfAbsent(OPTION_EXTENSION_ITEMS, key -> new HashSet<String>());
                        if (BooleanUtils.isTrue(newValue)) {
                            extensionItemValue.add(checkBox.getText());
                        } else {
                            extensionAll.setSelected(false);
                            extensionItemValue.remove(checkBox.getText());
                        }
                    });
                    return checkBox;
                }).toList());
        extensionPane.getChildren().addAll(extensionCheckBoxList);

        extensionAll.selectedProperty().addListener((observable, oldValue, newValue) -> {
            changed = true;
            optionMap.put(OPTION_EXTENSION_ALL, newValue);
            if (BooleanUtils.isTrue(newValue)) {
                for (CheckBox checkBox : extensionCheckBoxList) {
                    checkBox.setSelected(true);
                }
            }
        });

        cancelAllButton.setOnAction(event -> {
            extensionAll.setSelected(false);
            for (CheckBox checkBox : extensionCheckBoxList) {
                checkBox.setSelected(false);
            }
        });

        extensionSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            extensionPane.getChildren().clear();
            if (ObjectUtils.isEmpty(newValue)) {
                extensionPane.getChildren().addAll(extensionCheckBoxList);
            } else {
                matchedExtensionCheckBoxList.clear();
                for (CheckBox checkBox : extensionCheckBoxList) {
                    if (checkBox.getText().toLowerCase().contains(newValue.toLowerCase())) {
                        matchedExtensionCheckBoxList.add(checkBox);
                    }
                }
                extensionPane.getChildren().addAll(matchedExtensionCheckBoxList);
            }
        });
    }


    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public Map<String, Object> getOption() {
        return optionMap;
    }

    @Override
    public Set<String> optionKeys() {
        return OPTION_KEY_LIST;
    }

    @Override
    public void loadOption(SettingGroup settingGroup) {
        this.settingGroup = settingGroup;
        loadOption(settingGroup.getOptions());
    }

    private void loadOption(Map<String, Object> option) {
        optionMap.putAll(option);
        Boolean extensionAllValue = (Boolean) option.get(OPTION_EXTENSION_ALL);
        extensionAll.setSelected(BooleanUtils.isTrue(extensionAllValue));


        Set<String> extensionItemsValue = (Set<String>) option.get(OPTION_EXTENSION_ITEMS);
        if (extensionItemsValue != null) {
            for (CheckBox checkBox : extensionCheckBoxList) {
                checkBox.setSelected(extensionItemsValue.contains(checkBox.getText()));
            }
        }
        settingGroup.setOptions(optionMap);
    }

    @Override
    public void setOption(Map<String, Object> option) {
        changed = true;
        loadOption(option);
    }

    @Override
    public void apply() {
        for (CheckBox checkBox : extensionCheckBoxList) {
            if (checkBox.isSelected()) {
                markdownExtension.enableExtension(checkBox.getText());
            } else {
                markdownExtension.disableExtension(checkBox.getText());
            }
        }
    }
}
