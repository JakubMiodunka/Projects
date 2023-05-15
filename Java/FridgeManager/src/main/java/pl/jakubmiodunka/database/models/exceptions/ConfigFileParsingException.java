package pl.jakubmiodunka.database.models.exceptions;

/**
 * Exception thrown when extraction of data from config file fail.
 *
 * @author Jakub Miodunka
 * */
public class ConfigFileParsingException extends RuntimeException {
    public ConfigFileParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
