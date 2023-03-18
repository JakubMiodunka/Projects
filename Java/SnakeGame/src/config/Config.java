package config;

import general.directions.Direction;

public abstract class Config {
    /**
     * Class used as a storage for all configuration parameters.
     * */

    // GUI window settings
    public static final int windowWith = 600;

    public static final int scorePanelHeight = 50;
    public static final int gamePanelHeight = 600;

    // Initial snake position on game board
    public static final int initialSnakeHeadPositionRow = 0;
    public static final int initialSnakeHeadPositionColumn = 1;

    // Initial snake movement direction
    public static Direction initialSnakeMoveDirection = Direction.RIGHT;

    // Period between each time step of the game - determines how fast snake will be moving
    public static int refreshTime = 125;

    // Game board dimensions
    public static final int gameBoardRows = 24;
    public static final int gameBoardColumns = 24;

    // Graphical settings
    public static final int scorePanelColor = 0x5DB7E3;
    public static final int backgroundColor = 0x4D800F;
    public static final int snakeColor = 0x0C2E0D;
    public static final int appleColor = 0xAD0A41;

    // Control keys key codes
    public final static int upControlKeyCode = 38;     // Up arrow
    public final static int downControlKeyCode = 40;   // Down arrow
    public final static int leftControlKeyCode = 37;   // Left arrow
    public final static int rightControlKeyCode = 39;  // Right arrow
}
