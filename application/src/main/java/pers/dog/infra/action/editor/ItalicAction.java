package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class ItalicAction extends BaseEditorAction {
    public ItalicAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.italic", actionEvent -> editorService.italic());
    }

}
