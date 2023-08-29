package pers.dog.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.vladsch.flexmark.util.misc.Extension;
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
import pers.dog.app.service.MarkdownExtension;
import pers.dog.app.service.SettingService;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/29 17:07
 */
@Service
public class MarkdownExtensionImpl implements MarkdownExtension {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownExtensionImpl.class);
    private static final Set<String> SKIP_EXTENSIONS = Set.of(
            "com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension",
            "com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension",
            "com.vladsch.flexmark.jira.converter.JiraConverterExtension");
    /*
            "com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension",
            "com.vladsch.flexmark.ext.enumerated.reference.EnumeratedReferenceExtension",
            "com.vladsch.flexmark.ext.xwiki.macros.MacroExtension",
            "com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension",
            "com.vladsch.flexmark.ext.attributes.AttributesExtension",
            "com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension",
            "com.vladsch.flexmark.ext.superscript.SuperscriptExtension",
            "com.vladsch.flexmark.ext.typographic.TypographicExtension",
            "com.vladsch.flexmark.ext.toc.SimTocExtension",
            "com.vladsch.flexmark.ext.ins.InsExtension",
            "com.vladsch.flexmark.ext.aside.AsideExtension",
            "com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension",
            "com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension",
            "com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension",
            "com.vladsch.flexmark.ext.tables.TablesExtension",
            "com.vladsch.flexmark.ext.emoji.EmojiExtension",
            "com.vladsch.flexmark.ext.jekyll.front.matter.JekyllFrontMatterExtension",
            "com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension",
            "com.vladsch.flexmark.ext.wikilink.WikiLinkExtension",
            "com.vladsch.flexmark.ext.gitlab.GitLabExtension",
            "com.vladsch.flexmark.ext.footnotes.FootnoteExtension",
            "com.vladsch.flexmark.ext.definition.DefinitionExtension",
            "com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension",
            "com.vladsch.flexmark.ext.toc.TocExtension",
            "com.vladsch.flexmark.ext.macros.MacrosExtension",
            "com.vladsch.flexmark.ext.autolink.AutolinkExtension",
            "com.vladsch.flexmark.ext.admonition.AdmonitionExtension",
            "com.vladsch.flexmark.youtrack.converter.YouTrackConverterExtension",
            "com.vladsch.flexmark.ext.resizable.image.ResizableImageExtension",
            "com.vladsch.flexmark.ext.media.tags.MediaTagsExtension",
            "com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension",
            "com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension"
     */
    private static final Map<String, Class<? extends Extension>> EXTENSION_MAP = new HashMap<>();
    private static final List<String> EXTENSION_ENABLE_LIST = new ArrayList<>();
    private final ObservableList<Class<? extends Extension>> extensionEnabled = FXCollections.observableArrayList();

    private final SettingService settingService;

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
        this.settingService = settingService;
        Boolean extensionAll = (Boolean) settingService.getOption(SettingMarkdownConfigController.SETTING_CODE, SettingMarkdownConfigController.OPTION_EXTENSION_ALL);
        Set<String> enabledExtensions = (Set<String>) settingService.getOption(SettingMarkdownConfigController.SETTING_CODE, SettingMarkdownConfigController.OPTION_EXTENSION_ITEMS);
        for (String extensionKey : EXTENSION_ENABLE_LIST) {
            if (BooleanUtils.isTrue(extensionAll) || (enabledExtensions != null && enabledExtensions.contains(extensionKey))) {
                enableExtension(extensionKey);
            }
        }
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
}
