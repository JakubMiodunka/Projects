package pl.jakubmiodunka.gui.panels.models.config;

import org.w3c.dom.Element;
import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;

/**
 * Model of product explorer panel configuration, that can be used for initialising it.
 *
 * @author Jakub Miodunka
 * */
public class ProductBrowserConfig {
    // Layout
    private final int numberOfRows;

    // Column titles
    private final String idColumnTitle;
    private final String nameColumnTitle;
    private final String categoryColumnTitle;
    private final String expirationDateColumnTitle;
    private final String deleteButtonsColumnTitle;

    // Button titles
    private final String nextPageButtonTitle;
    private final String previousPageButtonTitle;
    private final String deleteButtonTitle;

    /**
     * @param  configXmlPath              Path to config XML file containing product browser panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public ProductBrowserConfig(Path configXmlPath) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "productExplorer");

            // Extracting sub-nodes
            Element layoutNode = XmlUtilities.getNode(rootElement, "layout");
            Element titlesNode = XmlUtilities.getNode(rootElement, "titles");
            Element columnsNode = XmlUtilities.getNode(titlesNode, "columns");
            Element buttonsNode = XmlUtilities.getNode(titlesNode, "buttons");

            // Properties init
            this.numberOfRows = Integer.parseInt(XmlUtilities.getContentOfNode(layoutNode, "numberOfRows"));

            this.idColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "idColumnTitle");
            this.nameColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "nameColumnTitle");
            this.categoryColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "categoryColumnTitle");
            this.expirationDateColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "expirationDateColumnTitle");
            this.deleteButtonsColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "deleteButtonsColumnTitle");

            this.nextPageButtonTitle= XmlUtilities.getContentOfNode(buttonsNode, "nextPageButtonTitle");
            this.previousPageButtonTitle= XmlUtilities.getContentOfNode(buttonsNode, "previousPageButtonTitle");
            this.deleteButtonTitle= XmlUtilities.getContentOfNode(buttonsNode, "deleteButtonTitle");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return Number of rows in each column displayed in product explorer panel.
     * */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * @return Title of product ID column.
     * */
    public String getIdColumnTitle() {
        return idColumnTitle;
    }

    /**
     * @return Title of product name column.
     * */
    public String getNameColumnTitle() {
        return nameColumnTitle;
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

    /**
     * @return Title of column, where buttons dedicated to removing particular products are placed.
     * */
    public String getDeleteButtonsColumnTitle() {
        return deleteButtonsColumnTitle;
    }

    /**
     * @return Title of every button, placed in delete buttons column.
     * */
    public String getDeleteButtonTitle() {
        return deleteButtonTitle;
    }

    /**
     * @return Title of the button, dedicated to moving to the next page during product browsing.
     * */
    public String getNextPageButtonTitle() {
        return nextPageButtonTitle;
    }

    /**
     * @return Title of the button, dedicated to moving to the previous page during product browsing.
     * */
    public String getPreviousPageButtonTitle() {
        return previousPageButtonTitle;
    }
}
