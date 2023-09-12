package pers.dog.api.controller.setting;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.control.PrefixSelectionComboBox;
import pers.dog.api.dto.ToolTranslate;
import pers.dog.boot.component.setting.AbstractSettingOptionController;
import pers.dog.boot.infra.dto.ValueMeaning;
import pers.dog.infra.constant.TranslateServiceType;

public class SettingToolTranslateController extends AbstractSettingOptionController<ToolTranslate> {
    public static final String SETTING_CODE = "tool-translate";
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> serviceType;
    @FXML
    public PasswordField apiKey;
    @FXML
    public TextField region;
    @FXML
    public TextField textTranslateEndpoint;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceType.setItems(FXCollections.observableArrayList(
                Arrays.stream(TranslateServiceType.values())
                        .map(item -> new ValueMeaning().setValue(item.name()).setMeaning(item.getProductName()))
                        .toList()));
        addOptionValueConverter(
                "service-type",
                value -> value == null ? null : TranslateServiceType.valueOf(((ValueMeaning) value).getValue()),
                value -> {
                    for (ValueMeaning item : serviceType.getItems()) {
                        if (Objects.equals(item.getValue(), ((TranslateServiceType) value).name())) {
                            return item;
                        }
                    }
                    return null;
                });
        super.initialize(location, resources);
    }
}
