package pers.dog.boot.infra.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

/**
 * @author 废柴 2023/9/4 14:10
 */
public class FileUtils {


    public enum FileReplaceOption {
        DELETE_IF_NOT_EXISTS;
    }
    private FileUtils() {
    }

    public static void delete(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            deleteDirectory(source);
        } else {
            Files.deleteIfExists(source);
        }
    }

    public static void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    public static void replace(Path source, Path target, FileReplaceOption... fileReplaceOptions) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        if (!Files.exists(target)) {
            Files.createDirectories(target);
        }
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                } else if (!Files.isDirectory(targetDir)) {
                    Files.delete(targetDir);
                    Files.createDirectories(targetDir);
                }
                return super.preVisitDirectory(dir, attrs);
            }
        });
        if (fileReplaceOptions != null && Set.of(fileReplaceOptions).contains(FileReplaceOption.DELETE_IF_NOT_EXISTS)) {
            Files.walkFileTree(target, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path sourceFile = source.resolve(target.relativize(file));
                    if (!Files.exists(sourceFile)) {
                        Files.delete(file);
                    }
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path sourceDir = source.resolve(target.relativize(dir));
                    if (!Files.exists(sourceDir)) {
                        deleteDirectory(dir);
                    }
                    return super.preVisitDirectory(dir, attrs);
                }
            });
        }
    }
}
