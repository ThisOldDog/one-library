package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class HeaderAction extends BaseEditorAction {
    public HeaderAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.header", actionEvent -> editorService.header());
    }

}
