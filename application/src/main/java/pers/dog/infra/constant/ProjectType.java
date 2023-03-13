package pers.dog.infra.constant;

/**
 * @author 废柴 2023/2/21 20:31
 */
public enum ProjectType {
    DIRECTORY("static/icon/project/dir"),
    FILE(null);
    private final String icon;

    ProjectType(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}
