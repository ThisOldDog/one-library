package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class TableAction extends BaseEditorAction {
    public TableAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.table", actionEvent -> editorService.table());
    }

}
