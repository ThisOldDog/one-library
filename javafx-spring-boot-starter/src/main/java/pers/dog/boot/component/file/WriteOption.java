package pers.dog.boot.component.file;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

/**
 * @author 废柴 2021/6/17 20:02
 */
public enum WriteOption {
    APPEND(StandardOpenOption.APPEND, StandardOpenOption.CREATE),
    CREATE_NEW(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

    private OpenOption[] openOption;

    WriteOption(OpenOption... openOption) {
        this.openOption = openOption;
    }

    public OpenOption[] toOpenOption() {
        return openOption;
    }
}
