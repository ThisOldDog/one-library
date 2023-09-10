package pers.dog.api.controller.setting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.control.PrefixSelectionComboBox;
import org.springframework.util.ObjectUtils;
import pers.dog.api.dto.SettingGroup;
import pers.dog.boot.infra.dto.ValueMeaning;

import java.net.URL;
import java.util.*;

public class SettingToolTranslateController implements SettingOptionController, Initializable {
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> translateServiceType;
    @FXML
    public PasswordField apiKey;
    @FXML
    public TextField endpoint;
    @FXML
    public TextField region;

    public static final String SETTING_CODE = "tool-translate";
    public static final String OPTION_SERVER_TYPE = "service-type";
    public static final String OPTION_API_KEY = "api-key";
    public static final String OPTION_ENDPOINT = "endpoint";
    public static final String OPTION_REGION = "region";

    public static final Set<String> OPTION_KEY_LIST = Set.of(OPTION_SERVER_TYPE, OPTION_API_KEY, OPTION_ENDPOINT, OPTION_REGION);

    private final ObservableList<ValueMeaning> translateServices = FXCollections.observableArrayList();

    private final Map<String, Object> optionMap = new HashMap<>();
    private SettingGroup settingGroup;
    private boolean changed = false;

    public SettingToolTranslateController() {
        translateServices.addAll(
                new ValueMeaning().setValue("AZURE_AI_TRANSLATE").setMeaning("Azure AI Translate")
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translateServiceType.setItems(translateServices);
        translateServiceType.valueProperty().addListener((observable, oldValue, newValue) -> {
            changed = true;
            optionMap.put(OPTION_SERVER_TYPE, newValue.getValue());
        });
        apiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            changed = true;
            optionMap.put(OPTION_API_KEY, newValue);
        });
        endpoint.textProperty().addListener((observable, oldValue, newValue) -> {
            changed = true;
            optionMap.put(OPTION_ENDPOINT, newValue);
        });
        region.textProperty().addListener((observable, oldValue, newValue) -> {
            changed = true;
            optionMap.put(OPTION_REGION, newValue);
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

    @Override
    public void setOption(Map<String, Object> option) {
        changed = true;
        loadOption(option);
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadOption(Map<String, Object> option) {
        optionMap.putAll(option);
        String translateServiceTypeValue = (String) option.get(OPTION_SERVER_TYPE);
        if (!ObjectUtils.isEmpty(translateServiceTypeValue)) {
            for (ValueMeaning item : translateServiceType.getItems()) {
                if (Objects.equals(item.getValue(), translateServiceTypeValue)) {
                    translateServiceType.setValue(item);
                }
            }
        }
        String apiKeyValue = (String) option.get(OPTION_API_KEY);
        if (!ObjectUtils.isEmpty(apiKeyValue)) {
            apiKey.setText(apiKeyValue);
        }
        String endpointValue = (String) option.get(OPTION_ENDPOINT);
        if (!ObjectUtils.isEmpty(endpointValue)) {
            endpoint.setText(endpointValue);
        }
        String regionValue = (String) option.get(OPTION_REGION);
        if (!ObjectUtils.isEmpty(regionValue)) {
            region.setText(regionValue);
        }
        settingGroup.setOptions(optionMap);
    }
}
