package pers.dog.infra.property;

import pers.dog.boot.infra.dialog.I18nProperty;

@I18nProperty(name = "info.editor.toolbar.header")
public class HeaderProperty {
    public enum Level {
        H1(1),
        H2(2),
        H3(3),
        H4(4),
        H5(5),
        H6(6);
        private final int level;

        Level(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private Level level = Level.H1;


    public void setLevel(Level level) {
        this.level = level;
    }

    @I18nProperty(name = "info.property.head.level")
    public Level getLevel() {
        return level;
    }
}
