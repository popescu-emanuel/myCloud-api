package fmi.unibuc.ro.mycloudapi.exception.encryption;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class CryptoException extends Exception {
    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
