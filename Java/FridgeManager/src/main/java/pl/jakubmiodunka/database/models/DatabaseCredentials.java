package pl.jakubmiodunka.database.models;

import pl.jakubmiodunka.database.models.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;
import org.w3c.dom.Element;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;

/**
 * Container, where database credentials can be stored.
 *
 * @author Jakub Miodunka
 * */
public class DatabaseCredentials {
    // Database URL and credentials
    private final String url;
    private final String username;
    private final String password;

    /**
     * @param  configXmlPath              Path to config XML file containing url,
     *                                    username and password for logging into database.
     * @throws ConfigFileParsingException When parsing of given XML file will fail.
     * */
    public DatabaseCredentials(Path configXmlPath) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "credentials");

            // Properties init
            this.url = XmlUtilities.getContentOfNode(rootElement, "url");
            this.username = XmlUtilities.getContentOfNode(rootElement, "username");
            this.password = XmlUtilities.getContentOfNode(rootElement, "password");

        } catch (XmlParsingException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create table model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @param url      Database URL.
     * @param username Username used for logging into database.
     * @param password User password.
     */
    public DatabaseCredentials(String url, String username, String password) {
        // Properties init
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * @return Database URL.
     * */
    public String getUrl() {
        return url;
    }

    /**
     * @return Username used for logging into database.
     * */
    public String getUsername() {
        return username;
    }

    /**
     * @return User password.
     * */
    public String getPassword() {
        return password;
    }
}
