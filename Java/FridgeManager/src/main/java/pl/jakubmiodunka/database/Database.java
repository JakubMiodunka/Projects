package pl.jakubmiodunka.database;

import pl.jakubmiodunka.database.client.Client;
import pl.jakubmiodunka.database.models.DatabaseCredentials;
import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.database.models.tables.CategoriesTable;
import pl.jakubmiodunka.database.models.tables.ProductsTable;
import pl.jakubmiodunka.database.repositories.CategoriesRepository;
import pl.jakubmiodunka.database.repositories.ProductRepository;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for created repositories - the end point of backed core.
 * The goal was to keep only one instance of each repository to be used in the program.
 *
 * @author Jakub Miodunka
 * */
public class Database {
    // Repositories instances
    private static ProductRepository productRepository;
    private static CategoriesRepository categoriesRepository;

    /**
     * Explicit initialisation of the class.
     *
     * @throws ConfigFileParsingException When parsing config XML files fail.
     * */
    public static void initialise() {
        // Initialising logger
        Logger logger = LoggerFactory.getLogger(Database.class);

        // Initialising database client
        Path credentialsXml = Path.of("src/main/resources/config/database/credentials/credentials.xml");
        logger.debug("Creating model of database credentials using '{}' file...", credentialsXml);
        DatabaseCredentials credentials = new DatabaseCredentials(credentialsXml);
        logger.debug("Database credentials model successfully created.");

        logger.info("Creating '{}' database client...", credentials.getUrl());
        Client client = new Client(credentials);
        logger.info("Database client successfully created.");

        // Initialising categories repository - step 1
        Path categoriesTableXml = Path.of("src/main/resources/config/database/structure/categories_table.xml");
        logger.debug("Creating model of categories table using '{}' file...", categoriesTableXml);
        CategoriesTable categoriesTable = new CategoriesTable(categoriesTableXml);
        logger.debug("Categories table model successfully created.");

        logger.info("Creating categories repository...");
        CategoriesRepository categoriesRepository = new CategoriesRepository(client, categoriesTable);

        // Initialising products repository - step 1
        Path productsTableXml = Path.of("src/main/resources/config/database/structure/products_table.xml");
        logger.debug("Creating model of products table using '{}' file...", productsTableXml);
        ProductsTable productsTable = new ProductsTable(productsTableXml);

        logger.info("Creating products repository...");
        ProductRepository productRepository = new ProductRepository(client, productsTable, categoriesTable);

        // Finishing the initialisation of both repositories - step 2
        logger.debug("Providing instance of product repository to categories repository.");
        categoriesRepository.setProductRepository(productRepository);
        logger.info("Categories repository successfully created.");

        logger.debug("Providing instance of categories repository to product repository.");
        productRepository.setCategoriesRepository(categoriesRepository);
        logger.info("Products repository successfully created.");

        // Setting static properties
        Database.categoriesRepository = categoriesRepository;
        Database.productRepository = productRepository;
    }

    /**
     * Returns categories repository.
     * Initialises the class if it was not already done.
     *
     * @return                            Categories repository.
     * @throws ConfigFileParsingException When parsing config XML files fail.
     * */
    public static CategoriesRepository getCategoriesRepository() {
        // Initializing the class if it was not already done
        if (Database.categoriesRepository == null) {
            Database.initialise();
        }

        // Returning the categories repository
        return Database.categoriesRepository;
    }

    /**
     * Returns products repository.
     * Initialises the class if it was not already done.
     *
     * @return                            Product repository.
     * @throws ConfigFileParsingException When parsing config XML files fail.
     * */
    public static ProductRepository getProductRepository() {
        // Initializing the class if it was not already done
        if (Database.productRepository == null) {
            Database.initialise();
        }

        // Returning the product repository
        return Database.productRepository;
    }
}
