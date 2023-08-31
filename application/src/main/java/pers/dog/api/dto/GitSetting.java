package pers.dog.api.dto;

import pers.dog.infra.constant.GitRepositoryType;

/**
 * @author 废柴 2023/8/30 15:37
 */
public class GitSetting {
    private GitRepositoryType gitRepositoryType = GitRepositoryType.GitHub;
    private String gitRepository = "https://github.com";
    private String repositoryName = "one-library";
    private String username;
    private String privateToken;

    public GitRepositoryType getGitRepositoryType() {
        return gitRepositoryType;
    }

    public GitSetting setGitRepositoryType(GitRepositoryType gitRepositoryType) {
        this.gitRepositoryType = gitRepositoryType;
        return this;
    }

    public String getGitRepository() {
        return gitRepository;
    }

    public GitSetting setGitRepository(String gitRepository) {
        this.gitRepository = gitRepository;
        return this;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public GitSetting setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public GitSetting setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public GitSetting setPrivateToken(String privateToken) {
        this.privateToken = privateToken;
        return this;
    }
}
