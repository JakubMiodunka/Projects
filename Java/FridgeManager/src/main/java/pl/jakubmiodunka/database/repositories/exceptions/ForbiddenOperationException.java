package pl.jakubmiodunka.database.repositories.exceptions;

/**
 * Exception thrown when requested operation jeopardizes data integrity
 *
 * @author Jakub Miodunka
 * */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
