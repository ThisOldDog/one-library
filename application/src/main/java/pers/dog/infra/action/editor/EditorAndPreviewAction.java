package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class EditorAndPreviewAction extends BaseEditorAction {
    public EditorAndPreviewAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.editor_and_preview", actionEvent -> editorService.editorAndPreview());
    }

}
