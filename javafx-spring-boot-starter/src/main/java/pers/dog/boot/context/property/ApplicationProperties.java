package pers.dog.boot.context.property;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import pers.dog.boot.component.cache.status.StatusStoreProperties;
import pers.dog.boot.component.setting.SettingGroup;

/**
 * 应用属性
 *
 * @author 废柴 2021/5/27 19:27
 */
@ConfigurationProperties(prefix = "javafx.application")
public class ApplicationProperties {
    public static final String DEFAULT_START_SCENE = "primary-controller";

    @NestedConfigurationProperty
    private StageProperties stage = new StageProperties()
            .setMinHeight(520.0)
            .setMinWidth(320.0);

    /**
     * 启动场景设置
     * 如果名称不设置，默认取 {@code ${spring.application.name}} 作为启动场景名称
     * 如果应用名称也没有设置，默认取 {@code primary-controller}
     */
    @NestedConfigurationProperty
    private SceneProperties startScene = new SceneProperties()
            .setHeight(800.0)
            .setWidth(1200.0);

    @NestedConfigurationProperty
    private StatusStoreProperties status = new StatusStoreProperties();

    @NestedConfigurationProperty
    private List<SettingGroup> setting;


    public StageProperties getStage() {
        return stage;
    }

    public ApplicationProperties setStage(StageProperties stage) {
        this.stage = stage;
        return this;
    }

    public SceneProperties getStartScene() {
        return startScene;
    }

    public ApplicationProperties setStartScene(SceneProperties startScene) {
        this.startScene = startScene;
        return this;
    }

    public StatusStoreProperties getStatus() {
        return status;
    }

    public ApplicationProperties setStatus(StatusStoreProperties status) {
        this.status = status;
        return this;
    }

    public List<SettingGroup> getSetting() {
        return setting;
    }

    public ApplicationProperties setSetting(List<SettingGroup> setting) {
        this.setting = setting;
        return this;
    }
}
