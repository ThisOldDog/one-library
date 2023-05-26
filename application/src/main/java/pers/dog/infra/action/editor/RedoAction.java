package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class RedoAction extends BaseEditorAction {
    public RedoAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.redo", actionEvent -> editorService.redo());
    }

}
