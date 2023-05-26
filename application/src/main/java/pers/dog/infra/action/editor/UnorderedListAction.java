package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class UnorderedListAction extends BaseEditorAction {
    public UnorderedListAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.unordered_list", actionEvent -> editorService.unorderedList());
    }

}
