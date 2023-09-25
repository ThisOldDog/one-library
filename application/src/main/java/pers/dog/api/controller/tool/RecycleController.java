package pers.dog.api.controller.tool;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pers.dog.api.callback.RecycleTableCellCallback;
import pers.dog.app.service.RecycleService;
import pers.dog.domain.entity.Recycle;

/**
 * @author 废柴 2023/9/22 14:51
 */
public class RecycleController implements Initializable {
    @FXML
    public TextField searchTextField;
    @FXML
    public TableView<Recycle> recycleTableView;
    @FXML
    public TableColumn<Recycle, Void> operateAction;
    private final RecycleService recycleService;

    public RecycleController(RecycleService recycleService) {
        this.recycleService = recycleService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        operateAction.setCellFactory(param -> new RecycleTableCellCallback(this, recycleService));
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> reload());
        reload();
    }

    public void reload() {
        recycleTableView.setItems(FXCollections.observableArrayList(recycleService.list(searchTextField.getText())));
    }

}
