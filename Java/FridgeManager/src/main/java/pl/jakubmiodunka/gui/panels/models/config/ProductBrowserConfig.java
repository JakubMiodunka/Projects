package pl.jakubmiodunka.gui.panels.models.config;

import org.w3c.dom.Element;
import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;

/**
 * Model of product explorer panel configuration, that can be used for initialising it.
 * As product browser extends the functionalities of category browser, the class is a child of CategoryBrowserConfig.
 *
 * @author Jakub Miodunka
 * */
public class ProductBrowserConfig extends CategoryBrowserConfig {
    // Additional column titles
    private final String categoryColumnTitle;
    private final String expirationDateColumnTitle;

    /**
     * @param  configXmlPath              Path to config XML file containing product browser panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public ProductBrowserConfig(Path configXmlPath) {
        // Parent class constructor call
        super(configXmlPath, "productBrowser"); // It would be better to store root node name as variable,
                                                            // but parent class constructor call should be first
                                                            // statement in constructor body
        // Initialisation of non-inherited properties
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "productBrowser");

            // Extracting sub-nodes
            Element titlesNode = XmlUtilities.getNode(rootElement, "titles");
            Element columnsNode = XmlUtilities.getNode(titlesNode, "columns");

            // Properties init
            this.categoryColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "categoryColumnTitle");
            this.expirationDateColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "expirationDateColumnTitle");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return Title of product category column.
     * */
    public String getCategoryColumnTitle() {
        return categoryColumnTitle;
    }

    /**
     * @return Title of product expiration date column.
     * */
    public String getExpirationDateColumnTitle() {
        return expirationDateColumnTitle;
    }
}
