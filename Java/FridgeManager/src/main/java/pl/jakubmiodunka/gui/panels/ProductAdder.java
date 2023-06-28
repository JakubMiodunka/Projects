package pl.jakubmiodunka.gui.panels;

import pl.jakubmiodunka.database.Database;
import pl.jakubmiodunka.database.models.content.Category;
import pl.jakubmiodunka.database.repositories.exceptions.ForbiddenOperationException;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;
import pl.jakubmiodunka.exceptions.InvalidConfigValueException;
import pl.jakubmiodunka.gui.panels.interfaces.RefreshablePanel;
import pl.jakubmiodunka.gui.panels.models.config.ProductAdderConfig;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

/**
 * A panel that allows the user to add products to the repository.
 *
 * Extends category adder panel as some properties are common in
 * both cases - for more details refer to docstrings placed in CategoryAdder class.
 *
 * @author Jakub Miodunka
 * */
public class ProductAdder extends CategoryAdder implements RefreshablePanel {
    // Additional components used on the panel
    private final JComboBox<String> categoriesComboBox;
    private final JTextField expirationDateTextField;

    // Date formatter used to convert timestamp given by the user to LocalDate objects
    private final DateTimeFormatter expirationDateFormatter;

    // List where all available in repository product categories are stored
    private List<Category> allCategories;

    /**
     * @param  config                      Configuration of the panel, that will be used during initialisation.
     * @throws InvalidConfigValueException When product expiration date format given in config is invalid.
     * */
    protected ProductAdder(ProductAdderConfig config) {
        // Parent class constructor call
        super(config);

        // Preparing additional components
        this.expirationDateTextField = new JTextField();
        this.categoriesComboBox = new JComboBox<>();

        // Initialising other additional properties
        try {
            this.expirationDateFormatter = DateTimeFormatter.ofPattern(config.getExpirationDateFormat());
        } catch (IllegalArgumentException exception) {
            // Exception wrapping
            throw new InvalidConfigValueException("Invalid format of product expiration date given.", exception);
        }
    }

    /**
     * Sets the layout of product adder panel.
     * Meant to be used only once during instance initialisation.
     *
     * @param  config   Configuration of the panel, that will be used during layout setting process.
     * */
    private void setLayout(ProductAdderConfig config) {
        // Setting the panel layout
        this.setLayout(new BorderLayout());

        // Adding the panel title
        this.add(titleLabelFactory(config.getPanelTitle()), BorderLayout.NORTH);

        // Creating sub-panel, where components are placed
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(10, 1));   // Additional rows added to keep
        // the proportions in panel apprentice
        centerPanel.add(titleLabelFactory(config.getRecordTextFieldTitle()));
        centerPanel.add(this.nameTextField);
        centerPanel.add(new JLabel());    // Spacer

        centerPanel.add(titleLabelFactory(config.getCategoriesComboBoxTitle()));
        centerPanel.add(this.categoriesComboBox);
        centerPanel.add(new JLabel());    // Spacer

        centerPanel.add(titleLabelFactory(config.getExpirationDateTextFieldTitle()));
        centerPanel.add(this.expirationDateTextField);
        centerPanel.add(new JLabel());    // Spacer

        this.add(centerPanel, JLabel.CENTER);

        // Adding the 'add' button as a footer
        this.add(this.addButton, BorderLayout.SOUTH);
    }

    /**
     * Adds a product, which properties are currently stored in panel components to repository.
     *
     * @throws RepositoryException         When adding new product to repository fail.
     * @throws RuntimeException            When some unexpected error occur.
     * */
    private void addProduct() {
        // Logging
        this.logger.info("Adding new product to repository...");

        // Getting the product name from text box
        String name = this.nameTextField.getText();

        // Checking the name validity
        if (name.isEmpty()) {
            // Logging
            String errorMessage = "Given product name is an empty string.";
            this.logger.error(errorMessage);

            // Showing pop-up window and exiting
            JOptionPane.showMessageDialog(this, errorMessage, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Getting product category from combo box
        String categoryName = (String) this.categoriesComboBox.getSelectedItem();
        long categoryId;
        try {
            categoryId = this.allCategories.stream()
                    .filter(category -> category.getName().equals(categoryName))
                    .map(Category::getId)
                    .findFirst()    // Assuming that categories have unique names
                    .orElseThrow();
        } catch (NoSuchElementException exception) {
            // Logging
            String errorMessage = "State of categories combo box not synchronised with internal categories list.";
            this.logger.error(errorMessage);

            // Throwing an exception
            throw new RuntimeException(errorMessage, exception);   // Should not be possible
        }

        // Getting product expiration date from text box
        String expirationDateAsString = this.expirationDateTextField.getText();

        // Converting given timestamp to LocalDate object
        LocalDate expirationDate;
        try {
            expirationDate = LocalDate.parse(expirationDateAsString, this.expirationDateFormatter);
        } catch (DateTimeParseException exception) {
            // Logging
            String errorMessage = "Given expiration date not in valid format.";
            this.logger.error(errorMessage);

            // Showing pop-up window and exiting
            JOptionPane.showMessageDialog(this, errorMessage, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Adding product to repository
        try {
            Database.getProductRepository().addNewProduct(name, categoryId, expirationDate);
        } catch (ForbiddenOperationException exception) {
            // Logging
            String errorMessage = "Invalid category selected.";
            this.logger.error(errorMessage);

            // Exception wrapping
            throw new RuntimeException(errorMessage, exception);    // Should not be possible
        }

        // Logging
        this.logger.info("New product successfully added to repository.");

        // Refreshing panels, that are dependent on performed action
        this.logger.debug("Refreshing the panels, that are dependent on performed action...");

        for (RefreshablePanel panel: this.dependentPanels) {
            panel.refresh();
        }

        this.logger.debug("All panels refreshed successfully.");
    }

    /**
     * Assigns functionalities to 'add' button placed in the panel footer.
     * Meant to be used only once during instance initialisation.
     * */
    private void setAddButtonFunction() {
        this.addButton.addActionListener(event -> this.addProduct());
    }

    /**
     * Import the data and refresh displayed content.
     *
     * @throws RepositoryException When import of the data from repository fail.
     * */
    public void refresh() {
        // Logging
        this.logger.info("Refreshing the panel...");

        // Updating the locally stored list of all available categories
        this.logger.debug("Filling internal categories list with all categories available in repository...");

        this.allCategories = Database.getCategoriesRepository().getAllCategories();

        this.logger.debug("Internal categories list filled.");

        // Updating the categories combo box
        this.logger.debug("Synchronising categories combo box with internal categories list...");

        this.categoriesComboBox.removeAllItems();
        this.allCategories.forEach(category -> this.categoriesComboBox.addItem(category.getName()));

        // Logging
        this.logger.debug("Synchronisation successful.");
        this.logger.info("Panel successfully refreshed.");
    }

    /**
     * Factory method, meant to be used externally to create new class instances.
     *
     * @param config               Configuration of the panel, that will be used during initialisation.
     * */
    public static ProductAdder getNewPanel(ProductAdderConfig config) {
        // Creating new categories adder panel
        ProductAdder newPanel = new ProductAdder(config);

        // Further panel initialisation
        newPanel.setLayout(config);
        newPanel.setAddButtonFunction();
        newPanel.refresh();

        // Returning created panel as ready to use
        return newPanel;
    }
}
