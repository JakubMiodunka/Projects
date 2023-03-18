import engine.Engine;

/*
######################################
#       Author: Jakub Miodunka       #
#            Version: 1              #
#       Last update: 24.03.2023      #
#     Used version of JDK: 19.0.2    #
######################################
*/

public class Main {
    public static void main(String[] args) {
        Engine snakeGame = new Engine();    // Creating a new game
        snakeGame.begin();                  // Starting the game runtime
    }
}