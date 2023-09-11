package pers.dog.api.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * @author qingsheng.chen@hand-china.com 2023/9/11 16:17
 */
public class MarkdownConfig {
    private boolean extensionAll;
    private Set<String> extensionItems = new HashSet<>();

    public boolean isExtensionAll() {
        return extensionAll;
    }

    public MarkdownConfig setExtensionAll(boolean extensionAll) {
        this.extensionAll = extensionAll;
        return this;
    }

    public Set<String> getExtensionItems() {
        return extensionItems;
    }

    public MarkdownConfig setExtensionItems(Set<String> extensionItems) {
        this.extensionItems = extensionItems;
        return this;
    }
}
