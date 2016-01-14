package GameLogic;

import ServerClient.ClientHandler;
import GameLogic.Game;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private static final int LOBBYSIZE = 8;
    private static int id;
    private List<ClientHandler> lobbyClients;

    //TODO add invitation system
    
    /**
     * Lobby constructor, takes an id to create
     * a new lobby.
     *
     * @param id Lobby id
     */
    public Lobby(int id) {
        this.id = id;
        this.lobbyClients = new ArrayList<>();
    }

    /**
     * Start a new game, using the clients provided as parameter.
     *
     * @param clients Clients to be used for the new game
     */
    public void startGame(ArrayList<ClientHandler> clients) {

        // Start game for those who don't care
        Game game = new Game(clients);

        // Loop over all clients in newly created game
        for (int i = 0; i < clients.size(); i++) {

            // And remove them from the lobby
            this.removeClient(clients.get(i));
        }

        // Empty gameType list
        clients.clear();
    }

    /**
     * Looks at all connected clients if there is
     * a matching game, if so it starts it.
     */
    public void searchForGame() {

        // Create array lists for each type of game
        ArrayList<ClientHandler> gameTypeZero = new ArrayList<>();
        ArrayList<ClientHandler> gameTypeOne = new ArrayList<>();
        ArrayList<ClientHandler> gameTypeTwo = new ArrayList<>();
        ArrayList<ClientHandler> gameTypeThree = new ArrayList<>();
        ArrayList<ClientHandler> gameTypeFour = new ArrayList<>();

        // Loop over all existing clients and check if match can be made
        for (int i = 0; i < lobbyClients.size(); i++) {

            // Get game type from client
            int requestsGameType = this.lobbyClients.get(i).getRequestsGameType();

            // Add client to desired gameType
            if (requestsGameType == 0) {
                gameTypeZero.add(this.lobbyClients.get(i));

                // We have a match
                if (gameTypeZero.size() >= 2) {
                    startGame(gameTypeZero);
                }
            } else if (requestsGameType == 1) {
                gameTypeOne.add(this.lobbyClients.get(i));

                // We have a match
                if (gameTypeOne.size() > 0) {
                    //TODO start game against AI
                    System.out.println("Start game with AI, this is not implemented yet.");
                }
            } else if (requestsGameType == 2) {
                gameTypeTwo.add(this.lobbyClients.get(i));

                // We have a match
                if (gameTypeTwo.size() == 2) {
                    startGame(gameTypeTwo);
                }
            } else if (requestsGameType == 3) {
                gameTypeThree.add(this.lobbyClients.get(i));

                // We have a match
                if (gameTypeThree.size() == 3) {
                    startGame(gameTypeThree);
                }
            } else if (requestsGameType == 4) {
                gameTypeFour.add(this.lobbyClients.get(i));

                // We have a match
                if (gameTypeFour.size() == 4) {
                    startGame(gameTypeFour);
                }
            }
        }
    }

    /**
     * Add client to the lobby.
     *
     * @param clientHandler Client to be added
     */
    public void addClient(ClientHandler clientHandler) {
        lobbyClients.add(clientHandler);
        System.out.println("Client: " + clientHandler.getClientName() + " was added to lobby: #" + this.getID());
    }

    /**
     * Remove client from internal lobby list.
     *
     * @param clientHandler Client to be removed
     */
    public void removeClient(ClientHandler clientHandler) {

        // Remove client from lobbylist
        this.lobbyClients.remove(clientHandler);
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
     * @return true if empty
     */
    public boolean isEmpty() {
        return lobbyClients.size() == 0;
    }

    /**
     * Get id of this lobby.
     *
     * @return int lobby id
     */
    public int getID() {
        return this.id;
    }
}
