package pl.jakubmiodunka.gui.panels.models.config;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Model of product adder panel configuration, that can be used for initialising it.
 *
 * @author Jakub Miodunka
 * */
public class ProductAdderConfig {
    // Component titles
    private final String panelTitle;
    private final String productNameTextFieldTitle;
    private final String expirationDateTextFieldTitle;
    private final String categoriesComboBoxTitle;
    private final String addButtonTitle;

    // Formatting
    private final String expirationDateFormat;

    /**
     * @param  configXmlPath              Path to config XML file containing product adder panel configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public ProductAdderConfig(Path configXmlPath) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "productAdder");

            // Extracting sub-nodes
            Element titlesNode = XmlUtilities.getNode(rootElement, "titles");
            Element textFieldsTitlesNode = XmlUtilities.getNode(titlesNode, "textFields");
            Element comboBoxesTitlesNode = XmlUtilities.getNode(titlesNode, "comboBoxes");
            Element buttonsTitlesNode = XmlUtilities.getNode(titlesNode, "buttons");
            Element formattingNode = XmlUtilities.getNode(rootElement, "formatting");

            // Properties init
            this.panelTitle = XmlUtilities.getContentOfNode(titlesNode, "panelTitle");
            this.productNameTextFieldTitle = XmlUtilities.getContentOfNode(textFieldsTitlesNode, "productNameTextFieldTitle");
            this.expirationDateTextFieldTitle = XmlUtilities.getContentOfNode(textFieldsTitlesNode, "expirationDateTextField");
            this.categoriesComboBoxTitle = XmlUtilities.getContentOfNode(comboBoxesTitlesNode, "categoriesComboBoxTitle");
            this.addButtonTitle = XmlUtilities.getContentOfNode(buttonsTitlesNode, "addButtonTitle");
            this.expirationDateFormat = XmlUtilities.getContentOfNode(formattingNode, "expirationDateFormat");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return Title of the panel.
     * */
    public String getPanelTitle() {
        return panelTitle;
    }

    /**
     * @return Title of text field, where product name is input.
     * */
    public String getProductNameTextFieldTitle() {
        return productNameTextFieldTitle;
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
     * @return Title of the button, which is used to submit the action of adding the product to the repository.
     * */
    public String getAddButtonTitle() {
        return addButtonTitle;
    }

    /**
     * @return Expected format of the product expiration date.
     *         Syntax the same as this used in DateTimeFormatter.ofPattern().
     * */
    public String getExpirationDateFormat() {
        return expirationDateFormat;
    }
}
