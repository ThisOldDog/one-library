package pers.dog.api.callback;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import pers.dog.api.controller.tool.RecycleController;
import pers.dog.app.service.RecycleService;
import pers.dog.boot.infra.i18n.I18nMessageSource;
import pers.dog.domain.entity.Recycle;

/**
 * @author 废柴 2023/9/25 20:27
 */
public class RecycleTableCellCallback extends TableCell<Recycle, Void> {
    private final HBox optionWrapper = new HBox();

    public RecycleTableCellCallback(RecycleController controller, RecycleService recycleService) {
        Button recover = new Button(I18nMessageSource.getResource("info.project.recycle.operate.recover"));
        recover.setOnAction(event -> {
            Recycle recycle = getTableRow().getItem();
            if (recycle != null) {
                recycleService.recover(recycle);
                controller.reload();
            }
        });
        Button delete = new Button(I18nMessageSource.getResource("info.project.recycle.operate.delete"));
        delete.setOnAction(event -> {
            Recycle recycle = getTableRow().getItem();
            if (recycle != null) {
                recycleService.delete(recycle);
                controller.reload();
            }
        });
        HBox.setMargin(delete, new Insets(0, 0, 0, 16));
        optionWrapper.setAlignment(Pos.CENTER);
        optionWrapper.getChildren().addAll(recover, delete);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(optionWrapper);
        }
    }
}
