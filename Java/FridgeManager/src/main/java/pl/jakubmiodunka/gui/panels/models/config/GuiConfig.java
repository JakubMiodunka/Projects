package pl.jakubmiodunka.gui.panels.models.config;

import pl.jakubmiodunka.exceptions.ConfigFileParsingException;
import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;
import pl.jakubmiodunka.utilities.xml.XmlUtilities;

import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * Model of GUI panel configuration, that can be used for initialising it.
 *
 * @author Jakub Miodunka
 * */
public class GuiConfig {
    // Gui window title
    private final String title;

    // Gui windows dimensions
    private final int width;
    private final int height;

    // Titles used in menu bar
    private final String modeMenuTitle;
    private final String productExplorerSwitchTitle;
    private final String categoryExplorerSwitchTitle;

    /**
     * @param  configXmlPath              Path to config XML file containing GUI configuration.
     * @throws ConfigFileParsingException When extraction of data from provided config XML file fail.
     * */
    public GuiConfig(Path configXmlPath) {
        try {
            // Extracting root node from given file
            Element rootElement = XmlUtilities.getRootNode(configXmlPath, "gui");

            // Extracting sub-nodes
            Element sizeNode = XmlUtilities.getNode(rootElement, "size");
            Element menuBarNode = XmlUtilities.getNode(rootElement, "menuBar");
            Element modeMenuNode = XmlUtilities.getNode(menuBarNode, "modeMenu");
            Element modeSwitchesNode = XmlUtilities.getNode(modeMenuNode, "switches");

            // Properties init
            this.title = XmlUtilities.getContentOfNode(rootElement, "title");
            this.width = Integer.parseInt(XmlUtilities.getContentOfNode(sizeNode, "width"));
            this.height = Integer.parseInt(XmlUtilities.getContentOfNode(sizeNode, "height"));
            this.modeMenuTitle = XmlUtilities.getContentOfNode(modeMenuNode, "title");
            this.productExplorerSwitchTitle = XmlUtilities.getContentOfNode(modeSwitchesNode, "productExplorerSwitchTitle");
            this.categoryExplorerSwitchTitle = XmlUtilities.getContentOfNode(modeSwitchesNode, "categoryExplorerSwitchTitle");

        } catch (XmlParsingException | NumberFormatException exception) {
            // Exception wrapping
            String errorMessage = "Failed to create panel configuration model using '" + configXmlPath + "' file.";
            throw new ConfigFileParsingException(errorMessage, exception);
        }
    }

    /**
     * @return GUI window title.
     * */
    public String getTitle() {
        return title;
    }

    /**
     * @return GUI window width.
     * */
    public int getWidth() {
        return width;
    }

    /**
     * @return GUI window height.
     * */
    public int getHeight() {
        return height;
    }

    /**
     * @return Mode menu title.
     * */
    public String getModeMenuTitle() {
        return modeMenuTitle;
    }

    /**
     * @return Title of item in mode menu, responsible for switching GUI to product explorer mode.
     * */
    public String getProductExplorerSwitchTitle() {
        return productExplorerSwitchTitle;
    }

    /**
     * @return Title of item in mode menu, responsible for switching GUI to category explorer mode.
     * */
    public String getCategoryExplorerSwitchTitle() {
        return categoryExplorerSwitchTitle;
    }
}
