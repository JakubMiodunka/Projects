package pl.jakubmiodunka.gui.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jakubmiodunka.database.Database;
import pl.jakubmiodunka.database.models.content.Category;
import pl.jakubmiodunka.database.repositories.exceptions.ForbiddenOperationException;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;
import pl.jakubmiodunka.gui.panels.exceptions.OutOfSpaceException;
import pl.jakubmiodunka.gui.panels.interfaces.RefreshablePanel;
import pl.jakubmiodunka.gui.panels.models.config.CategoryBrowserConfig;
import pl.jakubmiodunka.gui.panels.utilities.ButtonColumn;
import pl.jakubmiodunka.gui.panels.utilities.LabelColumn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.*;

/**
 * Panel, where product categories currently stored in repository can be browsed.
 *
 * As product browser can be treated as more advanced version of categories browser due to the similarities between
 * the product and category models, this class was designed in the way, that makes inheriting its functionalities
 * easier by other similar classes.
 *
 * @author Jakub Miodunka
 * */
public class CategoryBrowser extends JPanel implements RefreshablePanel {
    // Column where content of given records is presented
    protected final LabelColumn idColumn;
    protected final LabelColumn nameColumn;
    protected final ButtonColumn deleteButtonsColumn;

    // Number of rows in each column
    protected final int numberOfRows;

    // Title of each button in delete buttons column
    protected final String deleteButtonsTitle;

    // Buttons used for navigation between pages
    protected final JButton nextPageButton;
    protected final JButton previousPageButton;

    // Current page number
    protected long page;

    // Internally used logger
    protected final Logger logger;

    /**
     * @param config Configuration of the panel, that will be used during initialisation.
     * */
    protected CategoryBrowser(CategoryBrowserConfig config) {
        // Parent class constructor call
        super();

        // Initialising logger
        this.logger = LoggerFactory.getLogger(this.getClass());

        // Basic properties init
        this.numberOfRows = config.getNumberOfRows();
        this.deleteButtonsTitle = config.getDeleteButtonsTitle();

        // Creating columns
        this.idColumn = new LabelColumn(this.numberOfRows, config.getIdColumnTitle());
        this.nameColumn = new LabelColumn(this.numberOfRows, config.getNameColumnTitle());
        this.deleteButtonsColumn = new ButtonColumn(this.numberOfRows, config.getDeleteButtonsColumnTitle());

        // Creating buttons
        this.nextPageButton = new JButton(config.getNextPageButtonTitle());
        this.previousPageButton = new JButton(config.getPreviousPageButtonTitle());

        // Initialising things related with paging
        this.page = 0;                              // Initially set to first page
        this.previousPageButton.setEnabled(false);  // Initially disabled as moving to page -1 makes no sense
    }

    /**
     * Sets the layout of categories browser panel.
     * Meant to be used only once during instance initialisation.
     * */
    protected void setLayout() {
        // Setting the panel layout
        this.setLayout(new BorderLayout());

        // Preparing sub-panel, where details about imported records will be displayed
        JPanel columnPanel = new JPanel();
        columnPanel.setLayout(new GridLayout(1, 5));
        columnPanel.add(this.idColumn);
        columnPanel.add(this.nameColumn);
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
     * Moves displayed explorer content to the next page.
     *
     * @throws RepositoryException When import of the data from repository fail.
     * */
    protected void moveToNextPage() {
        // Logging
        this.logger.info("Moving to the next page...");

        this.page += 1;                             // Increment page counter
        this.previousPageButton.setEnabled(true);   // Make sure that 'previous page' button is enabled
        this.refresh();                             // Import the data and refresh displayed content

        this.logger.info("Next page loaded successfully.");
    }

    /**
     * Moves displayed explorer content to the previous page.
     *
     * @throws RepositoryException When import of the data from repository fail.
     * */
    protected void moveToPreviousPage() {
        // Logging
        this.logger.info("Moving to the previous page...");

        this.page -= 1;                                                 // Decrement page counter
        if (this.page == 0) this.previousPageButton.setEnabled(false);  // If it is first page disable 'previous page' button
        this.refresh();                                                 // Import the data and refresh displayed content

        this.logger.info("Previous page loaded successfully.");

    }

    /**
     * Assigns functionalities to navigation buttons.
     * Meant to be used only once during instance initialisation.
     * */
    protected void setNavigationButtonsFunctions() {
        // Assigning functionalities to navigation buttons
        this.nextPageButton.addActionListener(event -> this.moveToNextPage());
        this.previousPageButton.addActionListener(event -> this.moveToPreviousPage());
    }

    /**
     * Used as action, that is passed to the 'delete' buttons.
     *
     * @param  category            Product category, that is requested to be removed from repository.
     * @throws RepositoryException When category removal fail.
     * */
    private void removeCategoryFromRepository(Category category) {
        // Logging
        this.logger.info("Received removal request of product category with ID {} from repository.", category.getId());

        // Removing specified product from repository
        try {
            Database.getCategoriesRepository().removeCategory(category.getId());
        } catch (ForbiddenOperationException exception) {
            // Logging
            String errorMessage = "Unable to remove specified category as there are still some products assigned to it.";
            this.logger.warn(errorMessage);

            // Showing pop-up window and exiting
            JOptionPane.showMessageDialog(this, errorMessage, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.logger.info("Product category removed successfully.");

        // Refreshing content of the panel
        this.refresh();
    }

    /**
     * Displays given product category at the last row.
     *
     * @param  category            Product category, that will be displayed as single row.
     * @throws OutOfSpaceException When already all available rows are full of content.
     * */
    private void add(Category category) {
        // Logging
        this.logger.debug("Adding the product category with ID {} to the category browser...", category.getId());

        // Updating each text columns with corresponding content of given record
        this.idColumn.add(String.valueOf(category.getId()));
        this.nameColumn.add(category.getName());
        this.deleteButtonsColumn.add(this.deleteButtonsTitle, event -> this.removeCategoryFromRepository(category));

        this.logger.debug("Product category successfully added.");
    }

    /**
     * Remove all displayed content.
     * */
    protected void clear() {
        // Logging
        this.logger.debug("Erasing the columns content...");

        // Removing all rows from every column
        this.idColumn.clear();
        this.nameColumn.clear();
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

        // Importing product categories from repository
        this.logger.debug("Importing product categories requested to be displayed from repository...");

        List<Category> importedCategories = Database.getCategoriesRepository().getAllCategories();

        this.logger.debug("Products imported successfully.");

        // Checking if there is at least one product category in repository
        if (importedCategories.isEmpty()) {
            // Logging
            this.logger.warn("No categories available in repository - aborting the panel refresh.");

            // Exiting as further methods execution makes no sense
            return;
        }

        // Determining from which and to which index, list of imported categories should be sliced, to match the number
        // of currently displayed page
        int startIndex = (int)(this.page * this.numberOfRows);  // Inclusive. Long to int mapping potentially hazardous,
                                                                // but List.subList() method accept integers only.
        int endIndex = startIndex + this.numberOfRows;          // Exclusive

        // Determining if 'next page' navigation button should be enabled
        if (endIndex >= importedCategories.size() ) {          // Check if page requested to be displayed is the last one
            this.logger.debug("Displaying next page impossible " +
                    "- disabling 'next page' button.");

            this.nextPageButton.setEnabled(false);             // If yes disable the navigation button
            endIndex = importedCategories.size();              // And make sure that the end index does not exceed
        }                                                      // the size of categories list.
        else {
            // Logging
            this.logger.debug("Displaying next page still possible" +
                    " - enabling 'next page' button.");

            this.nextPageButton.setEnabled(true);              // Keep the navigation button enabled if end index fits
        }                                                      // into the limits.

        // Detecting if categories list slicing will result with empty page
        if (startIndex == endIndex) {
            // Moving to the previous page as there is no point to display empty page
            this.logger.info("No records to be displayed on current page" +
                    " - attempting to move to the previous page.");

            this.moveToPreviousPage();
            return;
        }

        // Slicing the categories list to sub-list of records, that should be displayed on currently selected page
        List<Category> toUpdate;
        try {
            toUpdate = importedCategories.subList(startIndex, endIndex);
        } catch (IndexOutOfBoundsException exception) {
            // Logging
            String errorMessage = "Invalid indexing during categories list slicing.";   // Should not be possible
            this.logger.error(errorMessage, exception);

            // Exception wrapping
            throw new RuntimeException(errorMessage, exception);
        }

        // Adding records to the columns
        this.logger.debug("Adding imported products categories to the displayed table...");

        for (Category category: toUpdate) {
            this.add(category);
        }

        this.logger.debug("Products categories successfully added to the table.");

        // Logging
        this.logger.info("Panel successfully refreshed.");
    }

    /**
     * Factory method, meant to be used externally to create categories browser instances.
     *
     * @param config               Configuration of the panel, that will be used during initialisation.
     * @throws RepositoryException When import of the data from repository fail.
     * */
    public static CategoryBrowser getNewPanel(CategoryBrowserConfig config) {
        // Creating new categories browser panel
        CategoryBrowser newPanel = new CategoryBrowser(config);

        // Further panel initialisation
        newPanel.setLayout();
        newPanel.setNavigationButtonsFunctions();
        newPanel.refresh();

        // Returning created panel as ready to use
        return newPanel;
    }
}
