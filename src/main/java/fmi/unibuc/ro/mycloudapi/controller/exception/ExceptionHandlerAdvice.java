package fmi.unibuc.ro.mycloudapi.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice  {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<String> handleException(FileNotFoundException e) {
        log.warn("File does not exist in user storage");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("File does not exist in your storage");
    }
}
