package pers.dog.api.controller.tool;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import org.controlsfx.control.PrefixSelectionComboBox;
import org.springframework.util.ObjectUtils;
import pers.dog.boot.infra.dto.ValueMeaning;
import pers.dog.component.translate.TranslateService;
import pers.dog.component.translate.TranslateServiceFactory;

/**
 * @author 废柴 2023/9/12 16:28
 */
public class TranslateController implements Initializable {
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> sourceLanguage;
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> targetLanguage;
    @FXML
    public TextArea sourceText;
    @FXML
    public TextArea targetText;

    private final TranslateServiceFactory translateServiceFactory;
    private final ObjectProperty<TranslateService> translateServiceProperty = new SimpleObjectProperty<>();

    public TranslateController(TranslateServiceFactory translateServiceFactory) {
        this.translateServiceFactory = translateServiceFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translateServiceFactory.onServiceChange((observable, oldValue, newValue) -> onServiceChange(newValue));
        onServiceChange(translateServiceFactory.getService());
        sourceLanguage.valueProperty().addListener((observable, oldValue, newValue) -> translate());
        targetLanguage.valueProperty().addListener((observable, oldValue, newValue) -> translate());
    }

    private void onServiceChange(TranslateService newValue) {
        translateServiceProperty.setValue(newValue);
        if (newValue == null) {
            sourceLanguage.setDisable(true);
            targetLanguage.setDisable(true);
        } else {
            List<ValueMeaning> languages = newValue.languages();
            sourceLanguage.setDisable(false);
            targetLanguage.setDisable(false);
            sourceLanguage.setItems(FXCollections.observableArrayList(languages));
            targetLanguage.setItems(FXCollections.observableArrayList(languages));
            sourceLanguage.setValue(newValue.defaultSourceLanguage());
            targetLanguage.setValue(newValue.defaultTargetLanguage());
        }
    }

    public void swapLanguage() {
        ValueMeaning sourceLanguageValue = sourceLanguage.getValue();
        ValueMeaning targetLanguageValue = targetLanguage.getValue();
        sourceLanguage.setValue(targetLanguageValue);
        targetLanguage.setValue(sourceLanguageValue);
        translate();
    }

    public void translate() {
        TranslateService service = translateServiceProperty.getValue();
        String sourceTextValue = sourceText.getText();
        if (service == null || ObjectUtils.isEmpty(sourceTextValue) || targetLanguage.getValue() == null) {
            return;
        }
        targetText.setText(service.translateMarkdown(sourceTextValue,
                sourceLanguage.getValue() == null ? null : sourceLanguage.getValue().getValue(),
                targetLanguage.getValue().getValue()));
    }

    public void setSourceText(String text) {
        sourceText.setText(text);
    }

    public String getTargetText() {
        return targetText.getText();
    }

}
