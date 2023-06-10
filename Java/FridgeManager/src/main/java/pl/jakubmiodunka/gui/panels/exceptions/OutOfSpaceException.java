package pl.jakubmiodunka.gui.panels.exceptions;

/**
 * Exception thrown when there are is no space left in particular GUI area.
 *
 * @author Jakub Miodunka
 * */
public class OutOfSpaceException extends RuntimeException {
    public OutOfSpaceException(String message) {
        super(message);
    }
}
