package pers.dog.app.service;

public interface ProjectEditorService {
    void quash();

    void redo();

    void save();

    void bold();

    void italic();

    void strikethrough();

    void header();

    void blockQuote();

    void unorderedList();

    void orderedList();

    void table();

    void dividingLine();

    void link();

    void image();

    void codeInline();

    void codeBlock();

    void search();

    void onlyEditor();

    void onlyPreview();

    void editorAndPreview();
}
