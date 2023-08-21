package pers.dog.api.controller.dto;

/**
 * @author 废柴 2023/8/17 20:08
 */
public class MarkdownStyle {
    private String name;
    private String code;

    public String getName() {
        return name;
    }

    public MarkdownStyle setName(String name) {
        this.name = name;
        return this;
    }

    public String getCode() {
        return code;
    }

    public MarkdownStyle setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
