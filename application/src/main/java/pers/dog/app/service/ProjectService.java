package pers.dog.app.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.dog.app.domain.FileStatement;
import pers.dog.boot.component.file.FileOperationHandler;
import pers.dog.boot.component.file.FileOperationHolder;
import pers.dog.boot.infra.constant.FileStoreLocation;
import pers.dog.boot.context.ApplicationContextHolder;

/**
 * @author 废柴 2022/8/19 15:59
 */
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private static final String[] PROJECT_RELATIVE_PATH = {".data", "project"};

    private static volatile ProjectService instance;
    private final List<FileStatement> fileList = new ArrayList<>();

    private ProjectService() {

    }

    public static ProjectService getInstance() {
        if (instance == null) {
            synchronized (ProjectService.class) {
                if (instance == null) {
                    instance = new ProjectService();
                }
            }
        }
        return instance;
    }

    public void init(Consumer<List<FileStatement>> callback) {
//        Path base = fileOperationHandler.targetPath(PROJECT_RELATIVE_PATH);
//        fileOperationHandler.walkFileTree(
//                new SimpleFileVisitor<>() {
//                    @Override
//                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                        logger.info("Visit : {}", base.relativize(file));
//                        return super.visitFile(file, attrs);
//                    }
//                },
//                PROJECT_RELATIVE_PATH);
//        callback.accept(fileList);
    }

    public List<FileStatement> getFileList() {
        return fileList;
    }
}
