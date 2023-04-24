package pers.dog.infra.property;

import pers.dog.boot.infra.dialog.I18nProperty;

@I18nProperty(name = "info.editor.toolbar.link")
public class LinkProperty {
    private String url = "http://";
    private String title = "";

    @I18nProperty(name = "info.editor.toolbar.link_url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @I18nProperty(name = "info.editor.toolbar.link_title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
