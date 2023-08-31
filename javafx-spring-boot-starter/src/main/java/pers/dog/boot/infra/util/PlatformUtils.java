package pers.dog.boot.infra.util;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.util.Duration;
import org.springframework.lang.NonNull;

public class PlatformUtils {
    static class ReversibleTimerTask implements Delayed {
        private final ExecutorService executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());
        private final AtomicLong time = new AtomicLong(0);
        private final AtomicReference<Runnable> runnable = new AtomicReference<>();

        ReversibleTimerTask(Duration delay, Runnable runnable) {
            reset(delay, runnable);
        }

        @SuppressWarnings("BusyWait")
        public void reset(Duration delay, Runnable runnable) {
            this.runnable.set(runnable);
            time.set((long) (System.currentTimeMillis() + delay.toMillis()));
            executorService.submit(() -> {
                long timeout;
                while ((timeout = getDelay(TimeUnit.MILLISECONDS)) > 0) {
                    try {
                        Thread.sleep(timeout);
                    } catch (Exception ignored) {
                    }
                }
                Platform.runLater(this.runnable.get());
            });
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NonNull Delayed other) {
            if (other == this) {
                return 0;
            }
            if (other instanceof ReversibleTimerTask) {
                ReversibleTimerTask x = (ReversibleTimerTask) other;
                long diff = time.get() - x.time.get();
                if (diff < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) -
                    other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }
    }

    private static final Map<String, ReversibleTimerTask> RUNNER = new ConcurrentHashMap<>();

    public static void runLater(String runnerCode, Duration delay, Runnable task) {
        RUNNER.compute(runnerCode, (key, oldValue) -> {
            if (oldValue != null) {
                oldValue.reset(delay, task);
                return oldValue;
            }
            return new ReversibleTimerTask(delay, task);
        });
    }

//    public static <T> void runLater(Runnable uiBefore, Supplier<T> task, Consumer<T> uiAfter) {
//        CompletableFuture
//                .runAsync(() -> {
//                    if (uiBefore != null) {
//                        Platform.runLater(uiBefore);
//                    }
//                })
//                .th(v -> task.get())
//                .thenRunAsync(() -> {
//                    if (uiAfter != null) {
//                        Platform.runLater(uiAfter);
//                    }
//                });
//
//    }
}
