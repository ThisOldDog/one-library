package pers.dog.infra.action.application;

import javafx.application.Platform;
import org.controlsfx.control.action.Action;
import pers.dog.boot.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/2 22:40
 */
public class ExitAction extends Action {
    private static final ExitAction INSTANCE = new ExitAction();

    public static ExitAction getInstance() {
        return INSTANCE;
    }

    private ExitAction() {
        super(I18nMessageSource.getResource("info.action.exit"));
        super.setEventHandler(actionEvent -> Platform.exit());
    }
}
