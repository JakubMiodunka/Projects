package pl.jakubmiodunka;

import pl.jakubmiodunka.database.Database;
import pl.jakubmiodunka.gui.Gui;
import pl.jakubmiodunka.gui.panels.models.config.GuiConfig;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLogger;

/**
 * Program entry point.
 *
 * @author Jakub Miodunka
 * */
public class Main {
    public static void main(String[] args) {
        // Setting logging level
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        // Initialising logger
        Logger logger = LoggerFactory.getLogger(Main.class);

        // Initialising backend core functionalities
        logger.info("Initialising backed core functionalities...");
        Database.initialise();
        logger.info("Initialisation of backend core functionalities successfully.");

        // Preparing program GUI
        Path guiConfigXml = Path.of("src/main/resources/config/gui/gui.xml");
        logger.debug("Creating GUI configuration model using '{}' file...", guiConfigXml);
        GuiConfig guiConfig = new GuiConfig(guiConfigXml);
        logger.debug("GUI configuration model successfully created.");

        logger.info("Preparing program GUI...");
        new Gui(guiConfig);
        logger.info("Preparation of program GUI successful.");
    }
}