package pers.dog.boot.autoconfiguration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.CollectionUtils;
import pers.dog.boot.component.cache.CacheProperties;
import pers.dog.boot.component.cache.status.SceneStatueResumeListener;
import pers.dog.boot.component.cache.status.StageStatueResumeListener;
import pers.dog.boot.component.cache.status.StatueSaveListener;
import pers.dog.boot.component.cache.status.StatusConstants;
import pers.dog.boot.component.cache.status.StatusStore;
import pers.dog.boot.component.file.FileOperationHolder;
import pers.dog.boot.context.property.ApplicationProperties;
import pers.dog.boot.i18n.I18nMessageSource;
import pers.dog.boot.i18n.I18nProperties;
import pers.dog.boot.i18n.I18nResourceLoader;
import pers.dog.boot.util.FXMLUtils;

/**
 * @author 废柴 2021/6/15 20:03
 */
@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class JavaFXSpringBootStarterAutoConfiguration {

    @SuppressWarnings("rawtypes")
    @Configuration
    @EnableConfigurationProperties(CacheProperties.class)
    public static class CacheAutoConfiguration {

        @ConditionalOnProperty(name = "javafx.cache.status.enable", havingValue = "true", matchIfMissing = true)
        public static class CacheStatusAutoConfiguration {

            public CacheStatusAutoConfiguration(CacheProperties cacheProperties,
                                                @Value("${spring.application.name:application}") String applicationName) {
                FileOperationHolder.registerAsDefault(StatusConstants.CACHE_FILE_HOLDER_NAME, cacheProperties, applicationName);
            }
            @Bean
            public StatueSaveListener statueSaveApplicationCloseEventListener(ApplicationProperties properties,
                                                                              ObjectProvider<List<? extends StatusStore>> statusStoreList) {
                return new StatueSaveListener(properties.getStatus(), FileOperationHolder.getHandler(StatusConstants.CACHE_FILE_HOLDER_NAME), statusStoreList);
            }

            @Bean
            public SceneStatueResumeListener sceneStatueResumeListener(ApplicationProperties properties,
                                                                       ObjectProvider<List<? extends StatusStore>> statusStoreList) {
                return new SceneStatueResumeListener(properties.getStatus(), FileOperationHolder.getHandler(StatusConstants.CACHE_FILE_HOLDER_NAME), statusStoreList);
            }

            @Bean
            public StageStatueResumeListener stageStatueResumeListener(ApplicationProperties properties,
                                                                       ObjectProvider<List<? extends StatusStore>> statusStoreList) {
                return new StageStatueResumeListener(properties.getStatus(), FileOperationHolder.getHandler(StatusConstants.CACHE_FILE_HOLDER_NAME), statusStoreList);
            }
        }
    }

    @Configuration
    @EnableConfigurationProperties(I18nProperties.class)
    @ConditionalOnProperty(value = "javafx.i18n.enable", havingValue = "true", matchIfMissing = true)
    public static class I18nAutoConfiguration {
        private static final String DEFAULT_I18N_SOURCE = "messages/messages";

        private final I18nProperties i18nProperties;

        public I18nAutoConfiguration(I18nProperties i18nProperties) {
            this.i18nProperties = i18nProperties;
        }

        @Bean
        public ResourceBundleMessageSource messageSource(List<I18nResourceLoader> loaders) {
            ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
            I18nMessageSource.setMessageSource(resourceBundleMessageSource);
            resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
            resourceBundleMessageSource.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
            Set<String> locations = i18nProperties.getLocation();
            locations.add(DEFAULT_I18N_SOURCE);
            for (String location : locations) {
                resourceBundleMessageSource.addBasenames(location);
                FXMLUtils.addBundleBaseName(location);
            }
            loaders.forEach(loader -> {
                List<String> resourceLocation = loader.resourceLocation();
                if (!CollectionUtils.isEmpty(resourceLocation)) {
                    resourceBundleMessageSource.addBasenames(resourceLocation.toArray(new String[0]));
                }
            });
            return resourceBundleMessageSource;
        }
    }
}
