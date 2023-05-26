package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class SearchAction extends BaseEditorAction {
    public SearchAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.search", actionEvent -> editorService.search());
    }

}
