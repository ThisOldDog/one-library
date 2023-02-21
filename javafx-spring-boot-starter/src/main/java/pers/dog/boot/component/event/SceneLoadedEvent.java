package pers.dog.boot.component.event;

import javafx.scene.Scene;
import org.springframework.context.ApplicationEvent;

/**
 * 场景加载监听器
 *
 * @author 废柴 2021/6/10 16:25
 */
public class SceneLoadedEvent extends ApplicationEvent {
    private final String sceneName;
    private final Scene scene;

    public SceneLoadedEvent(String sceneName, Scene scene) {
        super(scene);
        this.sceneName = sceneName;
        this.scene = scene;
    }

    public String getSceneName() {
        return sceneName;
    }

    public Scene getScene() {
        return scene;
    }
}
