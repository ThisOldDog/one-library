package pers.dog.app.service.impl;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Service;
import pers.dog.api.controller.OneLibraryController;
import pers.dog.api.controller.ProjectEditorController;
import pers.dog.app.service.ProjectEditorService;
import pers.dog.boot.component.control.FXMLControl;

@Service
public class ProjectEditorServiceImpl implements ProjectEditorService {
    @FXMLControl(controller = OneLibraryController.class)
    private TabPane projectEditorWorkspace;

    private ProjectEditorController editorController() {
        Tab selectedItem = projectEditorWorkspace.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return null;
        }
        return (ProjectEditorController) selectedItem.getUserData();
    }

    @Override
    public void quash() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.quash();
    }


    @Override
    public void redo() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.redo();
    }

    @Override
    public void save() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.save();
    }

    @Override
    public void bold() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.bold();
    }

    @Override
    public void italic() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.italic();
    }

    @Override
    public void strikethrough() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.strikethrough();
    }

    @Override
    public void header() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.header();
    }

    @Override
    public void blockQuote() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.blockQuote();
    }

    @Override
    public void unorderedList() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.unorderedList();
    }

    @Override
    public void orderedList() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.orderedList();
    }

    @Override
    public void table() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.table();
    }

    @Override
    public void dividingLine() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.dividingLine();
    }

    @Override
    public void link() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.link();
    }

    @Override
    public void image() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.image();
    }

    @Override
    public void codeInline() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.codeInline();
    }

    @Override
    public void codeBlock() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.codeBlock();
    }

    @Override
    public void search() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.search();
    }

    @Override
    public void onlyEditor() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.onlyEditor();
    }

    @Override
    public void onlyPreview() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.onlyPreview();
    }

    @Override
    public void editorAndPreview() {
        ProjectEditorController editorController = editorController();
        if (editorController == null) {
            return;
        }
        editorController.editorAndPreview();
    }
}
