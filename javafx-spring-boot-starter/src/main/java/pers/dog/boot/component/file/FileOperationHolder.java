package pers.dog.boot.component.file;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.dog.boot.infra.constant.FileStoreLocation;

/**
 * 文件操作 Holder
 *
 * @author 废柴 2021/6/22 19:43
 */
public class FileOperationHolder {
    private static final Logger logger = LoggerFactory.getLogger(FileOperationHolder.class);
    private static final Map<String, FileOperationHandler> fileOperationHandlerMap = new ConcurrentHashMap<>(2);
    private static FileOperationHandler defaultHandler;

    private FileOperationHolder() {
    }

    public static void register(String handlerName, FileOperationOption option, String applicationName) {
        register(handlerName, option, applicationName, false);
    }

    public static void registerAsDefault(String handlerName, FileOperationOption option, String applicationName) {
        register(handlerName, option, applicationName, true);
    }

    private static void register(String handlerName, FileOperationOption option, String applicationName, boolean asDefault) {
        if (fileOperationHandlerMap.containsKey(handlerName)) {
            logger.warn("[File Operation] FileOperationHandler {} already exists.", handlerName);
            return;
        }
        if (option.getLocation() == null) {
            logger.error("[File Operation] FileOperationHandler {} no storage location specified.", handlerName);
            return;
        }
        FileOperationHandler fileOperationHandler;
        switch (option.getLocation()) {
            case USER_HOME_DIR:
                fileOperationHandler = new UserHomeDirFileOperationHandler(option.getUserHome(), applicationName);
                break;
            case APPLICATION_DIR:
                fileOperationHandler = new ApplicationDirFileOperationHandler(option.getFileSystem());
                break;
            default:
                throw new IllegalArgumentException("[File Operation] Unknown file operation mode : " + handlerName);
        }
        if (asDefault) {
            setDefaultHandler(fileOperationHandler);
            logger.debug("[File Operation] FileOperationHandler {} registered as default.", handlerName);
        }
        register(handlerName, fileOperationHandler);
        logger.debug("[File Operation] FileOperationHandler {} registered success.", handlerName);
    }


    public static void register(String handlerName, FileOperationHandler handler) {
        logger.info("[File Operation] Register file operation {} : {}", handlerName, handler.directory());
        fileOperationHandlerMap.put(handlerName, handler);
    }

    public static void setDefaultHandler(FileOperationHandler handler) {
        logger.info("[File Operation] Default file operation : {}", handler.directory());
        defaultHandler = handler;
    }

    public static FileOperationHandler getHandler(String handlerName) {
        return fileOperationHandlerMap.getOrDefault(handlerName, defaultHandler);
    }

    public static FileOperationHandler getHandler(FileStoreLocation handlerName) {
        if (handlerName == null) {
            return defaultHandler;
        }
        return fileOperationHandlerMap.getOrDefault(handlerName.name(), defaultHandler);
    }
}
