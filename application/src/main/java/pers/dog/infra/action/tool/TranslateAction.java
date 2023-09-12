package pers.dog.infra.action.tool;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.action.Action;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.tool.TranslateController;
import pers.dog.app.service.ProjectEditorService;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.boot.infra.util.FXMLUtils;

@Component
@Scope("prototype")
public class TranslateAction extends Action {
    private static final String VIEW = "tool/translate";
    private Supplier<String> sourceText;
    private Consumer<String> consumerText;
    private Dialog<Void> cache;

    public TranslateAction(ProjectEditorService editorService) {
        super(I18nMessageSource.getResource("info.tool.translate"));
        super.setEventHandler(this::onAction);
        getDialog();
    }

    public void onSourceTextRequest(Supplier<String> sourceText) {
        this.sourceText = sourceText;
    }

    public void onConsumerTextApply(Consumer<String> consumerText) {
        this.consumerText = consumerText;
    }


    private void onAction(ActionEvent actionEvent) {
        getDialog().showAndWait();
    }

    @SuppressWarnings("DuplicatedCode")
    public Dialog<Void> getDialog() {
        if (cache != null) {
            return cache;
        }
        Parent parent = FXMLUtils.loadFXML(VIEW);
        TranslateController controller = FXMLUtils.getController(parent);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(getText());
        dialog.setResizable(true);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(parent);
        dialogPane.getButtonTypes().addAll(ButtonType.NEXT, ButtonType.OK, ButtonType.CANCEL);

        Button translateButton = (Button) dialogPane.lookupButton(ButtonType.NEXT);
        translateButton.setText(I18nMessageSource.getResource("info.tool.translate.action.translate"));
        translateButton.addEventFilter(ActionEvent.ACTION, event -> {
            controller.translate();
            event.consume();
        });
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (consumerText != null) {
                consumerText.accept(controller.getTargetText());
            }
        });
        dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtils.isTrue(newValue) && (sourceText != null)) {
                    controller.setSourceText(sourceText.get());
                    controller.translate();
            }
        });
        dialogPane.autosize();
        cache = dialog;
        return dialog;
    }
}
