package pers.dog.infra.property;

import pers.dog.boot.infra.control.I18nProperty;

/**
 * @author 废柴 2023/9/5 15:25
 */
@I18nProperty(name = "info.project.name")
public class NameProperty {
    private String name;

    @I18nProperty(name = "info.project.name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
