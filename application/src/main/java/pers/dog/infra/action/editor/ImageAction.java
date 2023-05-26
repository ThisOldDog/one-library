package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class ImageAction extends BaseEditorAction {
    public ImageAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.image", actionEvent -> editorService.image());
    }

}
