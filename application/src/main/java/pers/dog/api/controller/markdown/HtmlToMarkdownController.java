package pers.dog.api.controller.markdown;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.web.WebView;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.PrefixSelectionComboBox;
import pers.dog.api.controller.setting.SettingToolTranslateController;
import pers.dog.app.service.ProjectService;
import pers.dog.app.service.SettingService;
import pers.dog.boot.infra.dto.ValueMeaning;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.AlertUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.control.MarkdownCodeArea;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author 废柴 2023/8/21 19:53
 */
public class HtmlToMarkdownController implements Initializable {
    public interface TranslateService {
        List<ValueMeaning> languages();

        String translateHtml(String document, String sourceLanguage, String targetLanguage);
    }

    public static class AzureTranslateService implements TranslateService {
        private final TextTranslationClient client;
        private final List<ValueMeaning> languages;

        public AzureTranslateService(String apiKey, String endpoint, String region) {
            client = new TextTranslationClientBuilder()
                    .credential(new AzureKeyCredential(apiKey))
                    .endpoint(endpoint)
                    .region(region)
                    .buildClient();
            languages = client.getLanguages()
                    .getTranslation()
                    .values()
                    .stream()
                    .map(translationLanguage -> new ValueMeaning().setValue(translationLanguage.getName()).setMeaning(translationLanguage.getNativeName()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<ValueMeaning> languages() {
            return languages;
        }

        @Override
        public String translateHtml(String document, String sourceLanguage, String targetLanguage) {
            List<TranslatedTextItem> translate = client.translate(Collections.singletonList(targetLanguage), Collections.singletonList(new InputTextItem(document)), null, sourceLanguage, TextType.HTML, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);
            return translate.get(0).getTranslations().get(0).getText();
        }
    }

    public static class TranslateServiceFactory {
        public static TranslateService getService(String serverType, String apiKey, String endpoint, String region) {
            if ("AZURE_AI_TRANSLATE".equals(serverType)) {
                return new AzureTranslateService(apiKey, endpoint, region);
            }
            return null;
        }
    }

    private static final ObservableList<ValueMeaning> INSERT_POSITION_ALL = FXCollections.observableArrayList(
            new ValueMeaning()
                    .setValue("NEW_PROJECT")
                    .setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.new_project")),
            new ValueMeaning()
                    .setValue("CURSOR")
                    .setMeaning(I18nMessageSource.getResource("info.project.html-to-markdown.insert-position.cursor"))
    );

    @FXML
    public TextArea url;
    @FXML
    public PrefixSelectionComboBox<ValueMeaning> insertPosition;
    @FXML
    public WebView contentPreview;
    @FXML
    public MarkdownCodeArea markdownPreview;
    @FXML
    public MaskerPane masker;
    @FXML
    public TextField directory;
    @FXML
    public TextField projectName;
    private final FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();

    private final ProjectService projectService;
    private final SettingService settingService;
    public PrefixSelectionComboBox<ValueMeaning> sourceLanguage;
    public PrefixSelectionComboBox<ValueMeaning> targetLanguage;
    private TranslateService translateService;

    public HtmlToMarkdownController(ProjectService projectService, SettingService settingService) {
        this.projectService = projectService;
        this.settingService = settingService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentPreview.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                try {
                    String document = String.valueOf(contentPreview.getEngine().executeScript("document.documentElement.outerHTML"));
                    if (sourceLanguage.getValue() != null && targetLanguage.getValue() != null) {
                        document = translateService.translateHtml(document, sourceLanguage.getValue().getValue(), targetLanguage.getValue().getValue());
                    }
                    String markdown = converter.convert(document);
                    markdownPreview.replaceText(markdown);
                } finally {
                    masker.setVisible(false);
                }
            } else if (newValue == Worker.State.FAILED) {
                masker.setVisible(false);
                AlertUtils.showException("info.project.html-to-markdown.html.error.title",
                        "info.project.html-to-markdown.html.error.header_text",
                        "info.project.html-to-markdown.html.error.content_text",
                        "info.project.html-to-markdown.html.error.exception_stacktrace",
                        contentPreview.getEngine().getLoadWorker().getException());
            }
        });
        String serverType = (String) settingService.getOption(SettingToolTranslateController.SETTING_CODE, SettingToolTranslateController.OPTION_SERVER_TYPE);
        String apiKey = (String) settingService.getOption(SettingToolTranslateController.SETTING_CODE, SettingToolTranslateController.OPTION_API_KEY);
        String endpoint = (String) settingService.getOption(SettingToolTranslateController.SETTING_CODE, SettingToolTranslateController.OPTION_ENDPOINT);
        String region = (String) settingService.getOption(SettingToolTranslateController.SETTING_CODE, SettingToolTranslateController.OPTION_REGION);
        translateService = TranslateServiceFactory.getService(serverType, apiKey, endpoint, region);
        sourceLanguage.setItems(FXCollections.observableArrayList(translateService.languages()));
        targetLanguage.setItems(FXCollections.observableArrayList(translateService.languages()));
    }

    public void saveToDirectory() {
        insertPosition.setItems(INSERT_POSITION_ALL);
        insertPosition.setValue(INSERT_POSITION_ALL.get(0));
        directory.setText(getProjectTreeDirectory());
        projectName.setDisable(false);
    }

    public void saveToProject() {
        insertPosition.setItems(INSERT_POSITION_ALL);
        insertPosition.setValue(INSERT_POSITION_ALL.get(1));
        directory.setText(getProjectTreeDirectory());
        projectName.setText(projectService.currentProject().getValue().getSimpleProjectName());
    }


    private String getProjectTreeDirectory() {
        TreeItem<Project> projectTreeItem = projectService.currentDirectory();
        if (ProjectService.ROOT == projectTreeItem) {
            return projectTreeItem.getValue().getProjectName();
        }
        return getProjectTreeDirectory(null, projectTreeItem);
    }

    private String getProjectTreeDirectory(String path, TreeItem<Project> node) {
        if (node == null || ProjectService.ROOT == node) {
            return path;
        }
        if (node.getValue() != null) {
            path = path == null ? node.getValue().getProjectName() : (node.getValue().getProjectName() + File.separator + path);
        }
        return getProjectTreeDirectory(path, node.getParent());
    }

    public TextArea getUrl() {
        return url;
    }

    public PrefixSelectionComboBox<ValueMeaning> getInsertPosition() {
        return insertPosition;
    }

    public WebView getContentPreview() {
        return contentPreview;
    }

    public MarkdownCodeArea getMarkdownPreview() {
        return markdownPreview;
    }

    public void preview() {
        masker.setVisible(true);
        markdownPreview.clear();
        contentPreview.getEngine().load(url.getText());
    }

    public String getMarkdown() {
        return markdownPreview.getText();
    }
}
