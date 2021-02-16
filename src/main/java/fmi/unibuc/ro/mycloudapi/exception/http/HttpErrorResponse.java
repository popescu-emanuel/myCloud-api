package fmi.unibuc.ro.mycloudapi.exception.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class HttpErrorResponse extends RuntimeException {
    private String timestamp;
    private String status;
    private String error;
    private String message;
    private String path;
}
