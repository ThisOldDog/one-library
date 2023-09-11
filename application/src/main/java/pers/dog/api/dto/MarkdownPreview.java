package pers.dog.api.dto;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/11 16:25
 */
public class MarkdownPreview {
    private String previewStyle;

    public String getPreviewStyle() {
        return previewStyle;
    }

    public MarkdownPreview setPreviewStyle(String previewStyle) {
        this.previewStyle = previewStyle;
        return this;
    }
}
