package pers.dog.app.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;
import pers.dog.app.service.ProjectService;
import pers.dog.boot.component.file.ApplicationDirFileOperationHandler;
import pers.dog.boot.component.file.FileOperationOption;
import pers.dog.boot.context.ApplicationContextHolder;
import pers.dog.boot.infra.util.AlertUtils;
import pers.dog.boot.infra.util.FileUtils;
import pers.dog.domain.entity.Project;
import pers.dog.infra.constant.GitRepositoryType;
import pers.dog.infra.constant.ProjectType;
import pers.dog.infra.util.FreeMarkerUtils;
import pers.dog.infra.util.MessageDigestUtils;

/**
 * @author 废柴 2023/8/30 15:32
 */
@Service
public class GitServiceImpl implements GitService {
    private static final Logger logger = LoggerFactory.getLogger(GitServiceImpl.class);

    public enum GitStep {
        CHECK,
        OPEN,
        PULL,
        COPY_TO_REPOSITORY,
        COPY_TO_LOCAL,
        PUSH
    }

    public enum GitRepositoryResult {
        FAILED,
        SUCCESS,
        INVALID
    }

    public interface GitRepositoryService {
        GitRepositoryResult test(GitSetting setting);

        GitRepositoryResult create(GitSetting setting);

        void pull(GitSetting setting, Consumer<GitStep> pullStepListener);

        void push(GitSetting setting, Consumer<GitStep> pushStepListener);
    }

    @Component
    public static class GitRepositoryFactory {
        public GitRepositoryFactory(ProjectService projectService) {
            TYPE_HANDLER.put(GitRepositoryType.GitHub, new GitHubRepositoryService(projectService));
        }

        private static final Map<GitRepositoryType, GitRepositoryService> TYPE_HANDLER = new EnumMap<>(GitRepositoryType.class);


        public static GitRepositoryService getService(GitRepositoryType gitRepositoryType) {
            if (TYPE_HANDLER.containsKey(gitRepositoryType)) {
                return TYPE_HANDLER.get(gitRepositoryType);
            }
            throw new IllegalStateException("Unsupported git repository type: " + gitRepositoryType);
        }
    }

    public static class GitHubRepositoryService implements GitRepositoryService {
        private final ProjectService projectService;

        public GitHubRepositoryService(ProjectService projectService) {
            this.projectService = projectService;
        }

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
                return GitRepositoryResult.INVALID;
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

        @Override
        public void pull(GitSetting setting, Consumer<GitStep> pullStepListener) {
            pullStepListener.accept(GitStep.CHECK);
            Path dir = check(setting);
            if (dir == null) {
                return;
            }
            pullStepListener.accept(GitStep.OPEN);
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", setting.getPrivateToken());
            try (Git git = open(dir, setting, credentialsProvider)) {
                if (git == null) {
                    return;
                }
                pullStepListener.accept(GitStep.PULL);
                if (!pull(git, credentialsProvider)) {
                    return;
                }
                pullStepListener.accept(GitStep.COPY_TO_LOCAL);
                copyToLocal(git);
            }
        }

        @Override
        public void push(GitSetting setting, Consumer<GitStep> pushStepListener) {
            pushStepListener.accept(GitStep.CHECK);
            Path dir = check(setting);
            if (dir == null) {
                return;
            }
            pushStepListener.accept(GitStep.OPEN);
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", setting.getPrivateToken());
            try (Git git = open(dir, setting, credentialsProvider)) {
                if (git == null) {
                    return;
                }
                pushStepListener.accept(GitStep.PULL);
                if (!pull(git, credentialsProvider)) {
                    return;
                }
                pushStepListener.accept(GitStep.COPY_TO_REPOSITORY);
                if (!copyToRepository(git)) {
                    return;
                }
                pushStepListener.accept(GitStep.PUSH);
                push(git, credentialsProvider);
            }
        }

        private Path check(GitSetting setting) {
            if (ObjectUtils.isEmpty(setting.getRepositoryName())
                    || ObjectUtils.isEmpty(setting.getUsername())
                    || ObjectUtils.isEmpty(setting.getPrivateToken())) {
                Platform.runLater(() -> AlertUtils.showError("error.action.git.push.no_setting.title",
                        "error.action.git.push.no_setting.header_text",
                        "error.action.git.push.no_setting.content_text"));
                return null;
            }
            if (GitRepositoryResult.FAILED.equals(test(setting))) {
                Platform.runLater(() -> AlertUtils.showError("error.action.git.push.not_exists.title",
                        "error.action.git.push.not_exists.header_text",
                        "error.action.git.push.not_exists.content_text"));
                return null;
            }
            String gitId = MessageDigestUtils.md5(
                    GitRepositoryType.GitHub + "-" + setting.getUsername() + "-" + setting.getRepositoryName() + "-" + setting.getPrivateToken()
            );
            ApplicationDirFileOperationHandler pushHandler = new ApplicationDirFileOperationHandler(new FileOperationOption.ApplicationDirOption().setPathPrefix(".data/git/" + gitId));
            if (!Files.exists(pushHandler.directory())) {
                try {
                    return Files.createDirectories(pushHandler.directory());
                } catch (IOException e) {
                    Platform.runLater(() -> AlertUtils.showException("error.action.git.push.create.title",
                            "error.action.git.push.create.header_text",
                            "error.action.git.push.create.content_text",
                            "error.action.git.push.create.exception_stacktrace",
                            e));
                    return null;
                }
            }
            return pushHandler.directory();
        }

        private Git open(Path dir, GitSetting setting, UsernamePasswordCredentialsProvider credentialsProvider) {
            Path repositoryPath = dir.resolve(setting.getRepositoryName());
            Git git = null;
            if (Files.exists(repositoryPath)) {
                try {
                    git = Git.open(repositoryPath.toFile());
                } catch (IOException e) {
                    logger.error("[Git] Unable to open local repository.", e);
                    try {
                        FileUtils.deleteDirectory(repositoryPath);
                    } catch (IOException ex) {
                        logger.error("[Git] Unable to delete local repository.", e);
                        return null;
                    }
                }
            }
            if (git == null) {

                try {
                    git = Git.cloneRepository()
                            .setDirectory(repositoryPath.toFile())
                            .setURI(String.format("https://github.com/%s/%s.git", setting.getUsername(), setting.getRepositoryName()))
                            .setCredentialsProvider(credentialsProvider)
                            .call();
                    List<Ref> branchList = git.branchList().call();
                    if (branchList.isEmpty()) {
                        logger.info("[Git] Empty repository");
                        git = Git.init().setGitDir(git.getRepository().getDirectory()).setInitialBranch("master").call();
                    }
                } catch (GitAPIException e) {
                    Platform.runLater(() -> AlertUtils.showException("error.action.git.push.clone.title",
                            "error.action.git.push.clone.header_text",
                            "error.action.git.push.clone.content_text",
                            "error.action.git.push.clone.exception_stacktrace",
                            e));
                }
            }

            return git;
        }

        private boolean pull(Git git, UsernamePasswordCredentialsProvider credentialsProvider) {
            try {
                if (git.branchList().call().isEmpty()) {
                    return true;
                }
                PullResult result = git.pull().setCredentialsProvider(credentialsProvider).call();
                if (!result.isSuccessful()) {
                    Platform.runLater(() -> AlertUtils.showError("error.action.git.push.pull.title",
                            "error.action.git.push.pull.header_text",
                            "error.action.git.push.pull.content_text"));
                    return false;
                }
                return true;
            } catch (RefNotFoundException e) {
                logger.error("Ref not found.", e);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtils.showException("error.action.git.push.pull.title",
                        "error.action.git.push.pull.header_text",
                        "error.action.git.push.pull.content_text",
                        "error.action.git.push.pull.exception_stacktrace",
                        e));
            }
            return false;
        }

        private boolean copyToRepository(Git git) {
            try {
                Path repository = git.getRepository().getDirectory().toPath().getParent();
                FileUtils.replace(projectService.documentDir(), repository.resolve("document"), FileUtils.FileReplaceOption.DELETE_IF_NOT_EXISTS);

                String template = StreamUtils.copyToString(ApplicationContextHolder.getResourceLoader().getResource("static/README.ftl").getInputStream(), StandardCharsets.UTF_8);
                String readme = FreeMarkerUtils.templateProcess(template, buildParam());

                Files.writeString(repository.resolve("README.md"), readme, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            } catch (IOException e) {
                Platform.runLater(() -> AlertUtils.showException("error.action.git.push.copy.title",
                        "error.action.git.push.copy.header_text",
                        "error.action.git.push.copy.content_text",
                        "error.action.git.push.copy.exception_stacktrace",
                        e));
                return false;
            }
        }

        private void copyToLocal(Git git) {
            try {
                Path repository = git.getRepository().getDirectory().toPath().getParent();
                FileUtils.replace(repository.resolve("document"), projectService.documentDir());
            } catch (IOException e) {
                Platform.runLater(() -> AlertUtils.showException("error.action.git.push.copy.title",
                        "error.action.git.push.copy.header_text",
                        "error.action.git.push.copy.content_text",
                        "error.action.git.push.copy.exception_stacktrace",
                        e));
            }
        }

        private void push(Git git, UsernamePasswordCredentialsProvider credentialsProvider) {
            try {
                git.add().addFilepattern(".").call();
                git.commit().setMessage(String.format("[One Library] push %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))).call();
                git.push().setCredentialsProvider(credentialsProvider).call();
            } catch (GitAPIException e) {
                Platform.runLater(() -> AlertUtils.showException("error.action.git.push.push.title",
                        "error.action.git.push.push.header_text",
                        "error.action.git.push.push.content_text",
                        "error.action.git.push.push.exception_stacktrace",
                        e));
            }
        }

        private Map<String, Object> buildParam() {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, String>> projectList = new ArrayList<>();
            List<String> singleProjectList = new ArrayList<>();
            paramMap.put("projectList", projectList);
            paramMap.put("singleProjectList", singleProjectList);
            for (TreeItem<Project> child : ProjectService.ROOT.getChildren()) {
                if (ProjectType.DIRECTORY.equals(child.getValue().getProjectType())) {
                    projectList.add(buildProjectParam(child));
                } else {
                    singleProjectList.add(String.format("- [%s](%s)", child.getValue().getSimpleProjectName(), "./document/" + urlEncode(child.getValue().getProjectName())));
                }
            }
            return paramMap;
        }

        private Map<String, String> buildProjectParam(TreeItem<Project> project) {
            StringBuilder buildDirectoryTree = new StringBuilder();
            buildDirectoryParam(0, buildDirectoryTree, project, "./document/" + urlEncode(project.getValue().getProjectName()) + "/");
            return Map.of(
                    "title", project.getValue().getSimpleProjectName(),
                    "directory", buildDirectoryTree.toString());
        }

        private String urlEncode(String value) {
            if (ObjectUtils.isEmpty(value)) {
                return "";
            }
            return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
        }

        private void buildDirectoryParam(int level, StringBuilder buildDirectoryTree, TreeItem<Project> project, String parentPath) {
            if (CollectionUtils.isEmpty(project.getChildren())) {
                return;
            }
            String prefix = " ".repeat(level << 1);
            for (TreeItem<Project> child : project.getChildren()) {
                String path = parentPath + child.getValue().getProjectName();
                if (ProjectType.DIRECTORY.equals(child.getValue().getProjectType())) {
                    buildDirectoryTree.append(prefix).append("- ").append(child.getValue().getSimpleProjectName()).append("\n");
                    buildDirectoryParam(level + 1, buildDirectoryTree, child, path + "/");
                } else {
                    buildDirectoryTree.append(prefix).append("- ").append(String.format("[%s](%s)", child.getValue().getSimpleProjectName(), path)).append("\n");
                }
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

    @Override
    public void pull(Consumer<GitStep> pullStepListener) {
        GitRepositoryFactory.getService(getGitSetting().getGitRepositoryType()).pull(getGitSetting(), pullStepListener);
    }

    @Override
    public void push(Consumer<GitStep> pushStepListener) {
        GitRepositoryFactory.getService(getGitSetting().getGitRepositoryType()).push(getGitSetting(), pushStepListener);
    }
}
