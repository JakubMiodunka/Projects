package pl.jakubmiodunka.gui.panels.utilities;

import pl.jakubmiodunka.gui.panels.exceptions.OutOfSpaceException;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Representation of column placed in explorer window, which stores text content.
 *
 * @author Jakub Miodunka
 * */
public class LabelColumn extends JPanel {
    // List where labels containing each row content are stored
    private final List<JLabel> labels;

    /**
     * @param rows     Number of rows in column (without title row).
     * @param title    Column title.
     * */
    public LabelColumn(int rows, String title) {
        // Parent class constructor call
        super();

        // Setting position and layout of the column
        this.setLayout(new GridLayout(rows + 1, 1)); // Additional row required for placing column title

        // Adding column title
        JLabel titleLabel = new JLabel();                      // Title label creation
        titleLabel.setText(title);                             // Setting specified title
        titleLabel.setHorizontalAlignment(JLabel.CENTER);      // Title centering
        this.add(titleLabel);                                  // Adding title label as component to the panel

        // Rows initialisation
        this.labels = new ArrayList<>();                       // Rows list creation

        // Rows creation
        for (int rowCounter = 0; rowCounter< rows; rowCounter++) {
            JLabel newRow = new JLabel();                      // Row creation
            newRow.setHorizontalAlignment(JLabel.CENTER);      // Row content centering
            this.labels.add(newRow);                           // Adding row to the rows list
        }

        // Adding all created rows as components to the panel
        this.labels.forEach(this::add);
    }

    /**
     * Adds specified text content to the next free row.
     *
     * @param  text                Content to be added to the next free row.
     * @throws OutOfSpaceException When already all available rows are full of content.
     * */
    public void add(String text) {
        this.labels.stream()
                .filter(row -> row.getText().equals(""))                                    // Filtering rows with no text content
                .findFirst()                                                                // Grabbing firs encountered row
                .orElseThrow(() -> new OutOfSpaceException("No space left in text column")) // Detecting if all rows are already used
                .setText(text);                                                             // Setting new text content to found row
    }

    /**
     * Removes content from all rows.
     * */
    public void clear() {
        // Removing text content from each row
        this.labels.forEach(row -> row.setText(""));
    }
}
