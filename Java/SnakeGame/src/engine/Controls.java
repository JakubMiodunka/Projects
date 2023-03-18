package engine;

import config.Config;
import general.directions.Direction;
import general.directions.DirectionWrapper;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controls extends KeyAdapter {
    /**
     * Responsible for dynamic state change of object on which depends the current snake movement direction.
     * */

    private DirectionWrapper currentMovementDirection;  // Reference to object on which depends
                                                        // the current snake movement direction.
    // Init
    public Controls(DirectionWrapper currentMovementDirection) {
        /**
         * @param   currentMovementDirection    Reference to object on which depends
         *                                      the current snake movement direction.
         * */

        // Parent class constructor call
        super();

        // Properties init
        this.currentMovementDirection = currentMovementDirection;
    }

    //Others
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        /**
         * Method is called when user press any key on keyboard.
         * Responsible for changing the state of the object, on which depends the current snake movement direction
         *
         * @param   keyEvent    Event generated by pressed key.
         * */

        // Initialising temp variable where new movement direction will be stored
        Direction newDirection;

        // Extracting key code from captured event
        int keyCode = keyEvent.getKeyCode();

        // Determining the new movement direction
        switch (keyCode) {
            case Config.upControlKeyCode -> newDirection = Direction.UP;
            case Config.downControlKeyCode -> newDirection = Direction.DOWN;
            case Config.leftControlKeyCode -> newDirection = Direction.LEFT;
            case Config.rightControlKeyCode -> newDirection = Direction.RIGHT;
            default -> { return; }  // Exiting
        }

        // Checking if the new movement direction is not the opposite of current one
        Direction oppositeDirection = Direction.getOpposite(this.currentMovementDirection.getDirection());

        if (! newDirection.equals(oppositeDirection)){
            // Changing the movement direction
            this.currentMovementDirection.setDirection(newDirection);
        }
    }
}