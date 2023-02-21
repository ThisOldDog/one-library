package pers.dog.boot.context.property;

/**
 * @author 废柴 2021/5/27 19:54
 */
public class SceneProperties {
    private String name;
    private double width = Double.NaN;
    private double height = Double.NaN;

    public String getName() {
        return name;
    }

    public SceneProperties setName(String name) {
        this.name = name;
        return this;
    }

    public double getWidth() {
        return width;
    }

    public SceneProperties setWidth(double width) {
        this.width = width;
        return this;
    }

    public double getHeight() {
        return height;
    }

    public SceneProperties setHeight(double height) {
        this.height = height;
        return this;
    }
}
