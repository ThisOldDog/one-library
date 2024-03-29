package pers.dog.boot.component.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import pers.dog.boot.infra.util.FileUtils;
import pers.dog.boot.infra.util.ValueConverterUtils;

/**
 * @author 废柴 2021/6/17 20:04
 */
@SuppressWarnings("DuplicatedCode")
public abstract class AbstractFileOperationHandler implements FileOperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFileOperationHandler.class);

    private boolean withType;

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
    public void write(WriteOption writeOption, String filename, byte[] content, String... relativePath) {
        Path target = targetFile(filename, relativePath);
        try {
            Path directory = target.getParent();
            if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
                logger.info("[File Operation] Target directory doesn't exists {}, try to create.", target);
                Files.createDirectories(directory);
            }
            logger.info("[File Operation] Write to file {}", target);
            Files.write(target, content, writeOption.toOpenOption());
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to write setting to %s", target);
            logger.error(exceptionMessage, e);
            throw new FileOperationException("Unable to write file: " + target.toAbsolutePath());
        }
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
            if (content instanceof InputStream) {
                Files.write(target, StreamUtils.copyToByteArray((InputStream) content), writeOption.toOpenOption());
            } else {
                Files.writeString(target, ValueConverterUtils.write(content, withType), writeOption.toOpenOption());
            }
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to write setting to %s", target);
            logger.error(exceptionMessage, e);
            throw new FileOperationException("Unable to write file: " + target.toAbsolutePath());
        }
    }

    @Override
    public List<String> readAllLines(String filename, String... relativePath) {
        Path target = targetFile(filename, relativePath);
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            try {
                return Files.readAllLines(target);
            } catch (IOException e) {
                String exceptionMessage = String.format("[File Operation] Unable to read all line from %s", target);
                logger.error(exceptionMessage, e);
                throw new FileOperationException("Unable to read file: " + target.toAbsolutePath());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public byte[] read(String filename, String... relativePath) {
        Path target = targetFile(filename, relativePath);
        try {
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                logger.info("[File Operation] File not exists {}", target);
                return new byte[]{};
            }
            logger.info("[File Operation] Read from file {}", target);
            return Files.readAllBytes(target);
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to read setting from %s", target);
            logger.error(exceptionMessage, e);
            throw new FileOperationException("Unable to read file: " + target.toAbsolutePath());
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
            return ValueConverterUtils.read(Files.readString(target), type, withType);
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to read setting from %s", target);
            logger.error(exceptionMessage, e);
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
            return ValueConverterUtils.read(Files.readString(target), type, withType);
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to read setting from %s", target);
            logger.error(exceptionMessage, e);
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
            String exceptionMessage = String.format("[File Operation] Unable to walk file tree %s", target);
            logger.error(exceptionMessage, e);
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
            String exceptionMessage = String.format("[File Operation] Unable to create directory %s", target);
            logger.error(exceptionMessage, e);
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
            String exceptionMessage = String.format("[File Operation] Unable to create file %s", target);
            logger.error(exceptionMessage, e);
            throw new FileOperationException("Unable to create file: " + target.toAbsolutePath());
        }
    }

    @Override
    public void delete(String name, String... relativePath) {
        Path target = targetFile(name, relativePath);
        try {
            delete(target.toFile());
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to delete file %s", target);
            logger.error(exceptionMessage, e);
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

    @Override
    public boolean rename(String name, String newName, String[] relativePath) {
        Path oldFile = targetFile(name, relativePath);
        Path newFile = targetFile(newName, relativePath);
        if (Files.exists(oldFile, LinkOption.NOFOLLOW_LINKS) && !Files.exists(newFile, LinkOption.NOFOLLOW_LINKS)) {
            return oldFile.toFile().renameTo(newFile.toFile());
        }
        return false;
    }

    @Override
    public void move(String name, String[] sourcePath, String[] targetPath) {
        Path target = targetFile(name, targetPath);
        move(name, sourcePath, target);
    }

    @Override
    public void move(String name, String[] sourcePath, Path targetPath) {
        Path source = targetFile(name, sourcePath);
        try {
            FileUtils.replace(source, targetPath);
            FileUtils.delete(source);
        } catch (IOException e) {
            String exceptionMessage = String.format("[File Operation] Unable to move file from %s to %s", source, targetPath);
            logger.error(exceptionMessage, e);
            throw new FileOperationException(exceptionMessage);
        }
    }

    public boolean isWithType() {
        return withType;
    }

    public AbstractFileOperationHandler setWithType(boolean withType) {
        this.withType = withType;
        return this;
    }
}
