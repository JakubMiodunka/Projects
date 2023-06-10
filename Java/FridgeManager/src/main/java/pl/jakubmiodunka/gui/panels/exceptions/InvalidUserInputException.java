package pl.jakubmiodunka.gui.panels.exceptions;

/**
 * Thrown when data given by the user is invalid ex. not the right format or empty string.
 *
 * @author Jakub Miodunka
 * */
public class InvalidUserInputException extends RuntimeException {
    public InvalidUserInputException(String message) {
        super(message);
    }

    public InvalidUserInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
