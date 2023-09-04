package pers.dog.boot;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import pers.dog.boot.component.event.ApplicationCloseEvent;
import pers.dog.boot.component.event.ApplicationRunEvent;
import pers.dog.boot.component.event.SceneLoadedEvent;
import pers.dog.boot.context.ApplicationContextHolder;
import pers.dog.boot.context.property.ApplicationProperties;
import pers.dog.boot.controller.StartingController;
import pers.dog.boot.infra.util.FXMLUtils;
import pers.dog.boot.infra.util.ImageUtils;
import pers.dog.boot.infra.util.WordUtils;

/**
 * 基于 SpringBoot 的 JavaFX 开发脚手架
 * JavaFXSpringBootApplication 是应用程序的入口类，继承此类调用 {@link #run} 方法可以打开应用程序
 * <p>
 * JavaFX 与 Spring Boot 的声明周期：
 * <pre>
 * ▏ JavaFX   ▏  Spring Boot   ▏           Custom            ▏         Event          ▏       Stage       ▏
 * ▏ 1.init   ▏                ▏                             ▏                        ▏                   ▏
 * ▏          ▏                ▏          2.onCreate         ▏                        ▏                   ▏
 * ▏          ▏    3.run       ▏                             ▏                        ▏                   ▏
 * ▏          ▏                ▏    (Spring Boot Finished)   ▏                        ▏                   ▏
 * ▏          ▏                ▏     7.onError/onRunning     ▏                        ▏                   ▏
 * ▏          ▏                ▏                             ▏ 8.ApplicationRunEvent  ▏                   ▏
 * ▏          ▏                ▏                             ▏   9.SceneLoadedEvent   ▏                   ▏
 * ▏          ▏                ▏                             ▏                        ▏   10.app-scene    ▏
 * ▏ 4.start  ▏                ▏                             ▏                        ▏                   ▏
 * ▏          ▏                ▏         5.onStart           ▏                        ▏                   ▏
 * ▏          ▏                ▏                             ▏                        ▏   6.start-scene   ▏
 * </pre>
 *
 * @author 废柴
 */

public abstract class JavaFXSpringBootApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JavaFXSpringBootApplication.class);
    private static final String STARTING_SCENE = "starting";
    private static final String BANNER_NAME = "banner";
    private static final String FAVICON_NAME = "favicon";

    private final CompletableFuture<Runnable> javafxApplicationLaunch = new CompletableFuture<>();

    protected StringProperty startingStepProperty = new SimpleStringProperty();

    public static void run(Class<? extends JavaFXSpringBootApplication> applicationClass, String... args) {
        ApplicationContextHolder.setApplicationClass(applicationClass);
        ApplicationContextHolder.setArgs(args);
        Platform.setImplicitExit(false);
        launch(applicationClass, args);
    }

    protected void onCreate() {
    }

    protected Runnable onStart(Stage stage) throws IOException {
        Stage startStage = new Stage(StageStyle.UNDECORATED);
        Scene startScene = new Scene(FXMLUtils.loadFXML(STARTING_SCENE, JavaFXSpringBootApplication.class));
        StartingController controller = FXMLUtils.getController(startScene.getRoot());
        Bindings.bindBidirectional(controller.getStaringStep().textProperty(), startingStepProperty);
        loadFavicon(startStage);
        loadBanner(startScene);
        startStage.setScene(startScene);
        startStage.show();
        startingStepProperty.setValue("Starting ...");
        return startStage::close;
    }

    public void onStartScene(Stage stage) {
    }

    protected void onRunning(Stage stage) throws IOException {
        loadFavicon(stage);
        loadTitle(stage);
        ApplicationContextHolder.getContext().publishEvent(new ApplicationRunEvent(stage));
        loadStartScene(stage);
        stage.show();
    }

    protected void onError(Throwable throwable) {
        throwable.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setTitle("An error occurred when the application started!");
        alert.setContentText(ExceptionUtils.getMessage(throwable));
        alert.showAndWait().ifPresent(response -> Platform.exit());
    }

    @Override
    public void init() throws Exception {
        onCreate();
        ClassLoader classLoader = this.getClass().getClassLoader();
        CompletableFuture.supplyAsync(() -> {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    return new SpringApplicationBuilder(this.getClass()).bannerMode(Banner.Mode.OFF).run(ApplicationContextHolder.getArgs());
                })
                .whenComplete((context, throwable) -> {
                    if (throwable != null) {
                        logger.error("[Start] An error occurred when the application started.", throwable);
                        Platform.runLater(() -> onError(throwable));
                    } else {
                        ApplicationContextHolder.setContext(context);
                        ApplicationContextHolder.getStage().setMinWidth(getStageMinWidth());
                        ApplicationContextHolder.getStage().setMinHeight(getStageMinHeight());
                    }
                }).thenAcceptBothAsync(javafxApplicationLaunch, (context, closeStartStage) -> Platform.runLater(() -> {
                    try {
                        onRunning(ApplicationContextHolder.getStage());
                    } catch (IOException e) {
                        onError(e);
                    } finally {
                        closeStartStage.run();
                    }
                }));
    }

    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContextHolder.setStage(stage);
        javafxApplicationLaunch.complete(onStart(stage));
    }

    @Override
    public void stop() throws Exception {
        ApplicationContext context = ApplicationContextHolder.getContext();
        if (context != null) {
            context.publishEvent(new ApplicationCloseEvent(this));
        }
        System.exit(0);
    }

    private void loadBanner(Scene startScene) {
        ImageUtils.load(BANNER_NAME, inputStream -> {
            ImageView imageView = new ImageView(new Image(inputStream));
            imageView.setSmooth(true);
            imageView.setCache(true);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(720F);
            Parent root = startScene.getRoot();
            if (root instanceof AnchorPane) {
                AnchorPane anchorPane = (AnchorPane) root;
                anchorPane.getChildren().add(0, imageView);
                AnchorPane.setTopAnchor(imageView, 0D);
                AnchorPane.setLeftAnchor(imageView, 0D);
                AnchorPane.setRightAnchor(imageView, 0D);
                AnchorPane.setBottomAnchor(imageView, 16D);
            }
        });
    }


    protected void loadStartScene(Stage stage) {
        String startSceneName = getStartSceneName();
        Scene scene = new Scene(FXMLUtils.loadFXML(startSceneName), Double.isNaN(stage.getWidth()) ? getStartSceneWidth() : stage.getWidth(), Double.isNaN(stage.getHeight()) ? getStartSceneHeight() : stage.getHeight());
        stage.setScene(scene);
        stage.sizeToScene();
        ApplicationContextHolder.getContext().publishEvent(new SceneLoadedEvent(startSceneName, scene));
    }

    protected void loadFavicon(Stage stage) {
        ImageUtils.load(FAVICON_NAME, inputStream -> stage.getIcons().add(new Image(inputStream)));
    }

    protected void loadTitle(Stage stage) {
        String title = getStageTitle();
        if (StringUtils.hasText(title)) {
            stage.setTitle(title);
        }
    }

    protected String getStartSceneName() {
        return Optional.ofNullable(ApplicationContextHolder.getApplicationProperties().getStartScene().getName()).orElseGet(() -> ApplicationContextHolder.getContext().getEnvironment().getProperty("spring.application.name", ApplicationProperties.DEFAULT_START_SCENE));
    }

    protected double getStartSceneWidth() {
        return ApplicationContextHolder.getApplicationProperties().getStartScene().getWidth();
    }

    protected double getStartSceneHeight() {
        return ApplicationContextHolder.getApplicationProperties().getStartScene().getHeight();
    }

    protected double getStageMinWidth() {
        return ApplicationContextHolder.getApplicationProperties().getStage().getMinWidth();
    }

    protected double getStageMinHeight() {
        return ApplicationContextHolder.getApplicationProperties().getStage().getMinHeight();
    }

    protected String getStageTitle() {
        return Optional.ofNullable(ApplicationContextHolder.getApplicationProperties().getStage().getTitle()).orElseGet(() -> WordUtils.delimiterToFirstLetterCapitalized(getStartSceneName()));
    }
}
