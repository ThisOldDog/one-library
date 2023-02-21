package pers.dog.boot.component.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
 * 舞台显示监听器
 *
 * @author 废柴 2021/6/10 16:25
 */
public class ApplicationRunEvent extends ApplicationEvent {
    private final Stage stage;

    public ApplicationRunEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
