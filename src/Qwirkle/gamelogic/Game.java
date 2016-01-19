/**
 * TODO Major todo's listed below:
 * - check if socket connection to all clients in the game still works
 * - if not make sure game ends properly
 * - handle properly when a client disconnects
 */

package qwirkle.gamelogic;

import qwirkle.server.ClientHandler;

import java.util.ArrayList;

/**
 * This class handles everything related to a game. It can be started
 * by creating a new game object, and giving the clients and the lobby
 * from which it is spawned as parameters to the constructor.
 */
public class Game {

    /* Define maximum amount of stones a player may have in its hand */
    public static final int MAXHANDSIZE = 6;

    /* Lobby from which game was started */
    private Lobby lobby;

    /* List of clients */
    private ArrayList<ClientHandler> clients;

    /* List of players in the game */
    private Player[] players;

    /* Board on which the game is played */
    private Board board;

    /**
     * Game constructor, give clients and lobby as parameters
     * so that the game can create its players and have a reference
     * to the lobby from which it was created.
     *
     * @param clients
     * @param lobby
     */
    public Game(ArrayList<ClientHandler> clients, Lobby lobby) {

        // Store lobby
        this.lobby = lobby;

        // Let lobby know game started
        this.lobby.gameStarted();

        // Store clients
        this.clients = clients;

        // Initialize new board
        board = new Board();

        // Create players from connected clients
        players = new Player[clients.size()];
        for (int i = 0; i < players.length; i++) {

            // Update game state of client to in game
            clients.get(i).setGameState(true);

            // Create player from client
            players[i] = new Player(clients.get(i), board);
        }

        // Game can be started
        startGame();
    }

    /**
     * Starts the game, giving turns to the right player.
     */
    public void startGame() {
        // TODO handle starting the game
        System.out.println("Game was properly started!");
    }

    /**
     * Handles terminating the game, sends game end to all
     * clients in game.
     */
    public void terminateGame() {

        // Loop over all clients
        for (int i = 0; i < clients.size(); i++) {

            // Mark client as not in game
            clients.get(i).setGameState(false);

            // Create player from client
            clients.get(i).sendGameEnd("DISCONNECT");
        }

        // Tell lobby game was terminated
        lobby.endGame(this);
    }

    /**
     * Checks whether the game has a certain player.
     *
     * @param name Player name to be found
     * @return true if player is present in game
     */
    public boolean hasPlayer(String name) {

        // Loop over all players in the game
        for (int i = 0; i < players.length; i++) {

            // Create player from client
            if (players[i].getName().equals(name)) {
                return true;
            }
        }

        // Not found
        return false;
    }
}
