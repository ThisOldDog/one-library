package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class DividingLineAction extends BaseEditorAction {
    public DividingLineAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.dividing_line", actionEvent -> editorService.dividingLine());
    }

}
