package pers.dog.app.service;

import java.util.List;
import java.util.function.Consumer;

import com.vladsch.flexmark.util.misc.Extension;
import javafx.collections.ObservableList;

/**
 * @author 废柴 2023/8/29 17:07
 */
public interface MarkdownExtension {
    List<String> listExtension();
    ObservableList<Class<? extends Extension>> enabledExtension();

    void enableExtension(String extension);

    void disableExtension(String extension);

    void onExtensionChanged(Consumer<List<Class<? extends Extension>>> action);
    void removeOnExtensionChanged(Consumer<List<Class<? extends Extension>>> action);
}
