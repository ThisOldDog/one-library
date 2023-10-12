package pers.dog.infra.action.view;

import java.util.Optional;

import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionCheck;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.springframework.stereotype.Component;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.boot.component.control.ControlProvider;
import pers.dog.boot.component.control.FXMLControl;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.infra.status.StageStatusStore;

/**
 * @author 废柴 2023/10/11 17:18
 */
@Component
@ActionCheck
public class SidebarVisibleAction extends Action {
    @FXMLControl(controller = OneLibraryController.class)
    public SplitPane projectSplitPane;
    @FXMLControl(controller = OneLibraryController.class)
    public Pane projectWorkspace;
    @FXMLControl(controller = OneLibraryController.class)
    public ControlProvider<Button> sidebarVisibleButton = new ControlProvider<>();
    private final StageStatusStore statusStore;

    public SidebarVisibleAction(StageStatusStore statusStore) {
        super(I18nMessageSource.getResource("info.action.view.sidebar"));
        this.statusStore = statusStore;
        setEventHandler(event -> onAction());
        statusStore.addAfterReadListener(stageStatus -> {
            StageStatusStore.ViewStatus viewStatus = Optional.ofNullable(statusStore.getStageStatus().getViewStatus()).orElseGet(StageStatusStore.ViewStatus::new);
            setSelected(viewStatus.isShowSidebar());
            onAction();
        });
        sidebarVisibleButton.afterAssignment(button -> button.setOnAction(event -> setSelected(!isSelected())));
    }

    private void onAction() {
        boolean selected = isSelected();
        statusStore.getStageStatus().getViewStatus().setShowSidebar(selected);
        if (selected) {
            if (!projectSplitPane.getItems().isEmpty() && projectSplitPane.getItems().get(0) != projectWorkspace) {
                projectSplitPane.getItems().add(0, projectWorkspace);
                projectSplitPane.setDividerPositions(0.2D);
            }
            sidebarVisibleButton.get().setTooltip(new Tooltip(I18nMessageSource.getResource("info.action.view.sidebar.hide")));
            sidebarVisibleButton.get().setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.STEP_BACKWARD));
        } else {
            if (!projectSplitPane.getItems().isEmpty() && projectSplitPane.getItems().get(0) == projectWorkspace) {
                projectSplitPane.getItems().remove(0);
            }
            sidebarVisibleButton.get().setTooltip(new Tooltip(I18nMessageSource.getResource("info.action.view.sidebar.show")));
            sidebarVisibleButton.get().setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.STEP_FORWARD));
        }
    }
}
