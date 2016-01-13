package GameLogic;

import ServerClient.ClientHandler;

import java.util.ArrayList;

public class Game {
    private Player[] players;

    public Game(ArrayList<ClientHandler> clients) {

        System.out.println("Started a game with:");
        System.out.println(clients);

        // TODO create players from the clients here and start the game
    }

    public void startGame(){};
    public void setTurn(){};
    public int getScore(Player player){return 0;};
    public void terminateGame(){}; // Players back to lobby
}
