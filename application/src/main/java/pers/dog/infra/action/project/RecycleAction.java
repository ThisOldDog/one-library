package pers.dog.infra.action.project;

import org.springframework.stereotype.Component;
import pers.dog.api.controller.tool.RecycleController;
import pers.dog.boot.component.control.DialogAction;

/**
 * @author 废柴 2023/9/22 10:15
 */
@Component
public class RecycleAction extends DialogAction<RecycleController> {

    public RecycleAction() {
        super("info.project.recycle");
    }


    @Override
    public String getView() {
        return "tool/recycle";
    }
}
