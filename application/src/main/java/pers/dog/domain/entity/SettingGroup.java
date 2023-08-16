package pers.dog.domain.entity;

import java.util.List;
import java.util.Map;

/**
 * @author 废柴 2023/8/15 21:34
 */
public class SettingGroup {
    private String code;
    private String groupName;
    private List<SettingGroup> children;
    private String sceneName;
    private Map<String, String> options;

    public String getCode() {
        return code;
    }

    public SettingGroup setCode(String code) {
        this.code = code;
        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public SettingGroup setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public List<SettingGroup> getChildren() {
        return children;
    }

    public SettingGroup setChildren(List<SettingGroup> children) {
        this.children = children;
        return this;
    }

    public String getSceneName() {
        return sceneName;
    }

    public SettingGroup setSceneName(String sceneName) {
        this.sceneName = sceneName;
        return this;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public SettingGroup setOptions(Map<String, String> options) {
        this.options = options;
        return this;
    }
}
