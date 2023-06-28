package pl.jakubmiodunka.gui.panels;

import pl.jakubmiodunka.database.Database;
import pl.jakubmiodunka.database.repositories.exceptions.ForbiddenOperationException;
import pl.jakubmiodunka.database.repositories.exceptions.RepositoryException;
import pl.jakubmiodunka.gui.panels.interfaces.RefreshablePanel;
import pl.jakubmiodunka.gui.panels.models.config.CategoryAdderConfig;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JOptionPane;

/**
 * A panel that allows the user to add new product categories to the repository.
 *
 * As product adder panel can be treated as more advanced version of categories adder panel
 * this class was designed in the way, that makes inheriting its functionalities easier by other similar classes.
 *
 * @author Jakub Miodunka
 * */
public class CategoryAdder extends JPanel {
    // Components used on the panel
    protected final JTextField nameTextField;
    protected final JButton addButton;

    // List of panels that will be refreshed when new category will be added to repository
    protected final List<RefreshablePanel> dependentPanels;

    // Internally used logger
    protected final Logger logger;

    /**
     * @param  config   Configuration of the panel, that will be used during initialisation.
     * */
    protected CategoryAdder(CategoryAdderConfig config) {
        // Parent class constructor call
        super();

        // Initialising logger
        this.logger = LoggerFactory.getLogger(this.getClass());

        // Preparing components
        this.nameTextField = new JTextField();
        this.addButton = new JButton(config.getAddButtonTitle());

        // Initialising other properties
        this.dependentPanels = new ArrayList<>();
    }

    /**
     * Creates new label filled with given text aligned to the center.
     *
     * @param title Text, that will be placed inside created label.
     * @return      Label with given text align to the center.
     * */
    protected static JLabel titleLabelFactory(String title) {
        JLabel newTitleLabel = new JLabel(title);
        newTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        return newTitleLabel;
    }

    /**
     * Sets the layout of categories adder panel.
     * Meant to be used only once during instance initialisation.
     *
     * @param  config   Configuration of the panel, that will be used during layout setting process.
     * */
    private void setLayout(CategoryAdderConfig config) {
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

        for (int spacerCounter = 0; spacerCounter < 7; spacerCounter++) {
            centerPanel.add(new JLabel());  // Spacer
        }

        this.add(centerPanel, JLabel.CENTER);

        // Adding the 'add' button as a footer
        this.add(this.addButton, BorderLayout.SOUTH);
    }

    /**
     * Adds a product category, which name is currently stored in 'name' text field to repository.
     *
     * @throws RepositoryException         When adding new product to repository fail.
     * */
    private void addCategory() {
        // Logging
        this.logger.info("Adding new product category to repository...");

        // Getting the category name from text box
        String name = this.nameTextField.getText();

        // Checking the name validity
        if (name.isEmpty()) {
            // Logging
            String errorMessage = "Given category name is an empty string.";
            this.logger.error(errorMessage);

            // Showing pop-up window and exiting
            JOptionPane.showMessageDialog(this, errorMessage, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Adding product category to repository
        try {
            Database.getCategoriesRepository().addNewCategory(name);
        } catch (ForbiddenOperationException exception) {
            String errorMessage = "Category with provided name already exists.";

            // Showing pop-up window and exiting
            JOptionPane.showMessageDialog(this, errorMessage, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Logging
        this.logger.info("New product category named as '{}' successfully added to repository.", name);

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
        this.addButton.addActionListener(event -> this.addCategory());
    }

    /**
     * Factory method, meant to be used externally to create new class instances.
     *
     * @param config               Configuration of the panel, that will be used during initialisation.
     * */
    public static CategoryAdder getNewPanel(CategoryAdderConfig config) {
        // Creating new categories adder panel
        CategoryAdder newPanel = new CategoryAdder(config);

        // Further panel initialisation
        newPanel.setLayout(config);
        newPanel.setAddButtonFunction();

        // Returning created panel as ready to use
        return newPanel;
    }

    /**
     * Adds given panel to the pool of panels, that will be refreshed after each addition of record to repository.
     *
     * @param panel Refreshable panel, which state is dependent on the action of adding the record to repository
     *              by the instance of this class.
     * */
    public void addDependentPanel(RefreshablePanel panel) {
        this.dependentPanels.add(panel);
    }
}
