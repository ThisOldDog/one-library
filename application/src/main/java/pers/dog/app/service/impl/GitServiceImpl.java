package pers.dog.app.service.impl;

import org.springframework.stereotype.Service;
import pers.dog.api.dto.GitSetting;
import pers.dog.app.service.GitService;

/**
 * @author 废柴 2023/8/30 15:32
 */
@Service
public class GitServiceImpl implements GitService {
    private static final String SETTING_FILE_NAME = "git-setting.json";

    @Override
    public void save(GitSetting setting) {

    }

    @Override
    public void test(GitSetting setting) {

    }
}
