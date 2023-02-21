package pers.dog.boot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import pers.dog.boot.context.ApplicationContextHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author 废柴 2021/6/15 19:51
 */
public class ImageLoader {
    private static final Logger logger = LoggerFactory.getLogger(ImageLoader.class);
    private static final Set<String> IMAGE_SUFFIX = Set.of("png", "jpeg", "jpg", "gif");

    public static void load(String name, Consumer<InputStream> apply) {
        IMAGE_SUFFIX.forEach(bannerSuffix -> {
            Resource resource = ApplicationContextHolder.getResourceLoader().getResource(name + "." + bannerSuffix);
            if (resource.exists()) {
                logger.info("[Image Loader] Loaded image: {}", resource.getFilename());
                try (InputStream inputStream = resource.getInputStream()) {
                    apply.accept(inputStream);
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot access file:" + resource.getFilename());
                }
            }
        });
    }
}
