package pers.dog.boot.component.cache.status;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.stage.Stage;
import org.springframework.beans.factory.ObjectProvider;
import pers.dog.boot.component.event.ApplicationRunEvent;
import pers.dog.boot.component.file.FileOperationHandler;

/**
 * 程序状态保存监听器
 *
 * @author 废柴 2021/6/21 19:14
 */
@SuppressWarnings("rawtypes")
public class StageStatueResumeListener extends StatueResumeListener<ApplicationRunEvent> {

    public StageStatueResumeListener(StatusStoreProperties properties,
                                     FileOperationHandler fileOperationHandler,
                                     ObjectProvider<List<? extends StatusStore>> statusStoreList) {
        super(properties, fileOperationHandler, statusStoreList.getIfAvailable(Collections::emptyList)
                .stream()
                .filter(store -> Stage.class.equals(store.getType()))
                .sorted(Comparator.comparingInt(StatusStore::getOrder))
                .collect(Collectors.toList()));
    }
}
