/**
 * TODO Major todo's listed below:
 * - Handle game end, remove game, and update leaderboard
 */

package qwirkle.gamelogic;


import qwirkle.client.Client;
import qwirkle.server.ClientHandler;
import qwirkle.server.Server;
import qwirkle.server.ServerLogger;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When a client enters the server, it is placed in a lobby.
 * These lobbies have a fixed size, to prevent overloaded
 * lobbies. When a lobby is full a new lobby will be created,
 * and the client will be placed there. The lobby handles searching
 * for a matching game and will start it when found.
 */
public class Lobby {

    /* Lobby size, if full new lobby has to be created */
    private static final int LOBBYSIZE = 100;

    /* List of clients in lobby */
    private List<ClientHandler> lobbyClients;

    /* Server object for communication */
    private Server server;


    private List<Game> games;

    /**
     * Lobby constructor creates a lobby, and takes
     * a server object for communication.
     *
     * @param server
     */
    public Lobby(Server server) {
        this.lobbyClients = new ArrayList<>();
        this.games = new ArrayList<>();
        this.server = server;
    }

    /**
     * Start a new game, using the clients provided as parameter.
     *
     * @param clients Clients to be used for the new game
     */
    public void startGame(ArrayList<ClientHandler> clients) {

//        Code below can be commented out if we want to wait some time
//        before starting the game, to give players a chance to
//        disconnect/decline.

//        for (int i = 0; i < clients.size(); i++) {
//            clients.get(i).sendMessage("Game was found, players: " + clients + " game will start in 10 seconds. If you don't want to play this game, please disconnect.");
//        }
//
//        // Wait for 12 seconds
//        try {
//            Thread.sleep(12000);
//        } catch(InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }

        // Check if clients are still present
        boolean clientsPresent = true;

        // Loop over all clients in this lobby
        for (int i = 0; i < clients.size(); i++) {

            // Check if this lobby has this client
            if (!this.hasClient(clients.get(i))) {

                // If not make sure game will not start
                clientsPresent = false;
                break;
            }
        }

        // If all clients still present
        if (clientsPresent) {

            // Loop over all clients that will play the game
            for (int j = 0; j < clients.size(); j++) {

                // Send package to client to indicate game started
                clients.get(j).sendGameStarted(clients);
            }

            // Start game with clients
            Game game = new Game(clients, this);

            // Add game to internal games list
            addGame(game);

            // Loop over all clients in newly created game
            for (int j = 0; j < clients.size(); j++) {

                // And remove them from the lobby
                this.removeClientFromLobby(clients.get(j));
            }
        }
        // Indicate a client has left
        else {

            // Client left
            for (int i = 0; i < clients.size(); i++) {

                // Indicate to client that game has ended
                clients.get(i).sendGameEnd("DISCONNECT");

                // Also send a message to request a new game
                clients.get(i).requestGameType();
            }
        }
    }

    /**
     * Looks at all connected clients if there is
     * a matching game, if so it starts it.
     */
    public void checkForGame() {

        // Create Map that holds all game types and clients
        Map<Integer, ArrayList<ClientHandler>> games = new HashMap<>();

        // Loop over all existing clients and check if match can be made
        for (int i = 0; i < lobbyClients.size(); i++) {

            // Get game type from client
            int requestsGameType = this.lobbyClients.get(i).getRequestsGameType();

            // Add client to proper gameType in map
            if (games.containsKey(requestsGameType)) {
                games.get(requestsGameType).add(this.lobbyClients.get(i));
            } else {
                ArrayList<ClientHandler> clients = new ArrayList<>();
                clients.add(this.lobbyClients.get(i));
                games.put(requestsGameType, clients);
            }

            // Loop over all clients in their game map
            for (int key : games.keySet()) {
                int gameType = key;
                ArrayList<ClientHandler> clients = games.get(key);

                // Check if a game match can be found
                if (gameType == 0 && clients.size() >= 2) {
                    startGame(clients);
                } else if (gameType == 1 && clients.size() == 1) {
                    //TODO implement AI player
                    //TODO add AI player to clients and start game
//                    startGame(clients);
                } else if (clients.size() == gameType) {
                    startGame(clients);
                }
            }
        }
    }

    /**
     * Let server know a game has started.
     */
    public void gameStarted() {
        this.server.updateObserver(ServerLogger.GAME_STARTED);
    }

    /**
     * Keep track of all games running in this lobby.
     *
     * @param game Game that was started
     */
    public void addGame(Game game) {
        games.add(game);
    }

    /**
     * Getter for game objects, can be fetch by clientHandler.
     *
     * @param client
     * @return Game
     */
    public Game getGame(ClientHandler client) {
        for (int i = 0; i < this.games.size(); i++) {
            if (this.games.get(i).hasPlayer(client.getClientName())) {
                return this.games.get(i);
            }
        }
        return null;
    }

    /**
     * After game was terminated, remove it from the lobby.
     *
     * @param game
     */
    public void endGame(Game game) {
        games.remove(game);
    }

    /**
     * Add client to the lobby.
     *
     * @param clientHandler Client to be added
     */
    public void addClient(ClientHandler clientHandler) {

        // Add reference to lobby within clientHandler
        clientHandler.setLobby(this);

        // Add client to lobby
        lobbyClients.add(clientHandler);
    }

    /**
     * Remove client from internal lobby list and from
     * game if it is in-game.
     *
     * @param clientHandler Client to be removed
     */
    public void removeClient(ClientHandler clientHandler) {

        // Loop over all existing games
        for (int i = 0; i < games.size(); i++) {

            // If client is in game, end game and remove client
            if (games.get(i).hasPlayer(clientHandler.getClientName())) {

                // Let client know game has ended
                clientHandler.sendGameEnd("DISCONNECT");

                // Terminate the game
                games.get(i).terminateGame();
            }
        }

        // Remove client from lobby
        removeClientFromLobby(clientHandler);
    }

    /**
     * Removes client from lobby, for example when game is started,
     * or client disconnected.
     *
     * @param clientHandler
     */
    public void removeClientFromLobby(ClientHandler clientHandler) {

        // Remove client from lobbylist
        this.lobbyClients.remove(clientHandler);

        // If empty leaves empty, remove it from the server
        if (isEmpty()) {
            server.removeLobby(this);
        }
    }

    /**
     * Checks if this lobby has a certain client.
     *
     * @param clientHandler Client to be found
     * @return true if found
     */
    public boolean hasClient(ClientHandler clientHandler) {

        // Loop over all existing clients
        for (int i = 0; i < lobbyClients.size(); i++) {

            // If clientName and clientName match
            if (!this.lobbyClients.get(i).getClientName().equals(clientHandler.getClientName())) {

                // Return found true
                return true;
            }
        }

        // Not found
        return false;
    }

    /**
     * Check if lobby is already full.
     *
     * @return true if full
     */
    public boolean isFull() {
        return lobbyClients.size() >= LOBBYSIZE;
    }

    /**
     * Check if lobby is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return lobbyClients.size() == 0;
    }
}
