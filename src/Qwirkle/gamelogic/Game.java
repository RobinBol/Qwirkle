/**
 * TODO Major todo's listed below:
 * - check if socket connection to all clients in the game still works
 * - if not make sure game ends properly
 * - handle properly when a client disconnects
 */

package qwirkle.gamelogic;

import qwirkle.server.ClientHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.validator.PublicClassValidator;

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

    /* Bag used in this game */
    private Bag bag;

    private boolean firstMove = true;

    private Map<ClientHandler, Map<Integer, Stone[]>> firstMoves = new HashMap<>();

    private List<ClientHandler> moveOrder = new ArrayList<>();
    int currentMove = 0;

    /**
     * Game constructor, give clients and lobby as parameters
     * so that the game can create its players and have a reference
     * to the lobby from which it was created.
     *
     * @param clients Players in the game
     * @param lobby   Lobby from which they were fetched
     */
    public Game(ArrayList<ClientHandler> clients, Lobby lobby) {

        // Store lobby
        this.lobby = lobby;

        // Let lobby know game started
        this.lobby.gameStarted();

        // Store clients
        this.clients = clients;

        // Game can be started
        startGame();
    }

    /**
     * Starts the game, giving turns to the right player.
     */
    public void startGame() {
        // TODO handle starting the game

        // Initialize new board
        board = new Board();

        // Create new bag used in this game
        bag = new Bag();

        // Create players from connected clients
        for (int i = 0; i < clients.size(); i++) {

            // Update game state of client to in game
            clients.get(i).setGameState(true);

            // Give each client its own hand
            clients.get(i).sendAddToHand(getFirstHand());

            // Give the turn to all clients, no prev move done
            clients.get(i).giveTurn(null, clients.get(i), null);
        }
    }

    /**
     * Take stone from the bag.
     *
     * @return A newly composed array of stones for a hand
     */
    public Stone[] getFirstHand() {
        Stone[] hand = new Stone[Game.MAXHANDSIZE];
        for (int i = 0; i < Game.MAXHANDSIZE; i++) {
            hand[i] = bag.takeStone();
        }
        return hand;
    }
    
    

    /**
     * Forwards a make move from a client, to the board.
     *
     * @param stones Stone array
     */
    public void makeMove(Stone[] stones) {
        makeMove(stones, null);
    }

    /**
     * Forwards a make move from a client, to the board.
     *
     * @param stones Stone array
     * @param client Clienthandler that makes a move
     * @return int holding score of move (-1 if invalid)
     */
    public int makeMove(Stone[] stones, ClientHandler client) {

        // Make firstmove and retrieve score
        int score = this.board.makeMove(stones);
        if (firstMove && client != null) {

            // Create map to hold client and stones
            Map<Integer, Stone[]> playerMove = new HashMap<>();
            playerMove.put(score, stones);

            // Save score, client and its move
            firstMoves.put(client, playerMove);

            //TODO READD?
            // Undo it for now, until we know it is highest score
            //this.board.undoMove();
        } else if (client != null) { // Make regular move

            // Check if stones are present
            if (stones.length > 0) {

                // Give turn to player with second to highest score
                int nextClient = moveOrder.indexOf(client);

                // If client was found in list (which has to be)
                if (nextClient > -1) {

                    // Get next client
                    nextClient++;

                    // Hit end of list, go to first
                    if (nextClient == moveOrder.size()) {
                        nextClient = 0;
                    }

                    // Store new current move
                    currentMove = nextClient;
                }

                // Broadcast next turn
                broadcastNextTurn(client, moveOrder.get(nextClient), stones);
            } else {

                // No stones provided, return empty score
                return 0;
            }
        }
     

        // All first moves are in
        if (firstMove && firstMoves.size() == clients.size()) {

            // Calculate highest score
            ClientHandler highestScoreClient = null;
            ClientHandler secondHighestScoreClient = null;
            int highestScore = 0;
            Stone[] highestScoreMove = null;

            // Loop over all first moves
            for (Map.Entry<ClientHandler, Map<Integer, Stone[]>> move : firstMoves.entrySet()) {

                // Get entry set from a first move
                Map<Integer, Stone[]> scoreAndMove = move.getValue();
                for (Map.Entry<Integer, Stone[]> scoreAndMoveEntry : scoreAndMove.entrySet()) {

                    // If this score is higher then prev highscore
                    if (scoreAndMoveEntry.getKey() > highestScore) {

                        // Second highest was set before, now update
                        if (highestScore != 0) {
                            secondHighestScoreClient = highestScoreClient;
                        }

                        // Set new highscore
                        highestScore = scoreAndMoveEntry.getKey();
                        highestScoreClient = move.getKey();
                        
                        //TODO this doesnt keep in mind that 2 player might have same score and thus you retrieve a wrong moveSet.
                        highestScoreMove = move.getValue().get(highestScore);

                    } else {
                        // Set current second highest, to prev highscore
                        secondHighestScoreClient = move.getKey();
                    }
                }
            }

            // Make the move on the board
            this.board.makeMove(highestScoreMove); 
            //TODO: Synced with local instances?

            // Reset firstMove variable, to indicate regular game flow
            firstMove = false;

            // Build move order list
            moveOrder.add(highestScoreClient);
            moveOrder.add(secondHighestScoreClient);
            for (int i = 0; i < clients.size(); i++) {
                if (!clients.get(i).getClientName().equalsIgnoreCase(
                    highestScoreClient.getClientName())
                    && !clients.get(i).getClientName().equalsIgnoreCase(
                    secondHighestScoreClient.getClientName())) {
                    // Do not re add highest score client and second highest score client
                    moveOrder.add(clients.get(i));
                }
            }

            // Give turn to player with second to highest score
            broadcastNextTurn(moveOrder.get(0), moveOrder.get(1), highestScoreMove);

            // Store current move
            currentMove = 1;
        }

        // Return score to see if valid move
        return score;
    }
    
    /**
     * Changes out stones.
     */
    public Stone[] changeStones(Stone[] stones) {
    	return bag.tradeStones(stones);
    }

    /**
     * Takes a client as parameter, and skips the turn
     * for this client. Gives the turn to the next
     * player in the row.
     *
     * @param client Clienthandler that skips a turn
     */
    public void skipTurn(ClientHandler client) {

        if (firstMove) {
            // Create map to hold client and stones
            Map<Integer, Stone[]> playerMove = new HashMap<>();
            playerMove.put(0, new Stone[0]);

            // Save score, client and its move
            firstMoves.put(client, playerMove);
        } else {
            // Get index of current client
            int nextClient = moveOrder.indexOf(client);

            // If client was found in list (which has to be)
            if (nextClient > -1) {

                // Get next client
                nextClient++;

                // Hit end of list, go to first
                if (nextClient == moveOrder.size()) {
                    nextClient = 0;
                }

                // Store new current move
                currentMove = nextClient;
            }

            // Broadcast new turn
            broadcastNextTurn(moveOrder.get(currentMove), moveOrder.get(nextClient), null);
        }
    }

    /**
     * Send message to all clients, about who made a move, what move, and who is next.
     *
     * @param currentClient ClientHandler that made a move
     * @param nextClient    ClientHandler that gets the turn
     * @param move          Move that currentClient made
     */
    public void broadcastNextTurn(ClientHandler currentClient,
                                  ClientHandler nextClient, Stone[] move) {

        // Prevent looping over stones if user skips turn (no stones)
        if (move != null) {

            // Give current client as much stones back as he put on the board
            ArrayList<Stone> newStones = new ArrayList<>();
            for (int i = 0; i < move.length; i++) {
                Stone stone = this.bag.takeStone();
                if (stone != null) {
                    newStones.add(stone);
                }
            }

            // Give currentClient new stones
            currentClient.sendAddToHand(newStones.toArray(new Stone[newStones.size()]));
        }

        // Loop over all clients
        for (int j = 0; j < clients.size(); j++) {

            // Send all client give turn message
            clients.get(j).giveTurn(currentClient, nextClient, move);
        }
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
        for (int i = 0; i < clients.size(); i++) {

            // Create player from client
            if (clients.get(i).getClientName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        // Not found
        return false;
    }
}
