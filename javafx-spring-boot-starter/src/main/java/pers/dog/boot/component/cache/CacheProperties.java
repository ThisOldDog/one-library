package pers.dog.boot.component.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pers.dog.boot.component.file.FileOperationOption;

/**
 * 缓存配置
 *
 * @author 废柴 2023/2/20 19:36
 */
@ConfigurationProperties(prefix = "javafx.cache")
public class CacheProperties extends FileOperationOption {
}
