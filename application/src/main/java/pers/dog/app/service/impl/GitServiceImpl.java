package pers.dog.app.service.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.infra.util.AlertUtils;
import pers.dog.boot.infra.util.PlatformUtils;
import pers.dog.infra.constant.GitRepositoryType;

/**
 * @author 废柴 2023/8/30 15:32
 */
@Service
public class GitServiceImpl implements GitService {
    public interface GitRepositoryService {
        boolean test(GitSetting gitSetting);
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
        public boolean test(GitSetting gitSetting) {
            try {
                GitHub gitHub = new GitHubBuilder()
                        .withOAuthToken(gitSetting.getPrivateToken())
                        .build();
                return gitHub.getRepository(gitSetting.getUsername() + "/" + gitSetting.getRepositoryName()) != null;
            } catch (GHFileNotFoundException e) {
                return false;
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtils.showException("info.action.git.setting.test.error.title",
                                "info.action.git.setting.test.header_text",
                                "info.action.git.setting.test.error.content_text",
                                "info.action.git.setting.test.error.exception_stacktrace",
                                e));
                return false;
            }
        }
    }

    private static final String SETTING_FILE_NAME = "git-setting.json";
    private final ApplicationDirFileOperationHandler handler;
    private final ApplicationDirFileOperationHandler testHandler;
    private final GitSetting gitSetting;

    public GitServiceImpl() {
        handler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/conf"));
        testHandler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/tmp"));
        gitSetting = Optional.ofNullable(handler.read(SETTING_FILE_NAME, GitSetting.class))
                .orElseGet(GitSetting::new);
    }

    @Override
    public void save(GitSetting setting) {
        gitSetting.setGitRepository(setting.getGitRepository())
                .setUsername(setting.getUsername())
                .setPrivateToken(setting.getPrivateToken());
        handler.write(SETTING_FILE_NAME, gitSetting);
    }

    @Override
    public boolean test(GitSetting setting) {
        return GitRepositoryFactory.getService(setting.getGitRepositoryType()).test(setting);
    }

    public GitSetting getGitSetting() {
        return gitSetting;
    }
}
