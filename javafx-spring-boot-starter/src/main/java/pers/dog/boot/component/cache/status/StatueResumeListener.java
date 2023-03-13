package pers.dog.boot.component.cache.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.infra.util.ObjectMapperUtils;

/**
 * 程序状态保存监听器
 *
 * @author 废柴 2021/6/21 19:14
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class StatueResumeListener<T extends ApplicationEvent> implements ApplicationListener<T> {
    private final StatusStoreProperties properties;
    private final FileOperationHandler fileOperationHandler;
    private final List<StatusStore> statusStoreList;
    private Map<String, JsonNode> cache;

    public StatueResumeListener(StatusStoreProperties properties,
                                FileOperationHandler fileOperationHandler,
                                List<StatusStore> statusStoreList) {
        this.properties = properties;
        this.fileOperationHandler = fileOperationHandler;
        this.statusStoreList = statusStoreList;
    }

    @Override
    public void onApplicationEvent(T event) {
        Map<String, JsonNode> applicationStatusMap = readValue();
        statusStoreList.forEach(store -> read(store, applicationStatusMap));
    }

    protected synchronized Map<String, JsonNode> readValue() {
        if (cache == null) {
            cache = fileOperationHandler.read(properties.getFileName(),
                            new TypeReference<>() {
                            },
                            properties.getPath());
        }
        return cache;
    }

    private void read(StatusStore store, Map<String, JsonNode> applicationStatusMap) {
        Pair<String, Object> target;
        JsonNode statusValue;
        Class<?> valueType;
        Object value;
        if (((target = store.getTarget())) != null
                && applicationStatusMap != null
                && target.getRight() != null
                && (statusValue = applicationStatusMap.get(target.getLeft())) != null
                && (valueType = store.getValueType()) != null
                && (value = ObjectMapperUtils.readValue(statusValue, valueType)) != null) {
            store.readStatus(target.getRight(), value);
        }
    }


}
