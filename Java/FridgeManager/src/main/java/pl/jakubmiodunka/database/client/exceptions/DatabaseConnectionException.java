package pl.jakubmiodunka.database.client.exceptions;

/**
 * Exception thrown when there are issues with database connection.
 *
 * @author Jakub Miodunka
 * */
public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
