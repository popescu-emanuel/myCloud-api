package fmi.unibuc.ro.mycloudapi.exception.storage;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Exception cause) {
        super(message, cause);
    }
}
