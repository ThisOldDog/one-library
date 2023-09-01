package pers.dog.app.service.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.util.AlertUtils;
import pers.dog.infra.constant.GitRepositoryType;

/**
 * @author 废柴 2023/8/30 15:32
 */
@Service
public class GitServiceImpl implements GitService {
    public enum GitRepositoryResult {
        FAILED,
        SUCCESS,
        INVALID
    }

    public interface GitRepositoryService {
        GitRepositoryResult test(GitSetting setting);

        GitRepositoryResult create(GitSetting setting);
    }

    public static class GitRepositoryFactory {
        private GitRepositoryFactory() {
        }

        private static final Map<GitRepositoryType, GitRepositoryService> TYPE_HANDLER = new EnumMap<>(GitRepositoryType.class);

        static {
            TYPE_HANDLER.put(GitRepositoryType.GitHub, new GitHubRepositoryService());
        }

        public static GitRepositoryService getService(GitRepositoryType gitRepositoryType) {
            if (TYPE_HANDLER.containsKey(gitRepositoryType)) {
                return TYPE_HANDLER.get(gitRepositoryType);
            }
            throw new IllegalStateException("Unsupported git repository type: " + gitRepositoryType);
        }
    }

    public static class GitHubRepositoryService implements GitRepositoryService {

        @Override
        public GitRepositoryResult test(GitSetting setting) {
            try {
                return new GitHubBuilder()
                        .withOAuthToken(setting.getPrivateToken())
                        .build()
                        .getRepository(setting.getUsername() + "/" + setting.getRepositoryName()) != null
                        ? GitRepositoryResult.SUCCESS
                        : GitRepositoryResult.FAILED;
            } catch (GHFileNotFoundException e) {
                return GitRepositoryResult.FAILED;
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtils.showException("error.action.git.setting.test.title",
                                "error.action.git.setting.test.header_text",
                                "error.action.git.setting.test.content_text",
                                "error.action.git.setting.test.exception_stacktrace",
                                e));
                return GitRepositoryResult.FAILED;
            }
        }

        @Override
        public GitRepositoryResult create(GitSetting setting) {
            try {
                return new GitHubBuilder()
                        .withOAuthToken(setting.getPrivateToken())
                        .build()
                        .createRepository(setting.getRepositoryName())
                        .owner(setting.getUsername())
                        .create() != null
                        ? GitRepositoryResult.SUCCESS
                        : GitRepositoryResult.FAILED;
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("name already exists on this account")) {
                    Platform.runLater(() -> AlertUtils.showWarning("warning.action.git.setting.create.exists.title",
                            "warning.action.git.setting.create.exists.header_text",
                            "warning.action.git.setting.create.exists.content_text"));
                    return GitRepositoryResult.INVALID;
                } else {
                    Platform.runLater(() -> AlertUtils.showException("error.action.git.setting.create.title",
                            "error.action.git.setting.create.header_text",
                            "error.action.git.setting.create.content_text",
                            "error.action.git.setting.create.exception_stacktrace",
                            e));
                }
                return GitRepositoryResult.FAILED;
            }
        }
    }

    private static final String SETTING_FILE_NAME = "git-setting.json";
    private final ApplicationDirFileOperationHandler handler;
    private final GitSetting gitSetting;

    public GitServiceImpl() {
        handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/conf"));
        gitSetting = Optional.ofNullable(handler.read(SETTING_FILE_NAME, GitSetting.class))
                .orElseGet(GitSetting::new);
    }

    @Override
    public void save(GitSetting setting) {
        BeanUtils.copyProperties(setting, gitSetting);
        handler.write(SETTING_FILE_NAME, gitSetting);
    }

    @Override
    public GitRepositoryResult test(GitSetting setting) {
        return GitRepositoryFactory.getService(setting.getGitRepositoryType()).test(setting);
    }

    @Override
    public GitRepositoryResult create(GitSetting setting) {
        return GitRepositoryFactory.getService(setting.getGitRepositoryType()).create(setting);
    }

    public GitSetting getGitSetting() {
        return gitSetting;
    }
}
