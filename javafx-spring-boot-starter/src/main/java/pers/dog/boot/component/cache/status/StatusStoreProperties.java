package pers.dog.boot.component.cache.status;

/**
 *
 * @author 废柴 2021/6/21 20:38
 */
public class StatusStoreProperties {
    private boolean enable = true;
    private String fileName = ".status.json";
    private String[] path = {".data", "cache"};

    public boolean isEnable() {
        return enable;
    }

    public StatusStoreProperties setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public StatusStoreProperties setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String[] getPath() {
        return path;
    }

    public StatusStoreProperties setPath(String[] path) {
        this.path = path;
        return this;
    }
}
