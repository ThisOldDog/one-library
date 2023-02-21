package pers.dog.boot.context.property;

/**
 * @author 废柴 2021/5/27 19:54
 */
public class StageProperties {
    private String title;
    private double minWidth;
    private double minHeight;

    public String getTitle() {
        return title;
    }

    public StageProperties setTitle(String title) {
        this.title = title;
        return this;
    }

    public double getMinWidth() {
        return minWidth;
    }

    public StageProperties setMinWidth(double minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public double getMinHeight() {
        return minHeight;
    }

    public StageProperties setMinHeight(double minHeight) {
        this.minHeight = minHeight;
        return this;
    }
}
