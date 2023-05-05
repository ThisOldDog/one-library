package pers.dog.boot.component.cache.status;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import pers.dog.boot.component.event.ApplicationCloseEvent;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.WriteOption;

/**
 * 程序状态保存监听器
 *
 * @author 废柴 2021/6/21 19:14
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class StatueSaveListener implements ApplicationListener<ApplicationCloseEvent> {
    private final StatusStoreProperties properties;
    private final FileOperationHandler fileOperationHandler;
    private final List<? extends StatusStore> statusStoreList;

    public StatueSaveListener(StatusStoreProperties properties,
                              FileOperationHandler fileOperationHandler,
                              ObjectProvider<List<? extends StatusStore>> statusStoreList) {
        this.properties = properties;
        this.statusStoreList = statusStoreList.getIfAvailable(Collections::emptyList);
        this.fileOperationHandler = fileOperationHandler;
        this.statusStoreList.sort(Comparator.comparingInt(StatusStore::getOrder));
    }

    @Override
    public void onApplicationEvent(ApplicationCloseEvent event) {
        Map<String, Object> applicationStatusMap = new HashMap<>(statusStoreList.size());
        statusStoreList.forEach(store -> save(store, applicationStatusMap));
        fileOperationHandler.write(WriteOption.CREATE_NEW, properties.getFileName(), applicationStatusMap, properties.getPath());
    }

    private void save(StatusStore store, Map<String, Object> applicationStatusMap) {
        Pair<String, Object> target = store.getTarget();
        if (target != null && target.getRight() != null) {
            Object right = target.getRight();
            if (right instanceof Collection) {
                for (Object item : ((Collection<?>) right)) {
                    applicationStatusMap.put(target.getLeft(), store.storeStatus(item));
                }
            } else {
                applicationStatusMap.put(target.getLeft(), store.storeStatus(target.getRight()));
            }
        }
    }


}
