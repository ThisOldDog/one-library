package pers.dog.infra.control;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

public class ActionSeparator extends Action {
    public static Action getInstance() {
        return ActionUtils.ACTION_SEPARATOR;
    }

    public ActionSeparator() {
        super(null, null);
    }

    @Override
    public String toString() {
        return "Separator";
    }
}
