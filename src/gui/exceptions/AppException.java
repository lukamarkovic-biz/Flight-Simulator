package gui.exceptions;

/**
 * Base checked exception for user-relevant application errors.
 */
public class AppException extends Exception {
    private final String userMessage;

    /**
     * Constructs an AppException with a user-facing message.
     *
     * @param userMessage the message intended for the end-user
     */
    public AppException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    /**
     * Constructs an AppException with a user-facing message and a cause.
     *
     * @param userMessage the message intended for the end-user
     * @param cause       the underlying cause
     */
    public AppException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    /**
     * Returns the message intended for the end-user.
     */
    public String getUserMessage() {
        return userMessage;
    }
}
