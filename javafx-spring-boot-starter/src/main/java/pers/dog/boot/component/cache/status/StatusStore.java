package pers.dog.boot.component.cache.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.PriorityOrdered;
import pers.dog.boot.context.ApplicationContextHolder;
import pers.dog.boot.infra.util.ReflectUtils;

/**
 * 应用程序状态管理
 *
 * @author 废柴 2021/6/21 19:28
 */
public interface StatusStore<T, S> extends PriorityOrdered {
    Logger logger = LoggerFactory.getLogger(StatusStore.class);
    String FIND_BY_TYPE = "#_FIND_BY_TYPE";

    default String getFxId() {
        return FIND_BY_TYPE;
    }

    S storeStatus(T t);

    void readStatus(T t, S s);

    default Pair<String, Object> getTarget() {
        if (StatusStore.FIND_BY_TYPE.equals(getFxId())) {
            Class<?> type = getType();
            if (type == null) {
                return null;
            } else {
                String storeName = type.getName();
                String typeName = type.getName();
                if (Stage.class.getName().equals(typeName)) {
                    return Pair.of(storeName, ApplicationContextHolder.getStage());
                } else if (Scene.class.getName().equals(typeName)) {
                    return Pair.of(storeName, Optional.ofNullable(ApplicationContextHolder.getStage())
                            .map(Stage::getScene)
                            .orElse(null));
                } else if (Window.class.getName().equals(typeName)) {
                    return Pair.of(storeName, Optional.ofNullable(ApplicationContextHolder.getStage())
                            .map(Stage::getScene)
                            .map(Scene::getWindow)
                            .orElse(null));
                } else {
                    return Pair.of(storeName, getTargetByType(type));
                }
            }
        } else {
            return Pair.of(getFxId(), getTargetByFxId());
        }
    }

    default Object getTargetByFxId() {
        Stage stage = ApplicationContextHolder.getStage();
        if (stage == null) {
            return null;
        }
        Scene scene = stage.getScene();
        if (scene == null) {
            return null;
        }
        Node node = scene.lookup("#" + getFxId());
        if (node == null) {
            logger.error("[Status] The control declared in the class {} was not found: {}", getClass(), getFxId());
        }
        return node;
    }

    default Object getTargetByType(Class<?> type) {
        Stage stage = ApplicationContextHolder.getStage();
        if (stage == null) {
            return null;
        }
        Scene scene = stage.getScene();
        if (scene == null) {
            return null;
        }
        List<Object> targetList = new ArrayList<>();
        if (scene.getRoot() != null) {
            getTargetByType(scene.getRoot(), type, targetList);
            if (targetList.size() > 1) {
                logger.error("[Status] Found more than one control type declared in the {} class: {}", getClass(), type);
            } else if (targetList.size() == 0) {
                logger.error("[Status] The control type declared in the {} class was not found: {}", getClass(), type);
            } else {
                return targetList.get(0);
            }
        }
        return targetList;
    }

    default void getTargetByType(Parent node, Class<?> type, List<Object> targetList) {
        if (node == null) {
            return;
        }
        if (node.getClass().getName().equals(type.getName())) {
            targetList.add(node);
        }
        node.getChildrenUnmodifiable().forEach(child -> {
            if (child instanceof Parent) {
                getTargetByType((Parent) child, type, targetList);
            }
        });
    }

    default Class<?> getType() {
        return ReflectUtils.getClassGenericType(getClass(), 0);
    }

    default Class<?> getValueType() {
        return ReflectUtils.getClassGenericType(getClass(), 1);
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
