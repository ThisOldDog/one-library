package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class QuashAction extends BaseEditorAction {
    public QuashAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.quash", actionEvent -> editorService.quash());
    }

}
