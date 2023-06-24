package pl.jakubmiodunka.gui.panels;

import pl.jakubmiodunka.database.Database;
import pl.jakubmiodunka.database.models.content.Product;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;
import pl.jakubmiodunka.gui.panels.exceptions.OutOfSpaceException;
import pl.jakubmiodunka.gui.panels.models.config.ProductBrowserConfig;
import pl.jakubmiodunka.gui.panels.utilities.LabelColumn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;

/**
 * Panel, where product stored in repository can be browsed.
 * Extends category browser as some properties are common in
 * both cases - for more details refer to docstrings placed in CategoriesBrowser class.
 *
 * @author Jakub Miodunka
 * */
public class ProductBrowser extends CategoryBrowser {
    // Additional columns, where content of given records is presented
    private final LabelColumn categoryColumn;
    private final LabelColumn expirationDateColumn;

    /**
     * @param config Configuration of the panel, that will be used during initialisation.
     * */
    protected ProductBrowser(ProductBrowserConfig config) {
        // Parent class constructor call
        super(config);

        // Creating additional columns
        this.categoryColumn = new LabelColumn(this.numberOfRows, config.getCategoryColumnTitle());
        this.expirationDateColumn = new LabelColumn(this.numberOfRows, config.getExpirationDateColumnTitle());
    }

    /**
     * Sets the layout of product browser panel.
     * Meant to be used only once during instance initialisation.
     * */
    @Override
    protected void setLayout() {
        // Setting the panel layout
        this.setLayout(new BorderLayout());

        // Preparing sub-panel, where details about imported records will be displayed
        JPanel columnPanel = new JPanel();
        columnPanel.setLayout(new GridLayout(1, 5));
        columnPanel.add(this.idColumn);
        columnPanel.add(this.nameColumn);
        columnPanel.add(this.categoryColumn);
        columnPanel.add(this.expirationDateColumn);
        columnPanel.add(this.deleteButtonsColumn);
        this.add(columnPanel, BorderLayout.CENTER);

        // Preparing sub-panel, where navigation buttons will be placed
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new GridLayout(1, 2));
        navigationPanel.add(this.nextPageButton);
        navigationPanel.add(this.previousPageButton);
        this.add(navigationPanel, BorderLayout.SOUTH);
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
     * Displays given product at the last row.
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
        this.deleteButtonsColumn.add(this.deleteButtonsTitle, event -> this.removeProductFromRepository(product));

        this.logger.debug("Product added successfully.");
    }

    /**
     * Remove all displayed content.
     * */
    @Override
    protected void clear() {
        // Logging
        this.logger.debug("Erasing the columns content...");

        // Removing all rows from every column
        // Super.clear not used due to logg formatting issues (message duplication).
        this.idColumn.clear();
        this.nameColumn.clear();
        this.deleteButtonsColumn.clear();
        this.categoryColumn.clear();
        this.expirationDateColumn.clear();

        // Logging
        this.logger.debug("Content of columns erased.");
    }

    /**
     * Import the data and refresh displayed content.
     *
     * @throws RepositoryException When import of the data from repository fail.
     * */
    @Override
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
            this.logger.info("No records to be displayed on current page" +
                    " - checking if moving to the previous page is possible...");

            if (this.page > 0) {
                this.logger.info("Moving to the previous page possible - performing.");

                this.moveToPreviousPage();
                return;
            }
            else {
                this.logger.warn("Moving to the previous page not possible - product repository is empty.");
            }
        }

        // Determining if there are some records, that can be displayed on the next page
        List<Product> toUpdate; // List of records, that content will be put into columns

        logger.debug("Determining if displaying of nex page is still possible...");

        if (importedProducts.size() < numberOfRecordsToImport) {                    // If there is no records to display on next page
            this.logger.debug("Displaying next page impossible " +
                    "- disabling 'next page' button.");

            this.nextPageButton.setEnabled(false);                                  // Disable the navigation button
            toUpdate = importedProducts;                                            // And display content of all imported records
        }
        else {                                                                      // If there is at least one record that can be displayed on next page
            this.logger.debug("Displaying next page still possible" +
                    " - enabling 'next page' button.");

            toUpdate = importedProducts.subList(0, numberOfRecordsToImport - 1);    // Display all imported records besides the last one
            this.nextPageButton.setEnabled(true);                                   // And make sure that navigation button is enabled
        }

        // Adding records to the columns
        this.logger.debug("Adding imported products to the displayed table...");

        for (Product product: toUpdate) {
            this.add(product);
        }

        this.logger.debug("Products successfully added to the table.");

        // Logging
        this.logger.info("Panel successfully refreshed.");
    }

    /**
     * Factory method, meant to be used externally to create product browser instances.
     *
     * @param config Configuration of the panel, that will be used during initialisation.
     * */
    public static ProductBrowser getNewPanel(ProductBrowserConfig config) {
        // Creating new product browser panel
        ProductBrowser newPanel = new ProductBrowser(config);

        // Further panel initialisation
        newPanel.setLayout();
        newPanel.setNavigationButtonsFunctions();
        newPanel.refresh();

        // Returning created panel as ready to use
        return newPanel;
    }
}
