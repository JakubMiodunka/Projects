package pl.jakubmiodunka.gui;

import pl.jakubmiodunka.gui.panels.models.config.GuiConfig;
import pl.jakubmiodunka.gui.panels.models.config.ProductAdderConfig;
import pl.jakubmiodunka.gui.panels.models.config.ProductBrowserConfig;
import pl.jakubmiodunka.gui.panels.ProductAdder;
import pl.jakubmiodunka.gui.panels.ProductBrowser;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
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

    // Internally used logger
    private final Logger logger;

    /**
     * @param  config                      Configuration of the GUI, that will be used during initialisation.
     * */
    public Gui(GuiConfig config) {
        // Parent class constructor call
        super();

        // Initialising logger
        this.logger = LoggerFactory.getLogger(Gui.class);

        // Setting up frame properties
        this.setTitle(config.getTitle());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(config.getWidth(), config.getHeight());

        // Preparing panels used in product explorer mode
        Path productBrowserConfigXml = Path.of("src/main/resources/config/gui/panels/productBrowser.xml");
        this.logger.debug("Creating product browser panel configuration model using '{}' file...", productBrowserConfigXml);
        ProductBrowserConfig productBrowserConfig = new ProductBrowserConfig(productBrowserConfigXml);
        this.logger.debug("Product browser panel configuration model successfully created.");

        this.logger.info("Creating product browser panel...");
        ProductBrowser productBrowser = new ProductBrowser(productBrowserConfig);
        this.logger.info("Product browser panel successfully created.");
        this.productExplorerModeCenterPanel = productBrowser;

        Path productAdderConfigXml = Path.of("src/main/resources/config/gui/panels/productAdder.xml");
        this.logger.debug("Creating product adder panel configuration model using '{}' file...", productAdderConfigXml);
        ProductAdderConfig productAdderConfig = new ProductAdderConfig(productAdderConfigXml);
        this.logger.debug("Product adder panel configuration model successfully created.");

        this.logger.info("Creating product adder panel...");
        ProductAdder productAdder = new ProductAdder(productAdderConfig);
        productAdder.addDependentPanel(productBrowser);
        this.logger.info("Product adder panel successfully created.");
        this.productExplorerModeLeftPanel = productAdder;

        // Preparing panels used in categories explorer mode
        // TODO: Introduce categories explorer mode of GUI

        // Setting the GUI to product explorer mode
        this.setProductExplorerMode();

        // Making frame visible
        this.setVisible(true);
    }

    /**
     * Switches the GUI to product explorer mode.
     * */
    private void setProductExplorerMode() {
        this.logger.info("Switching the GUI to product explorer mode...");

        this.add(this.productExplorerModeCenterPanel, BorderLayout.CENTER);
        this.add(this.productExplorerModeLeftPanel, BorderLayout.WEST);

        this.logger.info("GUI successfully switched to product explorer mode.");
    }

}
