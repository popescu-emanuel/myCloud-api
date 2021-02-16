package fmi.unibuc.ro.mycloudapi.exception.storage;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceValidConstraintsException extends RuntimeException {
    public ResourceValidConstraintsException(String message) {
        super(message);
    }
}
