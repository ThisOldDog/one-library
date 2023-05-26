package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class OnlyEditorAction extends BaseEditorAction {
    public OnlyEditorAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.only_editor", actionEvent -> editorService.onlyEditor());
    }

}
