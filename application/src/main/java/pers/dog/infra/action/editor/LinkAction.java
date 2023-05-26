package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class LinkAction extends BaseEditorAction {
    public LinkAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.link", actionEvent -> editorService.link());
    }

}
