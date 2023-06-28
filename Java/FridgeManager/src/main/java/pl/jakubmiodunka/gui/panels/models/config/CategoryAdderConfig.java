package pl.jakubmiodunka.gui.panels.models.config;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import org.w3c.dom.Element;
import java.nio.file.Path;

/**
 * Model of category adder panel configuration, that can be used for initialising it.
 * Class was designed to be able to easily inherit its functionalities.
 *
 * @author Jakub Miodunka
 * */
public class CategoryAdderConfig {
    // Component titles
    protected final String panelTitle;
    protected final String recordTextFieldTitle;
    protected final String addButtonTitle;

    /**
     * @param  configXmlPath              Path to config XML file containing category adder panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    protected CategoryAdderConfig(Path configXmlPath, String rootNodeName) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, rootNodeName);

            // Extracting sub-nodes
            Element titlesNode = XmlUtilities.getNode(rootElement, "titles");
            Element textFieldsTitlesNode = XmlUtilities.getNode(titlesNode, "textFields");
            Element buttonsTitlesNode = XmlUtilities.getNode(titlesNode, "buttons");

            // Properties init
            this.panelTitle = XmlUtilities.getContentOfNode(titlesNode, "panelTitle");
            this.recordTextFieldTitle = XmlUtilities.getContentOfNode(textFieldsTitlesNode, "recordNameTextFieldTitle");
            this.addButtonTitle = XmlUtilities.getContentOfNode(buttonsTitlesNode, "addButtonTitle");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @param  configXmlPath              Path to config XML file containing category adder panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public CategoryAdderConfig(Path configXmlPath) {
        // Calling the main constructor with defined root XML node name
        this(configXmlPath, "categoryAdder");
    }

    /**
     * @return Title of the panel.
     * */
    public String getPanelTitle() {
        return panelTitle;
    }

    /**
     * @return Title of text field, where record name is input.
     * */
    public String getRecordTextFieldTitle() {
        return recordTextFieldTitle;
    }

    /**
     * @return Title of the button, which is used to submit the action of adding the record to the repository.
     * */
    public String getAddButtonTitle() {
        return addButtonTitle;
    }
}
