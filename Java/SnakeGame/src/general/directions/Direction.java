package general.directions;

public enum Direction {
    /**
     * Representation of direction in two-dimensional space.
     */

    UP, DOWN, LEFT, RIGHT;

    // Others
    public static Direction getOpposite(Direction direction) {
        /**
         * @param   direction   Direction, to which the oppisite will be returned.
         * @return  The opposite direction to given one.
         * */

        switch (direction) {
            case UP -> { return DOWN; }
            case DOWN -> { return UP; }
            case LEFT -> { return RIGHT; }
            case RIGHT -> { return LEFT; }
            default -> throw new IllegalArgumentException("Given direction unsupported");
        }
    }
}
