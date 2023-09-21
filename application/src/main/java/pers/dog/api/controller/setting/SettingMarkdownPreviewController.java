package pers.dog.api.controller.setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import org.controlsfx.control.PrefixSelectionComboBox;
import pers.dog.api.dto.MarkdownPreview;
import pers.dog.app.service.ProjectEditorService;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.component.setting.AbstractSettingOptionController;
import pers.dog.boot.infra.dto.ValueMeaning;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.infra.constant.TranslateServiceType;

/**
 * @author 废柴 2023/8/17 19:52
 */
public class SettingMarkdownPreviewController extends AbstractSettingOptionController<MarkdownPreview> {
    public static final String SETTING_CODE = "markdown-preview";
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> previewStyle;
    private final ProjectEditorService projectEditorService;
    private final ObservableList<ValueMeaning> markdownStyles = FXCollections.observableArrayList();

    public SettingMarkdownPreviewController(ProjectEditorService projectEditorService) {
        this.projectEditorService = projectEditorService;
        ApplicationDirFileOperationHandler handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix("style/markdown"));
        Properties markdownStyleProperties = new Properties();
        try {
            try (BufferedReader reader = Files.newBufferedReader(handler.directory().resolve("styles.properties"), StandardCharsets.UTF_8)) {
                markdownStyleProperties.load(reader);
                markdownStyleProperties.forEach((code, name) -> markdownStyles.add(new ValueMeaning().setValue(String.valueOf(code)).setMeaning(I18nMessageSource.getResource(String.valueOf(name)))));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load style/markdown/styles.properties", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Preview Style
        previewStyle.setItems(markdownStyles);
        addOptionValueConverter(
                "preview-style",
                value -> ((ValueMeaning) value).getValue(),
                value -> {
                    for (ValueMeaning item : previewStyle.getItems()) {
                        if (Objects.equals(item.getValue(), value)) {
                            return item;
                        }
                    }
                    return null;
                });
        super.initialize(location, resources);
    }

    @Override
    public void apply() {
        super.apply();
        Platform.runLater(projectEditorService::reloadAllSetting);
    }
}
