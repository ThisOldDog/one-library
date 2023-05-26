package pers.dog.infra.control;

import java.util.Objects;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.SkinBase;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import pers.dog.api.controller.FileInternalSearchController;
import pers.dog.boot.infra.util.FXMLUtils;

public class FileInternalSearchSkin extends SkinBase<FileInternalSearch> {
    private static final String FXML = "file-internal-search";
    private final FileInternalSearchController controller;

    public FileInternalSearchSkin(FileInternalSearch view) {
        super(view);
        Parent parent = FXMLUtils.loadFXML(FXML);
        controller = FXMLUtils.getController(parent);
        Bindings.bindBidirectional(controller.getSearchTextField().textProperty(), view.searchTextProperty());

        controller.searchActionProperty().bindBidirectional(view.searchActionProperty());
        controller.previousOccurrenceActionProperty().bindBidirectional(view.previousOccurrenceActionProperty());
        controller.nextOccurrenceActionProperty().bindBidirectional(view.nextOccurrenceActionProperty());

        controller.getCurrentIndex().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && ObjectUtils.isNotEmpty(newValue)) {
                view.currentIndexProperty().set(Integer.valueOf(newValue));
            }
        });

        view.requestForceProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtils.isTrue(newValue)) {
                requestFocus(view);
            }
        });
        view.searchCandidateCountProperty().addListener((observable, oldValue, newValue) -> {
            controller.sumText.setText("/ " + newValue);
            controller.currentIndex.setDisable(newValue == 0);
        });
        view.currentIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                controller.getCurrentIndex().setText(String.valueOf(newValue));
            }
        });

        getChildren().add(parent);

        layout(view);
    }

    private void layout(FileInternalSearch view) {
        controller.searchTextField.setText("");

        view.searchCandidateCountProperty().set(0);

        requestFocus(view);
    }

    private void requestFocus(FileInternalSearch view) {
        controller.searchTextField.requestFocus();
        view.requestForceProperty().set(false);
    }
}
