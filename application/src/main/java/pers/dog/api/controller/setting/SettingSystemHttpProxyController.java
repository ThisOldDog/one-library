package pers.dog.api.controller.setting;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import pers.dog.api.dto.HttpProxy;
import pers.dog.app.service.HttpProxyService;
import pers.dog.boot.component.control.NumberField;
import pers.dog.boot.component.setting.AbstractSettingOptionController;
import pers.dog.boot.infra.i18n.I18nMessageSource;

/**
 * @author 废柴 2023/9/21 10:11
 */
public class SettingSystemHttpProxyController extends AbstractSettingOptionController<HttpProxy> {
    private static final Logger logger = LoggerFactory.getLogger(SettingSystemHttpProxyController.class);
    public static final String SETTING_CODE = "http-proxy";
    @FXML
    public RadioButton noProxy;
    @FXML
    public RadioButton manualProxy;
    @FXML
    public RadioButton manualProxyHttp;
    @FXML
    public RadioButton manualProxySocks;
    @FXML
    public TextField hostName;
    @FXML
    public NumberField<Integer> portNumber;
    @FXML
    public TextField noProxyHostName;
    @FXML
    public TextField testConnectionHostName;
    @FXML
    public Button testConnectionButton;
    @FXML
    public HBox testConnectionResult;
    @FXML
    public MaskerPane masker;

    private final HttpProxyService httpProxyService;

    private final List<Node> testDisablePrompt;
    private final List<Node> testSuccessPrompt;
    private final List<Node> testFailedPrompt;

    public SettingSystemHttpProxyController(HttpProxyService httpProxyService) {
        this.httpProxyService = httpProxyService;
        testDisablePrompt = Arrays.asList(
                new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.ORANGE),
                new Text(I18nMessageSource.getResource("info.setting.system.http-proxy.manual-proxy.test.disable.prompt"))
        );
        testSuccessPrompt = Arrays.asList(
                new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE).color(Color.GREEN),
                new Text(I18nMessageSource.getResource("info.setting.system.http-proxy.manual-proxy.test.success.prompt"))
        );
        testFailedPrompt = Arrays.asList(
                new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.RED),
                new Text(I18nMessageSource.getResource("info.setting.system.http-proxy.manual-proxy.test.failed.prompt"))
        );
        HBox.setMargin(testDisablePrompt.get(0), new Insets(0, 4, 0, 0));
        HBox.setMargin(testSuccessPrompt.get(0), new Insets(0, 4, 0, 0));
        HBox.setMargin(testFailedPrompt.get(0), new Insets(0, 4, 0, 0));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        manualProxy.selectedProperty().addListener((observable, oldValue, newValue) -> manualEnable(newValue));
        manualEnable(manualProxy.isSelected());
        changedProperty().addListener((observable, oldValue, newValue) -> {
            testConnectionResult.getChildren().clear();
            if (BooleanUtils.isTrue(newValue)) {
                testConnectionResult.getChildren().addAll(testDisablePrompt);
            }
            testConnectionButton.setDisable(BooleanUtils.isTrue(newValue) && !ObjectUtils.isEmpty(testConnectionHostName.getText()));
            testConnectionHostName.setDisable(BooleanUtils.isTrue(newValue));
        });
        testConnectionHostName.textProperty().addListener((observable, oldValue, newValue) -> testConnectionButton.setDisable(BooleanUtils.isTrue(changed()) && ObjectUtils.isEmpty(newValue)));
    }

    private void manualEnable(Boolean newValue) {
        manualProxyHttp.setDisable(BooleanUtils.isNotTrue(newValue));
        manualProxySocks.setDisable(BooleanUtils.isNotTrue(newValue));
        hostName.setDisable(BooleanUtils.isNotTrue(newValue));
        portNumber.setDisable(BooleanUtils.isNotTrue(newValue));
        noProxyHostName.setDisable(BooleanUtils.isNotTrue(newValue));
    }

    public void testConnection() {
        testConnectionResult.getChildren().clear();
        masker.setVisible(true);
        new Thread(() -> {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(testConnectionHostName.getText()).openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                Assert.isTrue(responseCode >= 200 && responseCode < 300,
                        "Request " + testConnectionHostName.getText() + " failed with code: " + responseCode);
                Platform.runLater(() -> {
                    testConnectionResult.getChildren().clear();
                    testConnectionResult.getChildren().addAll(testSuccessPrompt);
                    masker.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    testConnectionResult.getChildren().clear();
                    testConnectionResult.getChildren().addAll(testFailedPrompt);
                    masker.setVisible(false);
                });
                logger.error("Request " + testConnectionHostName.getText() + " failed", e);
            }
        }).start();
    }

    @Override
    public void apply() {
        super.apply();
        httpProxyService.setProxy(getOption());
    }
}
