package pers.dog.api.controller.setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.controlsfx.control.PrefixSelectionComboBox;
import org.springframework.util.ObjectUtils;
import pers.dog.api.controller.dto.MarkdownStyle;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.domain.entity.SettingGroup;

/**
 * @author 废柴 2023/8/17 19:52
 */
public class SettingMarkdownController implements SettingOptionController, Initializable {
    public static final String SETTING_CODE = "markdown";
    public static final String OPTION_PREVIEW_STYLE = "preview-style";
    private final Map<String, String> optionMap = new HashMap<>();
    @FXML
    public PrefixSelectionComboBox<MarkdownStyle> previewStyleComboBox;
    private SettingGroup settingGroup;
    private boolean changed = false;
    private final ObservableList<MarkdownStyle> markdownStyles = FXCollections.observableArrayList();

    public SettingMarkdownController() {
        ApplicationDirFileOperationHandler handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/style/markdown"));
        Properties markdownStyleProperties = new Properties();
        try {
            try (BufferedReader reader = Files.newBufferedReader(handler.directory().resolve("styles.properties"), StandardCharsets.UTF_8)) {
                markdownStyleProperties.load(reader);
                markdownStyleProperties.forEach((code, name) -> markdownStyles.add(new MarkdownStyle().setCode(String.valueOf(code)).setName(I18nMessageSource.getResource(String.valueOf(name)))));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load .data/style/markdown/styles.properties", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Preview Style
        previewStyleComboBox.setItems(markdownStyles);
        previewStyleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            changed = true;
            optionMap.put(OPTION_PREVIEW_STYLE, newValue.getCode());
        });
    }


    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public Map<String, String> getOption() {
        return optionMap;
    }

    @Override
    public void loadOption(SettingGroup settingGroup) {
        this.settingGroup = settingGroup;
        loadOption(settingGroup.getOptions());
    }

    private void loadOption(Map<String, String> option) {
        optionMap.putAll(option);
        String previewStyle = option.get(OPTION_PREVIEW_STYLE);
        if (!ObjectUtils.isEmpty(previewStyle)) {
            for (MarkdownStyle item : previewStyleComboBox.getItems()) {
                if (Objects.equals(item.getCode(), previewStyle)) {
                    previewStyleComboBox.setValue(item);
                }
            }
        }
        settingGroup.setOptions(optionMap);
    }

    @Override
    public void setOption(Map<String, String> option) {
        changed = true;
        loadOption(option);
    }
}
