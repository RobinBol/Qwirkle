package GameLogic;

import Server.ClientHandler;

import java.util.ArrayList;

/**
 * This class handles everything related to a game. It can be started
 * by creating a new game object, and giving the clients and the lobby
 * from which it is spawned as parameters to the constructor.
 */
public class Game {

    public static final int MAXHANDSIZE = 6;

    private ArrayList<ClientHandler> clients;
    private Player[] players;
    private Board board;

    /**
     * Game constructor, give clients and lobby as parameters
     * so that the game can create its players and have a reference
     * to the lobby from which it was created.
     * @param clients
     * @param lobby
     */
    public Game(ArrayList<ClientHandler> clients, Lobby lobby) {

        // Let lobby know game started
        lobby.gameStarted(clients);

        // TODO check if socket connection to all clients in the game still works
        // TODO if not make sure game ends properly
        // TODO handle properly when a client disconnects:
        this.clients = clients;
        // Initialize new board
        board = new Board();

        // Create players array
        players = new Player[clients.size()];
        for (int i = 0; i < players.length; i++) {

            // Create player from client
            players[i] = new Player(clients.get(i), board);
        }

        // Game can be started
        startGame();
    }

    public void startGame() {
    }

    public void setTurn() {
    }

    public int getScore(Player player) {
        return 0;
    }

    /**
     * Handles terminating the game, sends game end to all
     * clients in game.
     */
    public void terminateGame() {
        for (int i = 0; i < clients.size(); i++) {

            // Create player from client
            clients.get(i).sendGameEnd("DISCONNECT");
        }
    }

    /**
     * Checks wheter the game has a certain player.
     * @param name Player name to be found
     * @return true if player is present in game
     */
    public boolean hasPlayer(String name) {

        // Loop over all players in the game
        for (int i = 0; i < players.length; i++) {

            // Create player from client
            if (players[i].getName().equals(name)){
                return true;
            }
        }


        // Not found
        return false;
    }

    // Players back to lobby
    public Player getFirstPlayer() {
        for (int i = 0; i < players.length; i++) {

        }

        return null;
    }
}
