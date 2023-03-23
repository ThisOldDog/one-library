package pers.dog.boot.component.file;

import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author 废柴 2021/6/17 20:01
 */
public interface FileOperationHandler {
    Path directory();

    Path targetFile(String filename, String... relativePath);

    Path targetPath(String... relativePath);

    default void write(String filename, Object content, String... relativePath) {
        write(WriteOption.CREATE_NEW, filename, content, relativePath);
    }

    void write(WriteOption writeOption, String filename, Object content, String... relativePath);

    List<String> readAllLines(String filename, String... relativePath);

    <T> T read(String filename, Class<T> type, String... relativePath);

    <T> T read(String filename, TypeReference<T> type, String... relativePath);

    void walkFileTree(FileVisitor<Path> fileVisitor, String... relativePath);

    boolean exists(String name, String... relativePath);

    void createDirectory(String name, String... relativePath);

    void createFile(String name, String... relativePath);

    void delete(String name, String... relativePath);

    boolean rename(String name, String newName, String[] relativePath);
}
