package pers.dog.infra.property;

import pers.dog.boot.infra.dialog.I18nProperty;

@I18nProperty(name = "info.editor.toolbar.table")
public class TableProperty {

    private int column = 2;
    private int row = 3;

    @I18nProperty(name = "info.editor.toolbar.table_column")
    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    @I18nProperty(name = "info.editor.toolbar.table_row")
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
