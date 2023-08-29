package pers.dog.boot.infra.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import pers.dog.boot.context.ApplicationContextHolder;

/**
 * @author 废柴 2021/6/15 19:51
 */
public class ImageUtils {
    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);
    private static final Set<String> IMAGE_SUFFIX = Set.of("png", "jpeg", "jpg", "gif", "ico");
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

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

    public static Image getImage(String name) {
        return IMAGE_CACHE.computeIfAbsent(name, key -> {
            AtomicReference<Image> image = new AtomicReference<>();
            load(key, inputStream -> image.set(new Image(inputStream)));
            return image.get();
        });
    }
}
