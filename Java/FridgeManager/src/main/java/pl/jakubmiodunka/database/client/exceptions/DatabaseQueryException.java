package pl.jakubmiodunka.database.client.exceptions;

/**
 * Exception thrown when execution of a SQL query fail.
 *
 * @author Jakub Miodunka
 * */
public class DatabaseQueryException extends RuntimeException {
    public DatabaseQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
