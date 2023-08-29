package pers.dog.app.service;

import java.util.List;

import com.vladsch.flexmark.util.misc.Extension;
import javafx.collections.ObservableList;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/29 17:07
 */
public interface MarkdownExtension {
    List<String> listExtension();
    ObservableList<Class<? extends Extension>> enabledExtension();

    void enableExtension(String extension);

    void disableExtension(String extension);
}
