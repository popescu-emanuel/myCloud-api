package fmi.unibuc.ro.mycloudapi.exception.database;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String message) {
        super(message);
    }
}
