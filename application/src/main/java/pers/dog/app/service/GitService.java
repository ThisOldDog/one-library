package pers.dog.app.service;

import java.util.function.Consumer;

import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.impl.GitServiceImpl;

/**
 * @author 废柴 2023/8/30 15:32
 */
public interface GitService {
    void save(GitSetting setting);

    GitServiceImpl.GitRepositoryResult test(GitSetting setting);

    GitServiceImpl.GitRepositoryResult create(GitSetting setting);

    GitSetting getGitSetting();

    void pull(Consumer<GitServiceImpl.GitStep> pullStepListener);

    void push(Consumer<GitServiceImpl.GitStep> pushStepListener);
}
