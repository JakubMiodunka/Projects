package pl.jakubmiodunka.database.repositories.exceptions;

/**
 * Generic exception thrown when action performed on repository fail.
 *
 * @author Jakub Miodunka
 * */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
