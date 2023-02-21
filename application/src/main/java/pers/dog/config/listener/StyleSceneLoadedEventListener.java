package pers.dog.config.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pers.dog.boot.component.event.SceneLoadedEvent;
import pers.dog.infra.resource.Resources;

/**
 * 样式监听器
 *
 * @author 废柴 2021/6/10 16:41
 */
@Component
public class StyleSceneLoadedEventListener implements ApplicationListener<SceneLoadedEvent> {

    @Override
    public void onApplicationEvent(SceneLoadedEvent event) {
        event.getScene().getStylesheets().add(Resources.CSS.BASE);
        event.getScene().getStylesheets().add(Resources.CSS.LIGHT);
    }
}
