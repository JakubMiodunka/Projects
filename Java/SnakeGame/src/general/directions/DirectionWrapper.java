package general.directions;

public class DirectionWrapper {
    /**
     * Wrapper for Direction type values.
     * Can be used to pas such value as reference.
     */

    private Direction direction;    // Stored Direction type value

    // Init
    public DirectionWrapper(Direction initialDirection) {
        /**
         * @param   initialDirection    Initial value of stored one.
         * */

        // Properties init
        this.direction = initialDirection;
    }

    //Getters/setters
    public Direction getDirection() {
        /**
         * @return Stored value.
         * */

        return this.direction;
    }

    public void setDirection(Direction newDirection) {
        /**
         * @param   newDirection    Value to be assigned to stored one.
         * */

        this.direction = newDirection;
    }
}
