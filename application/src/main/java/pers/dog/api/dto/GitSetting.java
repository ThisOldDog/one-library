package pers.dog.api.dto;

/**
 * @author qingsheng.chen@hand-china.com 2023/8/30 15:37
 */
public class GitSetting {
    private String gitRepository;
    private String username;
    private String privateToken;

    public String getGitRepository() {
        return gitRepository;
    }

    public GitSetting setGitRepository(String gitRepository) {
        this.gitRepository = gitRepository;
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
