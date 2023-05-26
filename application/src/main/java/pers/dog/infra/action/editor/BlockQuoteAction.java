package pers.dog.infra.action.editor;

import org.springframework.stereotype.Component;
import pers.dog.app.service.ProjectEditorService;

@Component
public class BlockQuoteAction extends BaseEditorAction {
    public BlockQuoteAction(ProjectEditorService editorService) {
        super("info.editor.toolbar.block_quote", actionEvent -> editorService.blockQuote());
    }

}
