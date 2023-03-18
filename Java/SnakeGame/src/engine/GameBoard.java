package engine;

import general.Coordinate;

import javax.swing.JLabel;
import java.awt.Color;

public class GameBoard {
    /**
     * Representation of game board in graphical environment.
     */

    private final JLabel[][] gameBoard;   // Graphical representation of game board

    // Init
    public GameBoard(int rows, int columns) {
        /**
         * @param   rows    Y-axis size of game board.
         * @param   columns X-axis size of game board.
         * */

        // Creating game board array
        this.gameBoard = new JLabel[rows][columns];

        // Filling up game board array with labels
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                JLabel newLabel = new JLabel();             // Creating new JLabel object
                newLabel.setOpaque(true);                   // Setting background color to be visible
                this.gameBoard[row][column] = newLabel;     // Adding the label to the game board array
            }
        }
    }

    // Getters/setters
    public JLabel[] getLabels() {
        /**
         * @return All labels contained by game board.
         * */

        // Determining how many labels are contained by the game board array
        int rows = this.gameBoard.length;
        int columns = this.gameBoard[0].length;
        int numberOfLabels = rows * columns;

        // Creating return table
        JLabel[] labels = new JLabel[numberOfLabels];

        // Adding reference to every label in game board array to the return array
        int labelCounter = 0;

        for (JLabel[] row : this.gameBoard) {
            for (JLabel label : row) {
                labels[labelCounter] = label;
                labelCounter++;
            }
        }

        // Returning all labels contained by game board array
        return labels;
    }

    public void fillWithColor(int labelRow, int labelColumn, Color color) {
        /**
         * Changes background color of label with given position to given color.
         *
         * @param   labelRow    Row index of label to fill.
         * @param   labelColumn Column index of label to fill.
         * @param   color       Color with selected label will be filled.
         * */

        // Changing the background color of label with given coordinates to given one.
        this.gameBoard[labelRow][labelColumn].setBackground(color);
    }

    public void fillWithColor(Coordinate labelPosition, Color color) {
        /**
         * Changes background color of label with given position to given color.
         *
         * @param   labelPosition   Position of a label to fill.
         * @param   color           Color with selected label will be filled.
         * */

        // Coordinate extraction to primitive types
        int labelRow = labelPosition.getRow();
        int labelColumn = labelPosition.getColumn();

        // Changing color of selected label to the given one
        this.gameBoard[labelRow][labelColumn].setBackground(color);
    }

    public void fillWithColor(Color color) {
        /**
         * Changes background color of whole game board to given one.
         *
         * @param   color   Color with whole game board will be filled.
         * */

        // Changing the background color of every label in the game board array
        for (JLabel[] row: this.gameBoard) {
            for (JLabel label: row) {
                label.setBackground(color);
            }
        }
    }
}
