package pers.dog.boot.component.file;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author 废柴 2021/6/17 20:04
 */
public class UserHomeDirFileOperationHandler extends AbstractFileOperationHandler {
    private static final String USER_HOME = "user.home";
    private final Path userHome;
    private final FileOperationOption.UserHomeDirOption option;

    public UserHomeDirFileOperationHandler(FileOperationOption.UserHomeDirOption option, String applicationName) {
        Assert.isTrue(StringUtils.hasText(applicationName), "If you want to use the settings store, you must configure property spring.application.name");
        String userHome = Optional.ofNullable(System.getProperty(USER_HOME)).orElse("");
        this.option = option;
        this.userHome = Path.of(userHome, option.getPathPrefix(), applicationName);
    }

    @Override
    public Path directory() {
        return userHome;
    }

    public FileOperationOption.UserHomeDirOption getOption() {
        return option;
    }
}
