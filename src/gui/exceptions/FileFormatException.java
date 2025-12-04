package gui.exceptions;

/** Error while reading or parsing a file. */
public class FileFormatException extends AppException {
    public FileFormatException(String userMessage) { 
        super(userMessage); 
    }
    public FileFormatException(String userMessage, Throwable cause) { 
        super(userMessage, cause); 
    }
}
