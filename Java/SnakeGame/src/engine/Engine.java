package engine;

import config.Config;

import javax.swing.JFrame;

public class Engine extends JFrame {
    /**
     * Place where all functionalities of the program are tide up together.
     * Serves as en engine of the program - the end point in terms of new game creation.
     * */

    private final ScorePanel scorePanel;    // Panel where game score is displayed
    private final GamePanel gamePanel;      // Panel where game board is displayed

    // Init
    public Engine() {
        // Parent class constructor call
        super();

        // Setting up frame properties
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // For some reason on Win11 is a need to add 35px to the height and 15px to the width of the frame
        // to make the game board fully visible
        int windowHeight = Config.scorePanelHeight + Config.gamePanelHeight + 35;
        this.setSize(Config.windowWith + 15, windowHeight);
        this.setResizable(false);
        this.setLayout(null);

        // Creating game panel and adding it to components
        this.gamePanel = new GamePanel();
        this.add(gamePanel);

        // Creating score panel and adding it to components
        this.scorePanel = new ScorePanel(this.gamePanel.getScore());
        this.add(this.scorePanel);

        // Creating user input handler and adding it to the frame components
        Controls controls = new Controls(this.gamePanel.getSnakeMoveDirection());
        this.addKeyListener(controls);

        // Making frame visible
        this.setVisible(true);
    }

    // Others
    private void updateScore() {
        /**
         * Refreshes the currently displayed game score.
         * */

        // Extracting current and displayed scores
        int currentScore = this.gamePanel.getScore();
        int currentlyDisplayedScore = this.scorePanel.getScore();

        // Checking if displayed score is up-to-date
        if (currentlyDisplayedScore != currentScore) {
            // Updating displayed score if necessary
            this.scorePanel.setScore(currentScore);
        }
    }

    private void refresh() {
        /**
         * Refreshes the game state.
         * */

        this.gamePanel.refresh();
        this.updateScore();
    }

    public void begin() {
        /**
         * Main handler of the game. When called game runtime begins.
         * */

        // Refreshing the game state once per configured time period
        while (true) {
            try {
                // Checking if game is not over
                if (!this.gamePanel.isGameOver()) {
                    this.refresh();                     // Refreshing state of the game
                    Thread.sleep(Config.refreshTime);   // Waiting
                }
                else {
                    // Exiting if game is over
                    return;
                }

            } catch (InterruptedException exception) {
                // Exiting if interrupted
                return;
            }
        }
    }
}
