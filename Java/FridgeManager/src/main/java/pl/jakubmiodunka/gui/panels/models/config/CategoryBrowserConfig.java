package pl.jakubmiodunka.gui.panels.models.config;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import org.w3c.dom.Element;
import java.nio.file.Path;

/**
 * Model of categories browser panel configuration, that can be used for initialising it.
 * Class was designed to be able to easily inherit its functionalities.
 *
 * @author Jakub Miodunka
 * */
public class CategoryBrowserConfig {
    // Layout
    protected final int numberOfRows;

    // Column titles
    protected final String idColumnTitle;
    protected final String nameColumnTitle;
    protected final String deleteButtonsColumnTitle;

    // Button titles
    protected final String nextPageButtonTitle;
    protected final String previousPageButtonTitle;
    protected final String deleteButtonsTitle;

    /**
     * @param  configXmlPath              Path to config XML file containing product browser panel configuration.
     * @param  rootNodeName               Name of the root XML node in provided config file.
     *                                    Can be helpful during initialisation child class instances,
     *                                    where name of the node probably will be defined differently.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    protected CategoryBrowserConfig(Path configXmlPath, String rootNodeName) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, rootNodeName);

            // Extracting sub-nodes
            Element layoutNode = XmlUtilities.getNode(rootElement, "layout");
            Element titlesNode = XmlUtilities.getNode(rootElement, "titles");
            Element columnsNode = XmlUtilities.getNode(titlesNode, "columns");
            Element buttonsNode = XmlUtilities.getNode(titlesNode, "buttons");

            // Properties init
            this.numberOfRows = Integer.parseInt(XmlUtilities.getContentOfNode(layoutNode, "numberOfRows"));

            this.idColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "idColumnTitle");
            this.nameColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "nameColumnTitle");
            this.deleteButtonsColumnTitle= XmlUtilities.getContentOfNode(columnsNode, "deleteButtonsColumnTitle");

            this.nextPageButtonTitle= XmlUtilities.getContentOfNode(buttonsNode, "nextPageButtonTitle");
            this.previousPageButtonTitle= XmlUtilities.getContentOfNode(buttonsNode, "previousPageButtonTitle");
            this.deleteButtonsTitle = XmlUtilities.getContentOfNode(buttonsNode, "deleteButtonsTitle");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @param  configXmlPath              Path to config XML file containing product browser panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public CategoryBrowserConfig(Path configXmlPath) {
        // Calling the main constructor with defined root XML node name
        this(configXmlPath, "categoryBrowser");
    }

    /**
     * @return Number of rows in each column displayed in browser panel.
     * */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * @return Title of ID column.
     * */
    public String getIdColumnTitle() {
        return idColumnTitle;
    }

    /**
     * @return Title of name column.
     * */
    public String getNameColumnTitle() {
        return nameColumnTitle;
    }

    /**
     * @return Title of column, where buttons dedicated to removing particular record are placed.
     * */
    public String getDeleteButtonsColumnTitle() {
        return deleteButtonsColumnTitle;
    }


    /**
     * @return Title of every button, placed in delete buttons column.
     * */
    public String getDeleteButtonsTitle() {
        return deleteButtonsTitle;
    }

    /**
     * @return Title of the button, dedicated to moving to the next page during browsing.
     * */
    public String getNextPageButtonTitle() {
        return nextPageButtonTitle;
    }

    /**
     * @return Title of the button, dedicated to moving to the previous page during browsing.
     * */
    public String getPreviousPageButtonTitle() {
        return previousPageButtonTitle;
    }
}
