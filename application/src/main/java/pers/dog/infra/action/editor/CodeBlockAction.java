package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class CodeBlockAction extends BaseEditorAction {
    public CodeBlockAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.code_block", actionEvent -> editorService.codeBlock());
    }

}
