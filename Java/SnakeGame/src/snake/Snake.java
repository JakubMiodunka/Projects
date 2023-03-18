package snake;

import general.Coordinate;
import general.directions.Direction;

import java.util.ArrayList;

public class Snake {
    /**
     * Representation of a snake.
     *
     * In terms of snake movement class does not define what is forbidden, only provides the functionalities
     * to detect potentially forbidden (causing game to be over) actions - the role of game engine is to define what
     * is allowed and what is not and perform related actions.
     * */

    private final ArrayList<Coordinate> snakePosition;  // Used as a storage for positions of all snakes body parts.
                                                        // First element is considered as a head and last as tail.
    private Coordinate previousTailPosition;            // Position of a tail before last snake movement.
                                                        // Used during growth process.
    // Init
    public Snake(Coordinate headPosition) {
        /**
         * Initially snake consists of two body parts - head and tail,
         * where tail is placed on the left site of the head.
         *
         * @param   headPosition    Initial position of snakes head.
         * */

        // Initialising snake position related collection.
        this.snakePosition = new ArrayList<>();

        // Head creation
        this.snakePosition.add(headPosition);

        // Tail creation -  initially placed on the left site of the head
        Coordinate tailPosition = new Coordinate(headPosition.getRow(), headPosition.getColumn() - 1);
        this.snakePosition.add(tailPosition);

        // Initially previous position of a tail is equal to current
        this.previousTailPosition = tailPosition;
    }

    // Getters/setters
    public Coordinate[] getPosition() {
        /**
         * @return Array containing the position of all snakes body parts.
         *          First element should be considered as a head, last as a tail.
         * */

        // Returning snake position related collection as an array.
        return this.snakePosition.toArray(new Coordinate[0]);
    }

    public Coordinate getHeadPosition() {
        /**
         * @return Current position of snakes head.
         * */

        // Returning the value of first element from snake position related collection.
        return this.snakePosition.get(0);
    }

    public Coordinate getTailPosition() {
        /**
         * @return Current position of snakes tail.
         * */

        // Extraction of last available index of snake position related collection.
        int lastElementIndex = this.snakePosition.size() - 1;

        // Returning the value of last element from snake position related collection.
        return this.snakePosition.get(lastElementIndex);
    }

    public int getLength() {
        /**
         * @return  Current snake length, where length of each body part is equal to 1.
         * */

        // Returning the size of snake position related collection
        return this.snakePosition.size();
    }

    public boolean isBitten() {
        /**
         * Checks if snake bite itself - if position of the head is equal to position of any other body part.
         *
         * @return  true of false depending on check result.
         * */

        // Checking if position of the head is equal to position of any other body part
        for (int index = 1; index < this.snakePosition.size(); index++) {
            // Extracting position of current body part
            Coordinate currentBodyPartPosition = this.snakePosition.get(index);

            // Performing check
            if (this.getHeadPosition().equals(currentBodyPartPosition)) {
                return true;
            }
        }

        // Returning false if none of body part position is the same as head
        return false;
    }

    public boolean isBodyContains(Coordinate coordinate) {
        /**
         * Checks if snakes body contains given coordinate.
         *
         * @return  true of false depending on check result.
         * */

        return this.snakePosition.contains(coordinate);
    }

    // Others
    public void grow() {
        /**
         * Responsible for snake growth.
         * Cannot be called before first snake move and more than once after each next move.
         *
         * @throws  IllegalStateException if called before first snake move or more than once after each next move.
         * */

        // Preventing of creation many snake body parts with the same position.
        if (this.getTailPosition().equals(this.previousTailPosition)) {
            // Throwing suitable exception
            throw new IllegalStateException("Creation many snake body parts with the same position");
        }

        // Adding new body part to snake position related collection.
        this.snakePosition.add(this.previousTailPosition);
    }

    public void move(Direction direction) {
        /**
         * Responsible for snake movement.
         * Each call of the method will move snakes head by 1 to given direction - rest of thew body will follow it.
         *
         * @param   direction   Direction where snakes head will be moved - rest of thew body will follow it.
         *
         * @throws  IllegalArgumentException if given movement direction is not supported.
         * */

        // Extraction of head position to primitive types
        int newHeadRow = this.getHeadPosition().getRow();
        int newHeadColumn = this.getHeadPosition().getColumn();

        // Changing extracted position according to direction given in argument
        switch (direction) {
            case UP -> newHeadRow--;
            case DOWN -> newHeadRow++;
            case LEFT -> newHeadColumn--;
            case RIGHT -> newHeadColumn ++;
            default -> throw new IllegalArgumentException("Given movement direction unsupported");
        }

        // Updating previous tail position with the current position of the tail
        this.previousTailPosition = this.getTailPosition();

        // Passing position of each body part to the previous body part starting from the tail.
        // Head is excluded from this process.
        int tailIndex = this.snakePosition.size() - 1;  // Index of tail in body part list

        for (int index = tailIndex; index > 0; index--) {
            Coordinate previousBodyPartPosition = this.snakePosition.get(index - 1);
            this.snakePosition.set(index, previousBodyPartPosition);
        }

        // Setting new position to the head
        this.snakePosition.set(0, new Coordinate(newHeadRow, newHeadColumn));
    }
}
