package pers.dog.boot.component.file;

import java.nio.file.Path;

/**
 * @author 废柴 2021/6/17 20:04
 */
public class ApplicationDirFileOperationHandler extends AbstractFileOperationHandler {
    private final Path directory;
    private final FileOperationOption.ApplicationDirOption option;

    public ApplicationDirFileOperationHandler(FileOperationOption.ApplicationDirOption option) {
        this.option = option;
        this.directory = Path.of(option.getPathPrefix()).toAbsolutePath();
    }

    @Override
    public Path directory() {
        return directory;
    }

    public FileOperationOption.ApplicationDirOption getOption() {
        return option;
    }
}
