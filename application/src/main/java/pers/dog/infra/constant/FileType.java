package pers.dog.infra.constant;

/**
 * @author 废柴 2023/2/21 20:32
 */
public enum FileType {
    MARKDOWN("Markdown", "md", "static/icon/project/markdown"),
    UNKNOWN("Unknown", "", "static/icon/project/markdown");
    private final String name;
    private final String suffix;
    private final String icon;

    FileType(String name, String suffix, String icon) {
        this.name = name;
        this.suffix = suffix;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getIcon() {
        return icon;
    }

    public static FileType identify(String fileName) {
        for (FileType fileType : FileType.values()) {
            if (fileName.endsWith(fileType.suffix)) {
                return fileType;
            }
        }
        return UNKNOWN;
    }
}
