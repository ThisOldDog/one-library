package pers.dog.boot.component.event;

import javafx.application.Application;
import org.springframework.context.ApplicationEvent;

/**
 * 舞台显示监听器
 *
 * @author 废柴 2021/6/10 16:25
 */
public class ApplicationCloseEvent extends ApplicationEvent {

    public ApplicationCloseEvent(Application application) {
        super(application);
    }
}
