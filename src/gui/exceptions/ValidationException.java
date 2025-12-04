package gui.exceptions;

/** Data validation error (user can correct it). */
public class ValidationException extends AppException {
    public ValidationException(String userMessage) {
        super(userMessage);
    }

    public ValidationException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }
}
