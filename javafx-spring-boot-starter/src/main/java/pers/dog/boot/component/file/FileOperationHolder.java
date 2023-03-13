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

    public static FileOperationHandler register(String handlerName, FileOperationOption option, String applicationName) {
        return register(handlerName, option, applicationName, false);
    }

    public static FileOperationHandler registerAsDefault(String handlerName, FileOperationOption option, String applicationName) {
        return register(handlerName, option, applicationName, true);
    }

    private static FileOperationHandler register(String handlerName, FileOperationOption option, String applicationName, boolean asDefault) {
        if (fileOperationHandlerMap.containsKey(handlerName)) {
            logger.warn("[File Operation] FileOperationHandler {} already exists.", handlerName);
            return getHandler(handlerName);
        }
        if (option.getLocation() == null) {
            throw new IllegalArgumentException("[File Operation] FileOperationHandler " + handlerName + " no storage location specified.");
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
        return fileOperationHandler;
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
