package pers.dog.config.status;

import javafx.scene.control.SplitPane;
import org.springframework.stereotype.Component;
import pers.dog.boot.component.cache.status.StatusStore;
import pers.dog.infra.resource.Resources;

/**
 * Window 状态存储和回复
 *
 * @author 废柴 2021/6/21 19:36
 */
@Component
public class WorkspaceStatusStore implements StatusStore<SplitPane, WorkspaceStatusStore.SplitPaneStatus> {
    public static class SplitPaneStatus {
        private double[] dividers;

        public double[] getDividers() {
            return dividers;
        }

        public SplitPaneStatus setDividers(double[] dividers) {
            this.dividers = dividers;
            return this;
        }
    }

    @Override
    public SplitPaneStatus storeStatus(SplitPane splitPane) {
        return new SplitPaneStatus()
                .setDividers(splitPane.getDividerPositions());
    }

    @Override
    public void readStatus(SplitPane splitPane, SplitPaneStatus splitPaneStatus) {
        splitPane.setDividerPositions(splitPaneStatus.dividers);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
