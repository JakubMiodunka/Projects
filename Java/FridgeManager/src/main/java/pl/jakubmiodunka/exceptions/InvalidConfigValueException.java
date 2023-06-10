package pl.jakubmiodunka.exceptions;

/**
 * Exception thrown, when it will be detected that given configuration value is invalid.
 *
 * @author Jakub Miodunka
 * */
public class InvalidConfigValueException extends RuntimeException {
    public InvalidConfigValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
