package pl.jakubmiodunka.database.repositories;

import pl.jakubmiodunka.database.client.Client;
import pl.jakubmiodunka.database.client.exceptions.DatabaseConnectionException;
import pl.jakubmiodunka.database.client.exceptions.DatabaseQueryException;
import pl.jakubmiodunka.database.client.exceptions.QueryResultProcessingException;
import pl.jakubmiodunka.database.models.content.Category;
import pl.jakubmiodunka.database.models.tables.CategoriesTable;
import pl.jakubmiodunka.database.repositories.exceptions.ForbiddenOperationException;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository of product categories.
 * Serves as a high level API for interactions with database.
 *
 * @author Jakub Miodunka
 */
public class CategoriesRepository {
    // Database client used by the repository along with structure of the table
    private final Client client;
    private final CategoriesTable tableStructure;

    // Product repository used for checking if requested operations won't cause data integrity issues.
    private ProductRepository productRepository;

    // Logger instance
    private final Logger logger;

    /**
     * First stage of categories repository initialisation.
     * Please do not forget to provide also product repository instance using setProductRepository method.
     *
     * @param client         Database client, that will be used by the repository.
     * @param tableStructure Structure of the table, where product categories are stored.
     */
    public CategoriesRepository(Client client, CategoriesTable tableStructure) {
        // Properties init
        this.client = client;
        this.tableStructure = tableStructure;
        this.logger = LoggerFactory.getLogger(CategoriesRepository.class);
    }

    /**
     * Second stage of categories repository initialisation.
     * Need of providing the instance of product repository separately from other properties was introduced
     * due to the fact, that both categories and product repositories needs each other as property - creation
     * of both would be blocked if all properties would be initialised in constructors.
     *
     * @param productRepository Product repository instance.
     * */
    public void setProductRepository(ProductRepository productRepository) {
        // Property init
        this.productRepository = productRepository;
    }

    /**
     * Converts each record from given query result into a list of product categories models.
     *
     * @param  queryResult                    Raw data imported from database.
     * @return                                List of product category models.
     * @throws QueryResultProcessingException When processing of given query result fails.
     */
    private static List<Object> queryResultToListOfCategories(ResultSet queryResult) {
        // Creating result list
        List<Object> importedCategories = new ArrayList<>();

        try {
            // Iterating through returned records
            while (queryResult.next()) {
                // Converting the record to category model and filling the result list with it
                long id = queryResult.getLong("id");
                String name = queryResult.getString("name");

                importedCategories.add(new Category(id, name));
            }
        } catch (SQLException exception) {
            // Wrapping occurred exception
            String errorMessage = "Failed to convert query result to a list of product categories models.";
            throw new QueryResultProcessingException(errorMessage, exception);
        }

        // Returning filled list
        return importedCategories;
    }

    /**
     * Imports all product categories currently present in the database.
     *
     * @return                     List of all categories currently present in database.
     * @throws RepositoryException When execution of generated query fail or there was an issue during
     *                             conversion from raw query result to the list of categories models.
     */
    public List<Category> getAllCategories() {
        // Logging
        this.logger.info("Importing all product categories from database...");

        // Query generation
        String tableName = this.tableStructure.getTableName();
        String idColumnName = this.tableStructure.getIdColumnName();
        String nameColumnName = this.tableStructure.getNameColumnName();

        String query = "SELECT " +
                idColumnName + " AS 'id', " +
                nameColumnName + " AS 'name' " +
                "FROM " + tableName;

        // Importing the data and converting it to the right format
        List<Category> importedCategories;

        try {
            importedCategories = this.client
                    .importData(query, CategoriesRepository::queryResultToListOfCategories)
                    .stream()
                    .map(category -> (Category)category)
                    .toList();

        } catch (DatabaseConnectionException | DatabaseQueryException | QueryResultProcessingException exception) {
            // Logging
            String errorMessage = "Failed to import all product categories from database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("All product categories imported successfully.");

        // Returning processed query result
        return importedCategories;
    }

    /**
     * Checks if category with provided ID exists in the database.
     *
     * @param  categoryId          Category ID.
     * @return                     True or false depending on the check result.
     * @throws RepositoryException When execution of generated query fail.
     * */
    public boolean isInDatabase(long categoryId) {
        // Logging
        logger.debug("Checking if category with ID {} exist in database...", categoryId);

        // Query generation
        String tableName = this.tableStructure.getTableName();
        String idColumnName = this.tableStructure.getIdColumnName();
        String nameColumnName = this.tableStructure.getNameColumnName();

        String query = "SELECT " +
                idColumnName + " AS 'id', " +
                nameColumnName + " AS 'name' " +
                "FROM " + tableName +
                " WHERE id = " + categoryId;

        // Importing data
        List<Object> rawQueryResult;
        try {
            rawQueryResult = this.client.importData(query, CategoriesRepository::queryResultToListOfCategories);

        } catch (DatabaseConnectionException | DatabaseQueryException | QueryResultProcessingException exception) {
            // Logging
            String errorMessage = "Failed to perform requested check.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Empty list means that category with specified name does not exist
        boolean isCategoryExist = !(rawQueryResult.isEmpty());

        // Logging
        if (isCategoryExist) {
            this.logger.debug("Category with specified ID exist in database.");
        }
        else {
            this.logger.debug("Category with specified ID does not exist in database.");
        }

        // Returning the check result
        return isCategoryExist;
    }

    /**
     * Checks if category with provided name exists in the database.
     *
     * @param categoryName         Category name.
     * @return                     True or false depending on the check result.
     * @throws RepositoryException When execution of generated query fail.
     * */
    public boolean isInDatabase(String categoryName) {
        // Logging
        logger.debug("Checking if category called '{}' exist in database...", categoryName);

        // Query generation
        String tableName = this.tableStructure.getTableName();
        String idColumnName = this.tableStructure.getIdColumnName();
        String nameColumnName = this.tableStructure.getNameColumnName();

        String query = "SELECT " +
                idColumnName + " AS 'id', " +
                nameColumnName + " AS 'name' " +
                "FROM " + tableName +
                " WHERE name = '" + categoryName + "'";

        // Importing data
        List<Object> rawQueryResult;
        try {
            rawQueryResult = this.client.importData(query, CategoriesRepository::queryResultToListOfCategories);

        } catch (DatabaseConnectionException | DatabaseQueryException | QueryResultProcessingException exception) {
            // Logging
            String errorMessage = "Failed to perform requested check.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Empty list means that category with specified name does not exist
        boolean isCategoryExist = !(rawQueryResult.isEmpty());

        // Logging
        if (isCategoryExist) {
            this.logger.debug("Category with specified name exist in database.");
        }
        else {
            this.logger.debug("Category with specified name does not exist in database.");
        }

        // Returning the check result
        return isCategoryExist;
    }

    /**
     * Adds new category into database.
     * There is no possibility to add a category named the same as already existing one.
     *
     * @param  categoryName                Category name.
     * @throws ForbiddenOperationException When category with provided name already exist in database.
     * @throws RepositoryException         When execution of generated query fail.
     */
    public void addNewCategory(String categoryName) {
        // Logging
        logger.info("Adding category named as '{}' to database...", categoryName);

        // Checking if requested operation does not cause violation of data integrity
        if (this.isInDatabase(categoryName)) {
            // Logging
            String errorMessage = "Operation considered as unsafe.";
            this.logger.error(errorMessage);

            // Throwing an exception
            throw new ForbiddenOperationException(errorMessage);
        }

        // Logging
        this.logger.debug("Operation considered as safe.");

        // Query generation
        String tableName = this.tableStructure.getTableName();
        String nameColumnName = this.tableStructure.getNameColumnName();

        String query = "INSERT INTO " + tableName + " (" + nameColumnName + ") VALUES ('" + categoryName + "')";

        // Execution of generated query
        try {
            this.client.updateData(query);

        } catch (DatabaseConnectionException | DatabaseQueryException exception) {
            // Logging
            String errorMessage = "Failed to add new category to database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("Category successfully added to database.");
    }

    /**
     * Removes category from database.
     * It is not possible to remove category, to which some products are still belong to.
     * If category with provided ID does not exist no exception will be thrown but
     * generated query still will be executed.
     *
     * @param  categoryId                  ID of category to be deleted.
     * @throws ForbiddenOperationException When to category with provided ID still belong some products.
     * @throws RepositoryException         When executions of generated queries fails.
     */
    public void removeCategory(long categoryId) {
        // Logging
        this.logger.info("Removing category with ID {} from database...", categoryId);

        // Checking if requested operation does not cause violation of data integrity
        try {
            if (this.productRepository.productQuantityInCategory(categoryId) > 0) {
                // Logging
                String errorMessage = "Removal of specified category not allowed" +
                        "- there are still some products related to it.";
                this.logger.error(errorMessage);

                // Throwing an exception
                throw new ForbiddenOperationException(errorMessage);
            }

        } catch (NullPointerException exception) {
            // Logging
            String errorMessage = "Requested operation require to use instance of product repository," +
                    "which was not provided during repository initialisation.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.debug("Operation considered as safe.");

        // Query generation
        String tableName = this.tableStructure.getTableName();
        String idColumnName = this.tableStructure.getIdColumnName();

        String query = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = " + categoryId;

        // Execution of generated query
        try {
            this.client.updateData(query);

        } catch (DatabaseConnectionException | DatabaseQueryException exception) {
            // Logging
            String errorMessage = "Failed to delete specified category from database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("Specified category successfully removed from database.");
    }
}
