package pers.dog.api.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/23 23:06
 */
public class ProjectEditorLineController implements Initializable {
    @FXML
    private Label lineNumber;
    @FXML
    private TextField lineText;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lineNumber.setLabelFor(lineText);
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber.setText(String.valueOf(lineNumber));
    }

    public void setLineText(String text) {
        this.lineText.setText(text);
    }
}
