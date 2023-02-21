package pers.dog.infra.action;

import java.util.Collections;

import javafx.beans.DefaultProperty;
import pers.dog.boot.i18n.I18nMessageSource;

/**
 * @author 废柴 2022/6/4 23:58
 */
@DefaultProperty("actions")
public class ActionGroup extends org.controlsfx.control.action.ActionGroup {
    public ActionGroup() {
        super("", Collections.emptyList());
    }

    public String getI18nText() {
        return getText();
    }
    public void setI18nText(String i18nText) {
        setText(I18nMessageSource.getResource(i18nText));
    }
}
