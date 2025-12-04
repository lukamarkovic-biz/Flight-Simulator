package gui.exceptions;

/**
 * Specific validation exception indicating a duplicate entity 
 * (e.g., an airport or flight with the same code already exists).
 */
public class DuplicateEntityException extends ValidationException {

    /**
     * Constructs a DuplicateEntityException with a user-facing message.
     *
     * @param userMessage the message intended for the end-user
     */
    public DuplicateEntityException(String userMessage) {
        super(userMessage);
    }
}
