package pl.jakubmiodunka.gui.panels.models.config;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Model of product adder panel configuration, that can be used for initialising it.
 * As product adder panel extends the functionalities of category adder, the class is a child of CategoryAdderConfig.
 *
 * @author Jakub Miodunka
 * */
public class ProductAdderConfig extends CategoryAdderConfig{
    // Additional component titles;
    private final String expirationDateTextFieldTitle;
    private final String categoriesComboBoxTitle;

    // Formatting
    private final String expirationDateFormat;

    /**
     * @param  configXmlPath              Path to config XML file containing product adder panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public ProductAdderConfig(Path configXmlPath) {
        // Parent class constructor call
        super(configXmlPath, "productAdder"); // It would be better to store root node name as variable,
                                                            // but parent class constructor call should be first
                                                            // statement in constructor body
        // Initialisation of non-inherited properties
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "productAdder");

            // Extracting sub-nodes
            Element titlesNode = XmlUtilities.getNode(rootElement, "titles");
            Element textFieldsTitlesNode = XmlUtilities.getNode(titlesNode, "textFields");
            Element comboBoxesTitlesNode = XmlUtilities.getNode(titlesNode, "comboBoxes");
            Element formattingNode = XmlUtilities.getNode(rootElement, "formatting");

            // Properties init
            this.expirationDateTextFieldTitle = XmlUtilities.getContentOfNode(textFieldsTitlesNode, "expirationDateTextField");
            this.categoriesComboBoxTitle = XmlUtilities.getContentOfNode(comboBoxesTitlesNode, "categoriesComboBoxTitle");
            this.expirationDateFormat = XmlUtilities.getContentOfNode(formattingNode, "expirationDateFormat");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return Title of text field, where product expiration date is input.
     * */
    public String getExpirationDateTextFieldTitle() {
        return expirationDateTextFieldTitle;
    }

    /**
     * @return Title of combo box, where product category is being chosen.
     * */
    public String getCategoriesComboBoxTitle() {
        return categoriesComboBoxTitle;
    }

    /**
     * @return Expected format of the product expiration date.
     *         Syntax the same as this used in DateTimeFormatter.ofPattern().
     * */
    public String getExpirationDateFormat() {
        return expirationDateFormat;
    }
}
