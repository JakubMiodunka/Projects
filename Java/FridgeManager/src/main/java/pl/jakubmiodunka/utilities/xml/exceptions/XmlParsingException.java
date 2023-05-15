package pl.jakubmiodunka.utilities.xml.exceptions;

/**
 * Exception thrown when parsing of XML file fail.
 *
 * @author Jakub Miodunka
 * */
public class XmlParsingException extends RuntimeException {
    public XmlParsingException(String message) {
        super(message);
    }

    public XmlParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
