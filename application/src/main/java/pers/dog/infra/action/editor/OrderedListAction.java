package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class OrderedListAction extends BaseEditorAction {
    public OrderedListAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.ordered_list", actionEvent -> editorService.orderedList());
    }

}
