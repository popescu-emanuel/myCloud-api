package fmi.unibuc.ro.mycloudapi.exception.database;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
