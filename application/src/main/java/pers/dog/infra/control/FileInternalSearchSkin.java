package pers.dog.infra.control;

import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.SkinBase;
import pers.dog.api.controller.FileInternalSearchController;
import pers.dog.boot.infra.util.FXMLUtils;

public class FileInternalSearchSkin extends SkinBase<FileInternalSearch> {
    private static final String FXML = "file-internal-search";
    private final FileInternalSearchController controller;

    public FileInternalSearchSkin(FileInternalSearch view) {
        super(view);
        Parent parent = FXMLUtils.loadFXML(FXML);
        controller = FXMLUtils.getController(parent);
        Bindings.bindBidirectional(controller.getSearchTextArea().textProperty(), view.searchTextProperty());
        controller.searchActionProperty().bindBidirectional(view.searchActionProperty());
        controller.previousOccurrenceActionProperty().bindBidirectional(view.previousOccurrenceActionProperty());
        controller.nextOccurrenceActionProperty().bindBidirectional(view.nextOccurrenceActionProperty());

        getChildren().add(parent);
    }
}
