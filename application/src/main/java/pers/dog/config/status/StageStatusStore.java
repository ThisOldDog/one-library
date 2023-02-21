package pers.dog.config.status;

import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import pers.dog.boot.component.cache.status.StatusStore;

/**
 * Window 状态存储和回复
 *
 * @author 废柴 2021/6/21 19:36
 */
@Component
public class StageStatusStore implements StatusStore<Stage, StageStatusStore.StageStatus> {

    public static class StageStatus {
        private boolean maximized;
        private double height;
        private double width;
        private double x;
        private double y;

        public boolean isMaximized() {
            return maximized;
        }

        public StageStatus setMaximized(boolean maximized) {
            this.maximized = maximized;
            return this;
        }

        public double getHeight() {
            return height;
        }

        public StageStatus setHeight(double height) {
            this.height = height;
            return this;
        }

        public double getWidth() {
            return width;
        }

        public StageStatus setWidth(double width) {
            this.width = width;
            return this;
        }

        public double getX() {
            return x;
        }

        public StageStatus setX(double x) {
            this.x = x;
            return this;
        }

        public double getY() {
            return y;
        }

        public StageStatus setY(double y) {
            this.y = y;
            return this;
        }
    }

    @Override
    public StageStatus storeStatus(Stage stage) {
        StageStatus stageStatus = new StageStatus()
                .setMaximized(stage.isMaximized());
        if (stage.getScene() != null) {
            stageStatus.setHeight(stage.getScene().getHeight())
                    .setWidth(stage.getScene().getWidth());
        } else {
            stageStatus.setHeight(stage.getHeight())
                    .setWidth(stage.getWidth());
        }
        return stageStatus.setX(stage.getX())
                .setY(stage.getY());
    }

    @Override
    public void readStatus(Stage stage, StageStatus stageStatus) {
        stage.setMaximized(stageStatus.isMaximized());
        stage.setHeight(stageStatus.getHeight());
        stage.setWidth(stageStatus.getWidth());
        stage.setX(stageStatus.getX());
        stage.setY(stageStatus.getY());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
