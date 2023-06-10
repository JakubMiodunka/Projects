package pl.jakubmiodunka.gui.panels.utilities;

import pl.jakubmiodunka.gui.panels.exceptions.OutOfSpaceException;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Representation of column placed in explorer window, which stores buttons,
 * that can be used to individually perform some actions.
 *
 * @author Jakub Miodunka
 * */
public class ButtonColumn extends JPanel {
    // List where buttons containing by each row are stored
    private final List<JButton> buttons;

    /**
     * @param rows     Number of rows in column (without title row).
     * @param title    Column title.
     * */
    public ButtonColumn(int rows, String title) {
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
        this.buttons = new ArrayList<>();                      // Buttons list creation

        // Rows creation
        for (int rowCounter = 0; rowCounter< rows; rowCounter++) {
            JButton newRow = new JButton();                    // New button creation
            newRow.setHorizontalAlignment(JLabel.CENTER);      // Button title centering
            newRow.setEnabled(false);                          // Disabling the button
            newRow.setVisible(false);                          // Making the button inadvisable
            this.buttons.add(newRow);                          // Adding button to the list
        }

        // Adding all created buttons as components to the panel
        this.buttons.forEach(this::add);
    }

    /**
     * Adds new button to the next free column row.
     * Button is titled using given text and performs specified action.
     *
     * @param  text                Button title
     * @param  action              Action, that should be performed when the button will be pressed.
     * @throws OutOfSpaceException When already all available buttons are used.
     * */
    public void add(String text, ActionListener action) {
        // Searching for first not used button
        JButton notUsedButton = this.buttons.stream()
                .filter(button -> !button.isEnabled())                                          // Not used button is disabled and not visible
                .filter(button -> !button.isVisible())
                .findFirst()
                .orElseThrow(() -> new OutOfSpaceException("No space left in button column"));  // Detecting if all buttons are already used

        // Adding requested functionalities to the found button
        notUsedButton.addActionListener(action);    // Adding action listener
        notUsedButton.setText(text);                // Setting a title
        notUsedButton.setVisible(true);             // Making visible
        notUsedButton.setEnabled(true);             // Making enable
    }

    /**
     * Removes all buttons from the column.
     * */
    public void clear() {
       for (JButton button: this.buttons) {
           // Removing all action listeners from the button
           ActionListener[] actionListeners = button.getActionListeners();
           for (ActionListener actionListener: actionListeners) {
               button.removeActionListener(actionListener);
           }

           // Disabling the button and making it invisible
           button.setText("");
           button.setVisible(false);
           button.setEnabled(false);
       }
    }
}
