package engine;

import config.Config;
import general.Coordinate;
import general.directions.DirectionWrapper;
import snake.Snake;

import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.GridLayout;

public class GamePanel extends JPanel {
    /**
     * Panel where game board along with all objects on it are displayed.
     * Serves as game engine.
     * */

    private final GameBoard gameBoard;  // Graphical representation of game board

    private final Snake snake;          // Snake representation
    private Coordinate applePosition;   // Apple position on game board

    private final DirectionWrapper snakeMoveDirection;  // Current snake movement direction

    private boolean gameOver;   // Current status of the game

    // Colors used for drawing objects on game board
    private final Color backgroundColor;
    private final Color snakeColor;
    private final Color appleColor;

    // Init
    public GamePanel() {
        // Parent class constructor call
        super();

        // Setting up panel properties
        this.setLayout(new GridLayout(Config.gameBoardRows, Config.gameBoardColumns));
        this.setBounds(0,Config.scorePanelHeight, Config.windowWith, Config.gamePanelHeight);

        // Snake creation
        Coordinate initialSnakeHeadPosition = new Coordinate(Config.initialSnakeHeadPositionRow,
                Config.initialSnakeHeadPositionColumn);
        this.snake = new Snake(initialSnakeHeadPosition);

        // Initialising snake movement direction
        this.snakeMoveDirection = new DirectionWrapper(Config.initialSnakeMoveDirection);

        // Game board creation
        this.gameBoard = new GameBoard(Config.gameBoardRows, Config.gameBoardColumns);

        // Background color init and filling entire game board with it
        this.backgroundColor = new Color(Config.backgroundColor);
        this.gameBoard.fillWithColor(this.backgroundColor);

        // Adding labels contained by game board to components
        for (JLabel label : this.gameBoard.getLabels()) {
            this.add(label);
        }

        // Snake color init and drawing the snake on game board
        this.snakeColor = new Color(Config.snakeColor);

        for (Coordinate position: this.snake.getPosition()) {
            this.gameBoard.fillWithColor(position.getRow(), position.getColumn(), this.snakeColor);
        }

        // Apple color init and drawing the apple on game board
        this.appleColor = new Color(Config.appleColor);
        this.placeApple();

        // Setting up game status as not over
        this.gameOver = false;
    }

    // Getters/setters
    public DirectionWrapper getSnakeMoveDirection() {
        /**
         * @return  Reference to object, where current snake movement direction is stored.
         * */

        return this.snakeMoveDirection;
    }

    public boolean isGameOver() {
        /**
         * @return  true if game is over or false if game is still ongoing.
         * */

        return this.gameOver;
    }

    public int getScore() {
        /**
         * @return  Current score archived by the player. For now, it is a snake length.
         * */

        return this.snake.getLength();
    }

    // Others
    private void placeApple() {
        /**
         * Places the new apple on game board.
         * */

        // Memory reservation for needed variables
        int row;
        int column;
        Coordinate newApplePosition;

        while (true) {
            // Generating random position of the apple
            row = ThreadLocalRandom.current().nextInt(0, Config.gameBoardRows);
            column = ThreadLocalRandom.current().nextInt(0, Config.gameBoardColumns);

            // Converting generated coordinates into Coordinate instance
            newApplePosition = new Coordinate(row, column);

            // Checking if generated coordinate is not placed on snakes body
            if (! this.snake.isBodyContains(newApplePosition)) {
                // Generation of apple position is considered as done - exiting the loop
                break;
            }
        }

        // Assigning generated apple position to the property
        this.applePosition = newApplePosition;

        // Drawing apple on game board
        this.gameBoard.fillWithColor(this.applePosition, this.appleColor);
    }

    public void refresh() {
        /**
         * Refreshes the game state.
         *
         * @throw   RuntimeException if was called when the game was already over.
         * */

        // Throwing an exception if game is already over
        if (this.gameOver) throw new RuntimeException("The game is already over");

        // Saving snake tail position before the move
        Coordinate previousSnakeTailPosition = this.snake.getTailPosition();

        // Moving snake according to current value of dedicated field
        this.snake.move(this.snakeMoveDirection.getDirection());

        // Checking if snake ate the apple
        if (this.snake.getHeadPosition().equals(this.applePosition)) {
            // Calling snake to grow
            this.snake.grow();

            // Placing new apple on game board
            this.placeApple();
        }

        // Checking if snake does not bite itself
        if (this.snake.isBitten()) {
            // Changing game status
            this.gameOver = true;
        }

        // Extracting current snake head position
        Coordinate currentSnakeHeadPosition = this.snake.getHeadPosition();

        // Updating the game board
        try {
            this.gameBoard.fillWithColor(currentSnakeHeadPosition, snakeColor);
            this.gameBoard.fillWithColor(previousSnakeTailPosition, backgroundColor);
        }
        // Checking if snake body does reach the border of game board
        catch (ArrayIndexOutOfBoundsException exception) {
            // Changing game status
            this.gameOver = true;
        }
    }
}
