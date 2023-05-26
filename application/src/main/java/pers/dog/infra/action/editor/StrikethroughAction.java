package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class StrikethroughAction extends BaseEditorAction {
    public StrikethroughAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.strikethrough", actionEvent -> editorService.strikethrough());
    }

}
