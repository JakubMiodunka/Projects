package pl.jakubmiodunka.database.models.tables;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Model of database table, where available products are stored.
 * Store names of the table and its columns.
 *
 * @author Jakub Miodunka
 * */
public class ProductsTable extends CategoriesTable {
    // Column names
    private final String categoryIdColumnName;
    private final String expirationDateColumnName;

    /**
     * @param configXmlPath               Path to config XML file containing table name and names of its columns.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public ProductsTable(Path configXmlPath) {
        // Parent class constructor call
        super(configXmlPath);

        // Further initialization of properties
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "table");

            // Extracting 'columns' node from root node
            Element columnsNode = XmlUtilities.getNode(rootElement, "columns");

            // Properties init
            this.categoryIdColumnName = XmlUtilities.getContentOfNode(columnsNode, "categoryId");
            this.expirationDateColumnName = XmlUtilities.getContentOfNode(columnsNode, "expirationDate");

        } catch (XmlParsingException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create table model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return Name of column, where ID of product category is stored.
     * */
    public String getCategoryIdColumnName() {
        return categoryIdColumnName;
    }

    /**
     * @return Name of column, where expiration date of product is stored.
     * */
    public String getExpirationDateColumnName() {
        return expirationDateColumnName;
    }
}
