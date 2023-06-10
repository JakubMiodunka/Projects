package pl.jakubmiodunka.gui.panels;

import pl.jakubmiodunka.database.Database;
import pl.jakubmiodunka.database.models.content.Product;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;
import pl.jakubmiodunka.gui.panels.exceptions.OutOfSpaceException;
import pl.jakubmiodunka.gui.panels.interfaces.RefreshablePanel;
import pl.jakubmiodunka.gui.panels.models.config.ProductBrowserConfig;
import pl.jakubmiodunka.gui.panels.utilities.ButtonColumn;
import pl.jakubmiodunka.gui.panels.utilities.LabelColumn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel, where product stored in repository can be browsed.
 *
 * @author Jakub Miodunka
 * */
public class ProductBrowser extends JPanel implements RefreshablePanel {
    // Column where content of given records is presented
    private final LabelColumn idColumn;
    private final LabelColumn nameColumn;
    private final LabelColumn categoryColumn;
    private final LabelColumn expirationDateColumn;
    private final ButtonColumn deleteButtonsColumn;

    // Number of rows in each column
    private final int numberOfRows;

    // Title of each button in delete buttons column
    private final String deleteButtonTitle;

    // Buttons used for navigation between pages
    private final JButton nextPageButton;
    private final JButton previousPageButton;

    // Current page number
    private long page;

    // Internally used logger
    private final Logger logger;

    /**
     * @param config Configuration of the panel, that will be used during initialisation.
     * */
    public ProductBrowser(ProductBrowserConfig config) {
        // Parent Class constructor call
        super();

        // Initialising logger
        this.logger = LoggerFactory.getLogger(ProductBrowser.class);

        // Basic properties init
        this.numberOfRows = config.getNumberOfRows();
        this.deleteButtonTitle = config.getDeleteButtonTitle();

        // Creating columns
        this.idColumn = new LabelColumn(this.numberOfRows, config.getIdColumnTitle());
        this.nameColumn = new LabelColumn(this.numberOfRows, config.getNameColumnTitle());
        this.categoryColumn = new LabelColumn(this.numberOfRows, config.getCategoryColumnTitle());
        this.expirationDateColumn = new LabelColumn(this.numberOfRows, config.getExpirationDateColumnTitle());
        this.deleteButtonsColumn = new ButtonColumn(this.numberOfRows, config.getDeleteButtonsColumnTitle());

        // Creating buttons
        this.nextPageButton = new JButton(config.getNextPageButtonTitle());
        this.nextPageButton.addActionListener(event -> this.moveToNextPage());
        this.previousPageButton = new JButton(config.getPreviousPageButtonTitle());
        this.previousPageButton.addActionListener(event -> this.moveToPreviousPage());

        // Initialising things related with paging
        this.page = 0;                              // Initially set to first page
        this.previousPageButton.setEnabled(false);  // Initially disabled as moving to page -1 makes no sense

        // Setting the panel layout
        this.setLayout(new BorderLayout());

        JPanel columnPanel = new JPanel();
        columnPanel.setLayout(new GridLayout(1, 5));
        columnPanel.add(this.idColumn);
        columnPanel.add(this.nameColumn);
        columnPanel.add(this.categoryColumn);
        columnPanel.add(this.expirationDateColumn);
        columnPanel.add(this.deleteButtonsColumn);
        this.add(columnPanel, BorderLayout.CENTER);

        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new GridLayout(1, 2));
        navigationPanel.add(this.nextPageButton);
        navigationPanel.add(this.previousPageButton);
        this.add(navigationPanel, BorderLayout.SOUTH);

        // Importing the data and refresh displayed data
        this.refresh();
    }

    /**
     * Used as action, that is passed to the 'delete' buttons.
     *
     * @param  product             Product that is requested to be removed from repository.
     * @throws RepositoryException When product removal fail.
     * */
    private void removeProductFromRepository(Product product) {
        // Logging
        this.logger.info("Received removal request of product with ID {} from repository.", product.getId());

        // Removing specified product from repository
        Database.getProductRepository().removeProduct(product.getId());

        this.logger.info("Product removed successfully.");

        // Refreshing content of the panel
        this.refresh();
    }

    /**
     * Display given product at the last row.
     *
     * @param  product             Product, that will be displayed as single row.
     * @throws OutOfSpaceException When already all available rows are full of content.
     * */
    private void add(Product product) {
        // Logging
        this.logger.debug("Adding the product with ID {} to the product explorer...", product.getId());

        // Updating each text columns with corresponding content of given record
        this.idColumn.add(String.valueOf(product.getId()));
        this.nameColumn.add(product.getName());
        this.categoryColumn.add(product.getCategory());
        this.expirationDateColumn.add(product.getExpirationDate().toString());
        this.deleteButtonsColumn.add(this.deleteButtonTitle, event -> this.removeProductFromRepository(product));

        this.logger.debug("Product added successfully.");
    }

    /**
     * Remove all displayed content.
     * */
    private void clear() {
        // Logging
        this.logger.debug("Erasing the columns content...");

        // Removing all rows from every text column
        this.idColumn.clear();
        this.nameColumn.clear();
        this.categoryColumn.clear();
        this.expirationDateColumn.clear();
        this.deleteButtonsColumn.clear();

        // Logging
        this.logger.debug("Content of columns erased.");
    }

    /**
     * Import the data and refresh displayed content.
     *
     * @throws RepositoryException When import of the data from repository fail.
     * */
    public void refresh() {
        // Logging
        this.logger.info("Refreshing the panel...");

        // Erasing the content of columns
        this.clear();

        // Computing from which index records should be imported
        this.logger.debug("Importing products requested to be displayed from repository...");

        long startIndex = this.page * this.numberOfRows;
        int numberOfRecordsToImport = this.numberOfRows + 1;    // Importing one additional record to determine
                                                                // if moving to the next page makes any sense
        // Importing products from repository
        List<Product> importedProducts = Database.getProductRepository().getProducts(startIndex, numberOfRecordsToImport);

        this.logger.debug("Products imported successfully.");

        // Checking if the current page is empty ex. due to the meanwhile product removal from repository
        if (importedProducts.isEmpty()) {
            // Moving to the previous page if possible
            this.logger.debug("No records to be displayed on current page" +
                    " - checking if moving to the previous page is possible...");

            if (this.page > 0) {
                this.logger.debug("Moving to the previous page possible - performing.");

                this.moveToPreviousPage();
                return;
            }
            else {
                this.logger.debug("Moving to the previous page not possible.");
            }
        }

        // Determining if there are some records, that can be displayed on the next page
        List<Product> toUpdate; // List of records, that content will be put into columns

        logger.debug("Determining if displaying of nex page is still possible...");

        if (importedProducts.size() < numberOfRecordsToImport) {                    // If there is no records to display on next page
            this.logger.debug("Displaying next page impossible " +
                    "- disabling 'next page' button ");

            this.nextPageButton.setEnabled(false);                                  // Disable the navigation button
            toUpdate = importedProducts;                                            // And display content of all imported records
        }
        else {                                                                      // If there is at least one record that can be displayed on next page
            this.logger.debug("Displaying next page still possible" +
                    " - enabling 'next page' button ");

            toUpdate = importedProducts.subList(0, numberOfRecordsToImport - 1);    // Display all imported records besides the last one
            this.nextPageButton.setEnabled(true);                                   // And make sure that navigation button is enabled
        }

        // Adding records to the columns
        for (Product product: toUpdate) {
            this.add(product);
        }

        // Logging
        this.logger.info("Panel successfully refreshed.");
    }

    /**
     * Moves displayed explorer content to the next page.
     * */
    private void moveToNextPage() {
        // Logging
        this.logger.info("Moving to the next page...");

        this.page += 1;                             // Increment page counter
        this.previousPageButton.setEnabled(true);   // Make sure that 'previous page' button is enabled
        this.refresh();                             // Import the data and refresh displayed content

        this.logger.info("Next page loaded successfully.");
    }

    /**
     * Moves displayed explorer content to the previous page.
     * */
    private void moveToPreviousPage() {
        // Logging
        this.logger.info("Moving to the previous page...");

        this.page -= 1;                                                 // Decrement page counter
        if (this.page == 0) this.previousPageButton.setEnabled(false);  // If it is first page disable 'previous page' button
        this.refresh();                                                 // Import the data and refresh displayed content

        this.logger.info("Previous page loaded successfully.");
    }
}
