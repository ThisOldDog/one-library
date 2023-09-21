package pers.dog.api.controller.setting;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import pers.dog.api.dto.MarkdownConfig;
import pers.dog.app.service.MarkdownExtension;
import pers.dog.boot.component.setting.AbstractSettingOptionController;

/**
 * @author 废柴 2023/8/17 19:52
 */
@SuppressWarnings("unchecked")
public class SettingMarkdownConfigController extends AbstractSettingOptionController<MarkdownConfig> {
    public static final String SETTING_CODE = "markdown-config";
    @FXML
    public CheckBox extensionAll;
    @FXML
    public TextField extensionSearch;
    @FXML
    public FlowPane extensionItems;
    @FXML
    public Button cancelAllButton;
    public final ObservableList<CheckBox> extensionCheckBoxList = FXCollections.observableArrayList();
    public final ObservableList<CheckBox> matchedExtensionCheckBoxList = FXCollections.observableArrayList();
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
                        Set<String> extensionItemValue = getOption().getExtensionItems();
                        if (BooleanUtils.isTrue(newValue)) {
                            extensionItemValue.add(checkBox.getText());
                        } else {
                            extensionAll.setSelected(false);
                            extensionItemValue.remove(checkBox.getText());
                        }
                        setChanged(true);
                    });
                    return checkBox;
                }).toList());
        extensionItems.getChildren().addAll(extensionCheckBoxList);
        extensionAll.selectedProperty().addListener((observable, oldValue, newValue) -> {
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
            extensionItems.getChildren().clear();
            if (ObjectUtils.isEmpty(newValue)) {
                extensionItems.getChildren().addAll(extensionCheckBoxList);
            } else {
                matchedExtensionCheckBoxList.clear();
                for (CheckBox checkBox : extensionCheckBoxList) {
                    if (checkBox.getText().toLowerCase().contains(newValue.toLowerCase())) {
                        matchedExtensionCheckBoxList.add(checkBox);
                    }
                }
                extensionItems.getChildren().addAll(matchedExtensionCheckBoxList);
            }
        });
        addOptionValueConverter(
                "extension-items",
                value -> extensionCheckBoxList.stream()
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .collect(Collectors.toSet()),
                value -> {
                    extensionCheckBoxList.forEach(checkBox -> checkBox.setSelected(((Set<String>) value).contains(checkBox.getText())));
                    return value;
                });
        super.initialize(location, resources);
    }

    @Override
    protected WritableValue<?> setControlListener(String optionCode, Field field, Object control) {
        if ("extensionItems".equals(field.getName())) {
            return new SimpleObjectProperty<>(getOption().getExtensionItems());
        }
        return super.setControlListener(optionCode, field, control);
    }

    @Override
    public void apply() {
        super.apply();
        for (CheckBox checkBox : extensionCheckBoxList) {
            if (checkBox.isSelected()) {
                markdownExtension.enableExtension(checkBox.getText());
            } else {
                markdownExtension.disableExtension(checkBox.getText());
            }
        }
    }
}
