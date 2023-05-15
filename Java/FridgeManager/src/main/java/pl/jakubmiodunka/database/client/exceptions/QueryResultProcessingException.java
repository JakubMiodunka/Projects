package pl.jakubmiodunka.database.client.exceptions;

/**
 * Exception thrown when conversion of raw query result to a list of record models fail.
 *
 * @author Jakub Miodunka
 * */
public class QueryResultProcessingException extends RuntimeException {
    public QueryResultProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
