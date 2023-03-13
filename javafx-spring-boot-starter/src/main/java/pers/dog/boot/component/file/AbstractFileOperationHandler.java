package pers.dog.boot.component.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.dog.boot.infra.util.ValueConverterUtils;

/**
 * @author 废柴 2021/6/17 20:04
 */
@SuppressWarnings("DuplicatedCode")
public abstract class AbstractFileOperationHandler implements FileOperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFileOperationHandler.class);

    public Path targetFile(String filename, String... relativePath) {
        return targetPath(relativePath).resolve(filename);
    }

    public Path targetPath(String... relativePath) {
        Path target = directory();
        if (relativePath != null) {
            for (String path : relativePath) {
                target = target.resolve(path);
            }
        }
        return target;
    }

    @Override
    public void write(WriteOption writeOption, String filename, Object content, String... relativePath) {
        Path target = targetFile(filename, relativePath);
        try {
            Path directory = target.getParent();
            if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
                logger.info("[File Operation] Target directory doesn't exists {}, try to create.", target);
                Files.createDirectories(directory);
            }
            logger.info("[File Operation] Write to file {}", target);
            Files.writeString(target, ValueConverterUtils.write(content), writeOption.toOpenOption());
        } catch (IOException e) {
            logger.error("[File Operation] Unable to write setting to {}", target, e);
            throw new FileOperationException("Unable to write file: " + target.toAbsolutePath());
        }
    }

    @Override
    public <T> T read(String filename, Class<T> type, String... relativePath) {
        Path target = targetFile(filename, relativePath);
        try {
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                logger.info("[File Operation] File not exists {}", target);
                return null;
            }
            logger.info("[File Operation] Read from file {}", target);
            return ValueConverterUtils.read(Files.readString(target), type);
        } catch (IOException e) {
            logger.error("[File Operation] Unable to read setting from {}", target, e);
            throw new FileOperationException("Unable to read file: " + target.toAbsolutePath());
        }
    }

    @Override
    public <T> T read(String filename, TypeReference<T> type, String... relativePath) {
        Path target = targetFile(filename, relativePath);
        try {
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                logger.info("[File Operation] File not exists {}", target);
                return null;
            }
            logger.info("[File Operation] Read from file {}", target);
            return ValueConverterUtils.read(Files.readString(target), type);
        } catch (IOException e) {
            logger.error("[File Operation] Unable to read setting from {}", target, e);
            throw new FileOperationException("Unable to read file: " + target.toAbsolutePath());
        }
    }

    @Override
    public void walkFileTree(FileVisitor<Path> fileVisitor, String... relativePath) {
        Path target = targetPath(relativePath);
        try {
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                logger.info("[File Operation] Directory not exists {}", target);
                return;
            }
            Files.walkFileTree(target, fileVisitor);
        } catch (IOException e) {
            logger.error("[File Operation] Unable to walk file tree {}", target, e);
            throw new FileOperationException("Unable to walk file tree: " + target.toAbsolutePath());
        }
    }

    @Override
    public boolean exists(String name, String... relativePath) {
        Path target = targetFile(name, relativePath);
        return Files.exists(target, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public void createDirectory(String name, String... relativePath) {
        if (exists(name, relativePath)) {
            return;
        }
        Path target = targetFile(name, relativePath);
        try {
            if (!Files.exists(target.getParent(), LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(target.getParent());
            }
            Files.createDirectory(target);
        } catch (IOException e) {
            logger.error("[File Operation] Unable to create directory {}", target, e);
            throw new FileOperationException("Unable to create directory: " + target.toAbsolutePath());
        }
    }

    @Override
    public void createFile(String name, String... relativePath) {
        if (exists(name, relativePath)) {
            return;
        }
        Path target = targetFile(name, relativePath);
        try {
            if (!Files.exists(target.getParent(), LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(target.getParent());
            }
            Files.createFile(target);
        } catch (IOException e) {
            logger.error("[File Operation] Unable to create file {}", target, e);
            throw new FileOperationException("Unable to create file: " + target.toAbsolutePath());
        }
    }

    @Override
    public void delete(String name, String... relativePath) {
        Path target = targetFile(name, relativePath);
        try {
            delete(target.toFile());
        } catch (IOException e) {
            logger.error("[File Operation] Unable to delete file {}", target, e);
            throw new FileOperationException("Unable to delete file: " + target.toAbsolutePath());
        }
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    delete(child);
                }
            }
        }
        Files.delete(file.toPath());
    }
}
