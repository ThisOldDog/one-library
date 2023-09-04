package pers.dog.infra.property;

import pers.dog.boot.infra.control.I18nProperty;

@I18nProperty(name = "info.editor.toolbar.image")
public class ImageProperty {
    private String name = "";
    private String url = "http://";
    private String title = "";

    @I18nProperty(name = "info.editor.toolbar.image_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @I18nProperty(name = "info.editor.toolbar.image_url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @I18nProperty(name = "info.editor.toolbar.image_title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
