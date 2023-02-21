package pers.dog.boot.component.file;

import java.io.File;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import pers.dog.boot.infra.constant.FileStoreLocation;

/**
 * 文件操作选项
 *
 * @author 废柴 2021/6/17 20:22
 */
public class FileOperationOption {
    public static class UserHomeDirOption {
        private String pathPrefix = "AppData" + File.separator + "Local";

        public String getPathPrefix() {
            return pathPrefix;
        }

        public UserHomeDirOption setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
            return this;
        }
    }

    public static class ApplicationDirOption {
        private String pathPrefix = "";

        public String getPathPrefix() {
            return pathPrefix;
        }

        public ApplicationDirOption setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
            return this;
        }
    }

    private FileStoreLocation location = FileStoreLocation.APPLICATION_DIR;
    @NestedConfigurationProperty
    private UserHomeDirOption userHome = new UserHomeDirOption();
    @NestedConfigurationProperty
    private ApplicationDirOption fileSystem = new ApplicationDirOption();

    public FileStoreLocation getLocation() {
        return location;
    }

    public FileOperationOption setLocation(FileStoreLocation location) {
        this.location = location;
        return this;
    }

    public UserHomeDirOption getUserHome() {
        return userHome;
    }

    public FileOperationOption setUserHome(UserHomeDirOption userHome) {
        this.userHome = userHome;
        return this;
    }

    public ApplicationDirOption getFileSystem() {
        return fileSystem;
    }

    public FileOperationOption setFileSystem(ApplicationDirOption fileSystem) {
        this.fileSystem = fileSystem;
        return this;
    }
}
