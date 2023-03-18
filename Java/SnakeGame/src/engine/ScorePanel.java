package engine;

import config.Config;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

public class ScorePanel extends JPanel {

    private int score;                  // Currently displayed score
    private final JLabel scoreLabel;    // Label where Score is displayed

    // Init
    public ScorePanel(int initialScore) {
        /**
         * @param   initialScore    Score value, that will be initially displayed.
         * */

        // Parent class constructor call
        super();

        // Setting panel properties
        this.setBounds(0,0, Config.windowWith, Config.scorePanelHeight);
        this.setBackground(new Color(Config.scorePanelColor));
        this.setLayout(new BorderLayout());

        // Setting up a label where current game score will be displayed
        this.scoreLabel = new JLabel();
        this.setScore(initialScore);
        this.add(scoreLabel, BorderLayout.WEST);
    }

    // Getters/setters
    public int getScore() {
        /**
         * @return  Currently displayed game score.
         * */

        return this.score;
    }

    public void setScore(int newScore) {
        /**
         * @param newScore  New game score to display.
         * */

        this.scoreLabel.setText("Score: " + String.valueOf(newScore));  // Displaying new score
        this.score = newScore;                                          // Setting the property
    }
}
