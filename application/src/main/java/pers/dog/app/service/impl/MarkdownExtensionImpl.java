package pers.dog.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vladsch.flexmark.util.misc.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.comparator.Comparators;
import pers.dog.app.service.MarkdownExtension;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/29 17:07
 */
@Service
public class MarkdownExtensionImpl implements MarkdownExtension {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownExtensionImpl.class);
    private static Map<String, Class<? extends Extension>> EXTENSION_MAP = new HashMap<>();
    private static Map<String, Boolean> EXTENSION_ENABLED_MAP = new HashMap<>();
    private static List<String> EXTENSION_KEY_LIST = new ArrayList<>();

    static {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Extension.class));
        scanner.addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();

            return classMetadata.isAbstract() || classMetadata.isInterface();
        });
        for (BeanDefinition definition : scanner.findCandidateComponents("com.vladsch.flexmark")) {
            String extensionName = definition.getBeanClassName();
            if (extensionName != null) {
                if (extensionName.contains(".")) {
                    extensionName = extensionName.substring(extensionName.lastIndexOf(".") + 1);
                }
                if (extensionName.endsWith("Extension")) {
                    extensionName = extensionName.substring(0, extensionName.length() - 9);
                }
                try {
                    // noinspection unchecked
                    EXTENSION_MAP.put(extensionName, (Class<? extends Extension>) Class.forName(definition.getBeanClassName()));
                    EXTENSION_KEY_LIST.add(extensionName);
                } catch (ClassNotFoundException e) {
                    logger.error("[Markdown Extension] Unable get class for: " + definition.getBeanClassName(), e);
                }
            }
        }
        EXTENSION_KEY_LIST.sort(Comparators.comparable());
    }

    @Override
    public List<String> listExtension() {
        return null;
    }
}
