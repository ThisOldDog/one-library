package pers.dog.app.service;

import pers.dog.api.dto.GitSetting;

/**
 * @author 废柴 2023/8/30 15:32
 */
public interface GitService {
    void save(GitSetting setting);

    void test(GitSetting setting);
}
