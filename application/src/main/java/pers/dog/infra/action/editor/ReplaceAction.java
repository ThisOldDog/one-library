package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class ReplaceAction extends BaseEditorAction {
    public ReplaceAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.replace", actionEvent -> editorService.replace());
    }

}
