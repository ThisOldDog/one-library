package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class OnlyPreviewAction extends BaseEditorAction {
    public OnlyPreviewAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.only_preview", actionEvent -> editorService.onlyPreview());
    }

}
