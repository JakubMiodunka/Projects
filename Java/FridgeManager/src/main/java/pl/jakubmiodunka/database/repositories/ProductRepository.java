package pl.jakubmiodunka.database.repositories;

import pl.jakubmiodunka.database.client.Client;
import pl.jakubmiodunka.database.client.exceptions.DatabaseConnectionException;
import pl.jakubmiodunka.database.client.exceptions.DatabaseQueryException;
import pl.jakubmiodunka.database.client.exceptions.QueryResultProcessingException;
import pl.jakubmiodunka.database.models.content.Product;
import pl.jakubmiodunka.database.models.tables.CategoriesTable;
import pl.jakubmiodunka.database.models.tables.ProductsTable;
import pl.jakubmiodunka.database.repositories.exceptions.ForbiddenOperationException;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository of products.
 * Serves as a high level API for interactions with database.
 *
 * @author Jakub Miodunka
 */
public class ProductRepository {
    // Database client used by the repository along with structure of the tables
    private final Client client;
    private final ProductsTable productsTable;
    private final CategoriesTable categoriesTable;

    // Categories repository used for checking if requested operations won't cause data integrity issues.
    private CategoriesRepository categoriesRepository;

    // Logger instance
    private final Logger logger;

    /**
     * First stage of products repository initialisation.
     * Please do not forget to provide also product repository instance using setCategoriesRepository method.
     *
     * @param client          Database client, that will be used by the repository.
     * @param productsTable   Structure of the table, where product are stored.
     * @param categoriesTable Structure of the table, where product categories are stored.
     */
    public ProductRepository(Client client, ProductsTable productsTable, CategoriesTable categoriesTable) {
        // Properties init
        this.client = client;
        this.productsTable = productsTable;
        this.categoriesTable = categoriesTable;
        this.logger = LoggerFactory.getLogger(ProductRepository.class);
    }

    /**
     * Second stage of products repository initialisation.
     * Need of providing the instance of categories repository separately from other properties was introduced
     * due to the fact, that both categories and product repositories needs each other as property - creation
     * of both would be blocked if all properties would be initialised in constructors.
     *
     * @param categoriesRepository Categories repository instance.
     */
    public void setCategoriesRepository(CategoriesRepository categoriesRepository) {
        // Property init
        this.categoriesRepository = categoriesRepository;
    }

    /**
     * Conversion method between java.sql.Date and java.time.LocalDate.
     * Found that usage of java.util.Date is not recommended since introduction of java.time.
     *
     * @param date Date in form of java.sql.Date instance.
     * @return     Date in form of java.time.LocalDate instance.
     */
    private static LocalDate convertToLocalDate(Date date) {
        // Getting epoch timestamp in milliseconds
        Instant timestamp = Instant.ofEpochMilli(date.getTime());

        // Creation and return the new java.time.LocalDate instance
        return LocalDate.ofInstant(timestamp, ZoneId.systemDefault());
    }

    /**
     * Converts each record from given query result into a list of product models.
     *
     * @param  queryResult                    Data imported from database.
     * @return                                List of converted product models.
     * @throws QueryResultProcessingException When processing of given query result fails.
     */
    private static List<Object> queryResultToListOfProducts(ResultSet queryResult) {
        // Creating result list
        List<Object> importedProducts = new ArrayList<>();

        try (queryResult) {
            // Iterating through returned records
            while (queryResult.next()) {
                // Converting record from query result to product model and filling the result list with it
                long id = queryResult.getLong("id");
                String name = queryResult.getString("name");
                String category = queryResult.getString("category");
                LocalDate expirationDate = convertToLocalDate(queryResult.getDate("expiration_date"));

                importedProducts.add(new Product(id, name, category, expirationDate));
            }

        } catch (SQLException exception) {
            // Wrapping occurred exception
            String errorMessage = "Failed to convert query result to a list of products models.";
            throw new QueryResultProcessingException(errorMessage, exception);
        }

        // Returning filled list
        return importedProducts;
    }

    /**
     * Imports specified number of products currently present in the database starting from given index.
     *
     * @param startIndex           Index, from which record importing should be started.
     * @param numberOfProducts     Number of records to import starting from index given previously.
     * @return                     List of products sized according to given parameters.
     * @throws RepositoryException When execution of generated query fail or there was an issue during
     *                             conversion from raw query result to the list of products models.
     */
    public List<Product> getProducts(long startIndex, long numberOfProducts) {
        // Logging
        this.logger.info("Importing {} products from database starting from index {}...", numberOfProducts, startIndex);

        // Query generation
        String productsTableName = this.productsTable.getTableName();
        String productsIdColumnName = this.productsTable.getIdColumnName();
        String productsNameColumnName = this.productsTable.getNameColumnName();
        String productCategoryIdColumnName = this.productsTable.getCategoryIdColumnName();
        String productExpirationDateColumnName = this.productsTable.getExpirationDateColumnName();

        String categoriesTableName = this.categoriesTable.getTableName();
        String categoriesIdColumnName = this.categoriesTable.getIdColumnName();
        String categoriesNameColumnName = this.categoriesTable.getNameColumnName();

        String query = "SELECT " +
                productsTableName + "." + productsIdColumnName + " AS 'id', " +
                productsTableName + "." + productsNameColumnName + " AS 'name', " +
                categoriesTableName + "." + categoriesNameColumnName + " AS 'category', " +
                productsTableName + "." + productExpirationDateColumnName + " AS 'expiration_date' " +
                "FROM " + productsTableName + " INNER JOIN " + categoriesTableName + " ON " +
                productsTableName + "." + productCategoryIdColumnName + " = " +
                categoriesTableName + "." + categoriesIdColumnName +
                " LIMIT " + startIndex + ", " + numberOfProducts;

        // Importing the data and converting it to the right format
        List<Product> importedProducts;

        try {
            importedProducts = this.client
                    .importData(query, ProductRepository::queryResultToListOfProducts)
                    .stream()
                    .map(product -> (Product)product)
                    .toList();

        } catch (DatabaseConnectionException | DatabaseQueryException | QueryResultProcessingException exception) {
            // Logging
            String errorMessage = "Failed to import products from database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("Products imported successfully.");

        // Returning processed query result
        return importedProducts;
    }

    /**
     * Imports specified number of products, that would already be expired on the given date starting from given index.
     *
     * @param date                 Date used as reference.
     * @param startIndex           Index, from which record importing should be started.
     * @param numberOfProducts     Number of records to import starting from index given previously.
     * @return                     List of products sized accordingly to given startIndex and numberOfProducts,
     *                             that would already be expired on the given date.
     * @throws RepositoryException When execution of generated query fail or there was an issue during
     *                             conversion from raw query result to the list of products models.
     */
    public List<Product> getExpiredProducts(LocalDate date, long startIndex, long numberOfProducts) {
        // Logging
        this.logger.info("Importing {} products, that would be expired on {} starting from index {}...",
                numberOfProducts, date, startIndex);

        // Query generation
        String productsTableName = this.productsTable.getTableName();
        String productsIdColumnName = this.productsTable.getIdColumnName();
        String productsNameColumnName = this.productsTable.getNameColumnName();
        String productExpirationDateColumnName = this.productsTable.getExpirationDateColumnName();

        String categoriesTableName = this.categoriesTable.getTableName();
        String categoriesIdColumnName = this.categoriesTable.getIdColumnName();
        String categoriesNameColumnName = this.categoriesTable.getNameColumnName();

        String query = "SELECT " +
                productsTableName + "." + productsIdColumnName + " AS 'id', " +
                productsTableName + "." + productsNameColumnName + " AS 'name', " +
                categoriesTableName + "." + categoriesNameColumnName + " AS 'category'," +
                productsTableName + "." + productExpirationDateColumnName + " AS 'expiration_date'" +
                "FROM " + productsTableName + " INNER JOIN " + categoriesTableName + " ON " +
                productsTableName + "." + productsIdColumnName + " = " +
                categoriesTableName + "." + categoriesIdColumnName +
                " WHERE expiration_date <= " + "'" + date + "'" +
                " LIMIT " + startIndex + ", " + numberOfProducts;

        // Importing the data and converting it to the right format
        List<Product> importedProducts;

        try {
            importedProducts = this.client
                    .importData(query, ProductRepository::queryResultToListOfProducts)
                    .stream()
                    .map(product -> (Product)product)
                    .toList();

        } catch (DatabaseConnectionException | DatabaseQueryException | QueryResultProcessingException exception) {
            // Logging
            String errorMessage = "Failed to import products from database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("Products imported successfully.");

        // Returning processed query result
        return importedProducts;
    }

    /**
     * Converts given query result to the product quantity.
     *
     * @param  queryResult                    Data imported from database.
     * @return                                One element list where the product quantity is stored.
     * @throws QueryResultProcessingException When processing of given query result fails.
     * */
    private static List<Object> queryResultToQuantityOfProducts (ResultSet queryResult) {
        try (queryResult) {
            // Creating result list
            List<Object> resultList = new ArrayList<>();

            // Moving on to the result of products counting and extracting the value
            queryResult.next();
            resultList.add(queryResult.getLong("quantity"));

            // Returning the value
            return resultList;

        } catch (SQLException exception) {
            // Wrapping caught exception
            String errorMessage = "Failed to extract products quantity from given query result.";
            throw new QueryResultProcessingException(errorMessage, exception);
        }
    }

    /**
     * Checks how many products belongs to specified category.
     *
     * @return                     Check result.
     * @throws RepositoryException When execution of generated query fail.
     * */
    public long productQuantityInCategory(long categoryId) {
        // Logging
        this.logger.debug("Checking how many products belong to category with ID {}", categoryId);

        // Query generation
        String tableName = this.productsTable.getTableName();
        String idColumnName = this.productsTable.getIdColumnName();
        String filterColumn = this.productsTable.getCategoryIdColumnName();

        String query = "SELECT COUNT(" + idColumnName + ") AS 'quantity' " +
                "FROM " + tableName +
                " WHERE " + filterColumn + " = " + categoryId;

        // Importing the data
        List<Object> queryResult;

        try {
            queryResult = this.client.importData(query, ProductRepository::queryResultToQuantityOfProducts);
        } catch (DatabaseQueryException | QueryResultProcessingException exception) {
            // Logging
            String errorMessage = "Failed to perform requested check.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Extracting the exact value from query result
        long productsQuantity = (long) queryResult.get(0);

        // Logging
        this.logger.debug("There is(are) {} product(s) that belong to specified category.", productsQuantity);

        // Returning the value
        return productsQuantity;
    }

    /**
     * Adds new product into database.
     * There is no possibility to add a product that belongs to category, that does not exist.
     *
     * @param  productName                 Name of the product.
     * @param categoryId                   ID of category, to which product belong to.
     * @param expirationDate               Date of product expiration.
     * @throws ForbiddenOperationException When category, to which provided category belong does not exist in database.
     * @throws RepositoryException         When execution of generated query fail.
     */
    public void addNewProduct(String productName, long categoryId, LocalDate expirationDate) {
        // Logging
        logger.info("Adding product named as '{}' to database...", productName);

        // Checking if requested operation does not cause violation of data integrity
        try {
            if (this.categoriesRepository.isInDatabase(categoryId)) {
                // Logging
                this.logger.debug("Operation considered as safe.");
            } else {
                // Logging
                String errorMessage = "Operation considered as unsafe for data integrity.";
                this.logger.error(errorMessage);

                // Throwing an exception
                throw new ForbiddenOperationException(errorMessage);
            }

        } catch (NullPointerException exception) {
            // Logging
            String errorMessage = "Requested operation require to use instance of categories repository," +
                    "which was not provided during repository initialisation.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Query generation
        String tableName = this.productsTable.getTableName();
        String nameColumnName = this.productsTable.getNameColumnName();
        String categoryIdColumnName = this.productsTable.getCategoryIdColumnName();
        String expirationDateColumnName = this.productsTable.getExpirationDateColumnName();

        String query = "INSERT INTO " + tableName +
                " (" + nameColumnName + ", " + categoryIdColumnName + ", " + expirationDateColumnName +
                ") VALUES ('" + productName + "', " + categoryId + ", '" + expirationDate + "')";

        // Execution of generated query
        try {
            this.client.updateData(query);

        } catch (DatabaseConnectionException | DatabaseQueryException exception) {
            // Logging
            String errorMessage = "Failed to add new product to database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("Product successfully added to database.");
    }

    /**
     * Removes product from database.
     * If product with provided ID does not exist no exception will be thrown but
     * generated query still will be executed.
     *
     * @param  productId                   ID of product to be deleted.
     * @throws RepositoryException         When executions of generated queries fails.
     */
    public void removeProduct(long productId) {
        // Logging
        this.logger.info("Removing product with ID {} from database...", productId);

        // Query generation
        String tableName = this.productsTable.getTableName();
        String idColumnName = this.productsTable.getIdColumnName();

        String query = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = " + productId;

        // Execution of generated query
        try {
            this.client.updateData(query);

        } catch (DatabaseConnectionException | DatabaseQueryException exception) {
            // Logging
            String errorMessage = "Failed to delete specified product from database.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RepositoryException(errorMessage, exception);
        }

        // Logging
        this.logger.info("Specified product successfully removed from database.");
    }
}
