package gui.exceptions;

import java.io.IOException;

/** Wraps IOException for easier propagation to the UI. */
public class IoExceptionWrapper extends AppException {
    public IoExceptionWrapper(String userMessage, IOException cause) {
        super(userMessage, cause);
    }
}
