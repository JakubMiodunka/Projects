package pl.jakubmiodunka.database.models.tables;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Model of database table, where available products categories are stored.
 * Store names of the table and its columns.
 *
 * @author Jakub Miodunka
 * */
public class CategoriesTable {
    // Name of the table in database
    protected final String tableName;

    // Column names
    protected final String idColumnName;
    protected final String nameColumnName;

    /**
     * @param  configXmlPath              Path to config XML file containing table name and names of its columns.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public CategoriesTable(Path configXmlPath) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "table");

            // Extracting 'columns' node from root node
            Element columnsNode = XmlUtilities.getNode(rootElement, "columns");

            // Properties init
            this.tableName = XmlUtilities.getContentOfNode(rootElement, "name");
            this.idColumnName = XmlUtilities.getContentOfNode(columnsNode, "id");
            this.nameColumnName = XmlUtilities.getContentOfNode(columnsNode, "name");

        } catch (XmlParsingException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create table model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return Name of the table.
     * */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return Name of column, where record ID is stored.
     * */
    public String getIdColumnName() {
        return idColumnName;
    }

    /**
     * @return Name of column, where record name is stored.
     * */
    public String getNameColumnName() {
        return nameColumnName;
    }
}
