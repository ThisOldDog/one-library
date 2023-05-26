package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class CodeInlineAction extends BaseEditorAction {
    public CodeInlineAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.code_inline", actionEvent -> editorService.codeInline());
    }

}
