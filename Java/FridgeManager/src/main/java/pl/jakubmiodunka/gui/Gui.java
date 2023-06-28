package pl.jakubmiodunka.gui;

import pl.jakubmiodunka.gui.panels.CategoryAdder;
import pl.jakubmiodunka.gui.panels.CategoryBrowser;
import pl.jakubmiodunka.gui.panels.models.config.*;
import pl.jakubmiodunka.gui.panels.ProductAdder;
import pl.jakubmiodunka.gui.panels.ProductBrowser;

import java.awt.BorderLayout;
import javax.swing.*;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for handling of app GUI.
 * GUI will be initialised in moment, when this class will be instantiated.
 *
 * @author Jakub Miodunka
 * */
public class Gui extends JFrame {
    // Panels in product explorer mode
    private final JPanel productExplorerModeLeftPanel;
    private final JPanel productExplorerModeCenterPanel;

    // Panels in category explorer mode
    private final JPanel categoryExplorerModeLeftPanel;
    private final JPanel categoryExplorerModeCenterPanel;

    // Internally used logger
    private final Logger logger;

    /**
     * @param  config Configuration of the GUI, that will be used during initialisation.
     * */
    public Gui(GuiConfig config) {
        // Parent class constructor call
        super();

        // Initialising logger
        this.logger = LoggerFactory.getLogger(Gui.class);

        // Preparing panels used in product explorer mode
        Path productBrowserConfigXml = Path.of("src/main/resources/config/gui/panels/productBrowser.xml");
        this.logger.debug("Creating product browser panel configuration model using '{}' file...", productBrowserConfigXml);
        ProductBrowserConfig productBrowserConfig = new ProductBrowserConfig(productBrowserConfigXml);
        this.logger.debug("Product browser panel configuration model successfully created.");

        this.logger.info("Creating product browser panel...");
        ProductBrowser productBrowser = ProductBrowser.getNewPanel(productBrowserConfig);
        this.logger.info("Product browser panel successfully created.");
        this.productExplorerModeCenterPanel = productBrowser;

        Path productAdderConfigXml = Path.of("src/main/resources/config/gui/panels/productAdder.xml");
        this.logger.debug("Creating product adder panel configuration model using '{}' file...", productAdderConfigXml);
        ProductAdderConfig productAdderConfig = new ProductAdderConfig(productAdderConfigXml);
        this.logger.debug("Product adder panel configuration model successfully created.");

        this.logger.info("Creating product adder panel...");
        ProductAdder productAdder = ProductAdder.getNewPanel(productAdderConfig);
        productAdder.addDependentPanel(productBrowser);
        this.logger.info("Product adder panel successfully created.");
        this.productExplorerModeLeftPanel = productAdder;

        // Preparing panels used in categories explorer mode
        Path categoryBrowserConfigXml = Path.of("src/main/resources/config/gui/panels/categoryBrowser.xml");
        this.logger.debug("Creating category browser panel configuration model using '{}' file...", categoryBrowserConfigXml);
        CategoryBrowserConfig categoryBrowserConfig = new CategoryBrowserConfig(categoryBrowserConfigXml);
        this.logger.debug("Category browser panel configuration model successfully created.");

        this.logger.info("Creating category browser panel...");
        CategoryBrowser categoryBrowser = CategoryBrowser.getNewPanel(categoryBrowserConfig);
        categoryBrowser.addDependentPanel(productAdder);
        this.logger.info("Category browser panel successfully created.");
        this.categoryExplorerModeCenterPanel = categoryBrowser;

        Path categoryAdderConfigXml = Path.of("src/main/resources/config/gui/panels/categoryAdder.xml");
        this.logger.debug("Creating category adder panel configuration model using '{}' file...", categoryAdderConfigXml);
        CategoryAdderConfig categoryAdderConfig = new CategoryAdderConfig(categoryAdderConfigXml);
        this.logger.debug("Category adder panel configuration model successfully created.");

        this.logger.info("Creating category adder panel...");
        CategoryAdder categoryAdder = CategoryAdder.getNewPanel(categoryAdderConfig);
        categoryAdder.addDependentPanel(categoryBrowser);
        categoryAdder.addDependentPanel(productAdder);
        this.logger.info("Category adder panel successfully created.");
        this.categoryExplorerModeLeftPanel = categoryAdder;

        // Setting up frame properties
        this.setTitle(config.getTitle());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(config.getWidth(), config.getHeight());

        // Adding menu bar
        JMenu modeMenu = new JMenu(config.getModeMenuTitle());

        JMenuItem productExplorerSwitch = new JMenuItem(config.getProductExplorerSwitchTitle());
        productExplorerSwitch.addActionListener(event -> this.setProductExplorerMode());
        modeMenu.add(productExplorerSwitch);

        JMenuItem categoryExplorerSwitch = new JMenuItem(config.getCategoryExplorerSwitchTitle());
        categoryExplorerSwitch.addActionListener(event -> this.setCategoryExplorerMode());
        modeMenu.add(categoryExplorerSwitch);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(modeMenu);

        this.setJMenuBar(menuBar);

        // Setting the GUI to product explorer mode as default
        this.setProductExplorerMode();

        // Making frame visible
        this.setVisible(true);
    }

    /**
     * Switches the GUI to product explorer mode.
     * */
    private void setProductExplorerMode() {
        this.logger.info("Switching the GUI to product explorer mode...");

        // Removing components related to category explorer mode
        this.remove(this.categoryExplorerModeCenterPanel);
        this.remove(this.categoryExplorerModeLeftPanel);

        // Adding components related to product explorer mode
        this.add(this.productExplorerModeCenterPanel, BorderLayout.CENTER);
        this.add(this.productExplorerModeLeftPanel, BorderLayout.WEST);

        // Updating displayed content
        this.validate();
        this.repaint();

        this.logger.info("GUI successfully switched to product explorer mode.");
    }

    /**
     * Switches the GUI to category explorer mode.
     * */
    private void setCategoryExplorerMode() {
        // Logging
        this.logger.info("Switching the GUI to category explorer mode...");

        // Removing components related to product explorer mode
        this.remove(this.productExplorerModeCenterPanel);
        this.remove(this.productExplorerModeLeftPanel);

        // Adding components related to category explorer mode
        this.add(this.categoryExplorerModeCenterPanel, BorderLayout.CENTER);
        this.add(this.categoryExplorerModeLeftPanel, BorderLayout.WEST);

        // Updating displayed content
        this.validate();
        this.repaint();

        this.logger.info("GUI successfully switched to category explorer mode.");
    }
}
