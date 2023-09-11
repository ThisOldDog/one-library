package pers.dog.app.service.impl;

import java.util.*;
import java.util.function.Consumer;

import com.vladsch.flexmark.util.misc.Extension;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.comparator.Comparators;
import pers.dog.api.controller.setting.SettingMarkdownConfigController;
import pers.dog.api.dto.MarkdownConfig;
import pers.dog.app.service.MarkdownExtension;
import pers.dog.boot.component.setting.SettingService;

/**
 * @author 废柴 2023/8/29 17:07
 */
@Service
public class MarkdownExtensionImpl implements MarkdownExtension {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownExtensionImpl.class);
    private static final Set<String> SKIP_EXTENSIONS = Set.of(
            "com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension",
            "com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension",
            "com.vladsch.flexmark.jira.converter.JiraConverterExtension",
            "com.vladsch.flexmark.youtrack.converter.YouTrackConverterExtension");
    private static final Map<String, Class<? extends Extension>> EXTENSION_MAP = new HashMap<>();
    private static final List<String> EXTENSION_ENABLE_LIST = new ArrayList<>();
    private final ObservableList<Class<? extends Extension>> extensionEnabled = FXCollections.observableArrayList();

    private final List<Consumer<List<Class<? extends Extension>>>> extensionChangeActions = new ArrayList<>();

    static {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Extension.class));
        scanner.addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            return classMetadata.isAbstract() || classMetadata.isInterface();
        });
        for (BeanDefinition definition : scanner.findCandidateComponents("com.vladsch.flexmark")) {
            String extensionName = definition.getBeanClassName();
            if (extensionName != null && !SKIP_EXTENSIONS.contains(extensionName)) {
                if (extensionName.contains(".")) {
                    extensionName = extensionName.substring(extensionName.lastIndexOf(".") + 1);
                }
                if (extensionName.endsWith("Extension")) {
                    extensionName = extensionName.substring(0, extensionName.length() - 9);
                }
                try {
                    // noinspection unchecked
                    EXTENSION_MAP.put(extensionName, (Class<? extends Extension>) Class.forName(definition.getBeanClassName()));
                    EXTENSION_ENABLE_LIST.add(extensionName);
                } catch (ClassNotFoundException e) {
                    logger.error("[Markdown Extension] Unable get class for: " + definition.getBeanClassName(), e);
                }
            }
        }
        EXTENSION_ENABLE_LIST.sort(Comparators.comparable());
    }

    @SuppressWarnings("unchecked")
    public MarkdownExtensionImpl(SettingService settingService) {
        MarkdownConfig markdownConfig = settingService.getOption(SettingMarkdownConfigController.SETTING_CODE);
        Boolean extensionAll = markdownConfig.isExtensionAll();
        Set<String> enabledExtensions = markdownConfig.getExtensionItems();
        for (String extensionKey : EXTENSION_ENABLE_LIST) {
            if (BooleanUtils.isTrue(extensionAll) || (enabledExtensions != null && enabledExtensions.contains(extensionKey))) {
                enableExtension(extensionKey);
            }
        }
        extensionEnabled.addListener((InvalidationListener) observable -> {
            if (!extensionChangeActions.isEmpty()) {
                extensionChangeActions.forEach(action -> action.accept(extensionEnabled));
            }
        });
    }


    @Override
    public List<String> listExtension() {
        return EXTENSION_ENABLE_LIST;
    }

    @Override
    public ObservableList<Class<? extends Extension>> enabledExtension() {
        return extensionEnabled;
    }

    @Override
    public void enableExtension(String extension) {
        Class<? extends Extension> extensionClass = EXTENSION_MAP.get(extension);
        if (extensionClass != null && !extensionEnabled.contains(extensionClass)) {
            extensionEnabled.add(extensionClass);
        }
    }

    @Override
    public void disableExtension(String extension) {
        Class<? extends Extension> extensionClass = EXTENSION_MAP.get(extension);
        if (extensionClass != null) {
            extensionEnabled.remove(extensionClass);
        }
    }

    public void onExtensionChanged(Consumer<List<Class<? extends Extension>>> action) {
        this.extensionChangeActions.add(action);
    }

    public void removeOnExtensionChanged(Consumer<List<Class<? extends Extension>>> action) {
        this.extensionChangeActions.add(action);
    }
}
