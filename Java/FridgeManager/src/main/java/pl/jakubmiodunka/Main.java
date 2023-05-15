package pl.jakubmiodunka;

import pl.jakubmiodunka.database.Database;

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

        // Initialising backend core functionalities
        Database.initialise();

        // Place for some other actions...
    }
}