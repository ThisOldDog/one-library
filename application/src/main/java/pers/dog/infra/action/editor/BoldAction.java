package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class BoldAction extends BaseEditorAction {
    public BoldAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.bold", actionEvent -> editorService.bold());
    }

}
