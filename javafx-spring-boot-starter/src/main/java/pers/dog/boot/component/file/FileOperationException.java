package pers.dog.boot.component.file;

/**
 * @author qingsheng.chen@hand-china.com 2023/3/13 16:27
 */
public class FileOperationException extends RuntimeException {

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
