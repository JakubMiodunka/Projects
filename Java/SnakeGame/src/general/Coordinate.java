package general;

public class Coordinate {
    /**
     * Representation of particular position on two-dimensional game board.
     */

    // Table-like coordinates stored in class instance
    private final int row;      // Row index of coordinate.
    private final int column;   // Column index of coordinate.

    // Init
    public Coordinate(int row, int column) {
        /**
         * @param   row     Row index of coordinate.
         * @param   column  Column index of coordinate.
         * */

        // Properties init
        this.row = row;
        this.column = column;
    }

    // Getters/setters
    public int getRow() {
        /**
         * @return Row index of coordinate.
         * */

        return this.row;
    }

    public int getColumn() {
        /**
         * @return Row index of coordinate.
         * */

        return this.column;
    }

    // Others
    public boolean equals(Coordinate operand) {
        /**
         * Meat to be used to check if given object is equal to the instance of an object.
         * Both row and column values are taken into consideration.
         *
         * @param   operand Object to which particular instance will be compared.
         *
         * @return  true if row and column properties of both instances are equal to each other, false otherwise.
        * */

        return ((operand.getRow() == this.getRow()) && (operand.getColumn() == this.getColumn()));
    }
}
