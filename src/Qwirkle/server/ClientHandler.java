/**
 * TODO Major todo's listed below:
 * - Fix niet arriverende END_GAME
 * - Fix crash wanneer client met foute SSL connect
 */

package qwirkle.server;

import qwirkle.gamelogic.Game;
import qwirkle.gamelogic.Lobby;
import qwirkle.gamelogic.Stone;
import qwirkle.util.Logger;
import qwirkle.util.Protocol;
import qwirkle.util.ProtocolHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static qwirkle.util.Protocol.Client.DECLINEINVITE;

/**
 * Handles all things related to a client that
 * is connected to the server. It reads from the input, and
 * writes to the output. It runs on a separate thread, and
 * handles all communication to the client.
 */
public class ClientHandler extends Thread {

    /* Instance variables for server, socket and I/O */
    private Server server;
    private Lobby lobby;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    /* Keep track of clients name */
    private String clientName;

    /* Init gameType to invalid number */
    private int requestsGameType = 999;

    /* Need to know state of connection */
    private boolean disconnected = false;

    /* Internal reference to clients features */
    private ArrayList<String> features = new ArrayList<>();

    /* Keep track of in game mode */
    private boolean inGame = false;

    /*@
    invariant getClientName() == clientName
    invariant server != null
    invariant lobby != null && lobby == getLobby()
     */

    /**
     * ClientHandler constructor, takes a QwirkleServer and Socket,
     * then handles all incoming messages from the client.
     *
     * @param server QwirkleServer on which the client connects
     * @param sock   Socket used to read from/write to client
     * @throws IOException if something failes regarding the socket
     */
    //@ requires server != null && sock != null
    public ClientHandler(Server server, Socket sock) throws IOException {
        this.server = server;
        this.socket = sock;
        this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
    }

    /**
     * Runs on a separate thread to handle all incoming/outgoing
     * messages to this client.
     */
    public void run() {
        String incomingMessage;

        // Then read all incoming messages from client
        try {

            // First wait for client to announce itself
            waitForHandshake();

            // Now we can start listening for incoming messages to the server
            while ((incomingMessage = in.readLine()) != null) {

                // Make sure message is not empty
                if (!incomingMessage.isEmpty()) {

                    // Update server with new message
                    server.updateObserver(ServerLogger.INCOMING_MESSAGE + incomingMessage);

                    // Parse package
                    ArrayList<Object> result = ProtocolHandler.readPackage(incomingMessage);
                    //System.out.println(result + "TEST");

                    // Check if properly parsed data is present
                    if (!result.isEmpty()) {

                        // Handle incoming packages
                        if (result.get(0).equals(Protocol.Client.ERROR)) {
                            handleIncomingError(result);
                        } else if (result.get(0).equals(Protocol.Client.REQUESTGAME) && result
                            .size() == 2) {

                            // Client requested a gameType
                            this.requestsGameType = Integer.valueOf((String) result.get(1));

                            // Check updated lobby for matches
                            this.getLobby().checkForGame();
                        } else if (result.get(0).equals(Protocol.Client.INVITE) && result.size()
                            == 2) {

                            // Get opponent clientHandler
                            ClientHandler opponent = server.getClientHandler(String.valueOf(
                                result.get(1)));

                            // Check if opponent is challengable
                            if (opponent == null || !opponent.hasFeature(Protocol.Server.Features
                                .CHALLENGE)) {

                                // Create error package
                                ArrayList<Object> errorCode = new ArrayList<>();
                                errorCode.add(5);

                                // Send error package
                                sendMessage(ProtocolHandler.createPackage(Protocol.Client.ERROR,
                                    errorCode));
                            } else if (opponent != null) {

                                // Forward the invite to the proposed opponent
                                opponent.sendMessage(incomingMessage);

                                // Register it at the server as an invite
                                server.registerInvite(this, opponent);

                                // Set a timeout, to decline after 15 seconds
                                server.setTimeoutForInvite(this, opponent);
                            } else {

                                //TODO specify precise error?
                                // Send error package
                                sendMessage(ProtocolHandler.createPackage(Protocol.Client.ERROR));
                            }
                        } else if (result.get(0).equals(Protocol.Client.ACCEPTINVITE)) {

                            // Get inviter clientHandler
                            ClientHandler opponent = server.getInviter(this);

                            // Check if opponent exists and is within same lobby
                            if (opponent != null && this.getLobby().equals(opponent.getLobby())) {

                                // Create client list with the two clients
                                ArrayList<ClientHandler> clients = new ArrayList<>();
                                clients.add(this);
                                clients.add(opponent);

                                // Start a game with the two clients
                                this.getLobby().startGame(clients);

                                // Remove registered invite
                                server.removeInvite(this, opponent);
                            }

                            // Make sure to reset timer
                            server.resetInviteTimeout(this, opponent);
                        } else if (result.get(0).equals(DECLINEINVITE)) {
                            sendMessage("Invite was declined...");

                            // Get inviter clientHandler
                            ClientHandler opponent = server.getInviter(this);

                            // Send message to opponent to decline
                            if (opponent != null) {
                                opponent.sendMessage(ProtocolHandler.createPackage(DECLINEINVITE));

                                // Remove registered invite
                                server.removeInvite(this, opponent);

                                // Make sure to reset timer
                                server.resetInviteTimeout(this, opponent);
                            }
                        } else if (result.get(0).equals(Protocol.Client.CHANGESTONE)) {
                        	//change stones.
                        	Game game = this.getLobby().getGame(this);
                        	List<Stone> stones = new ArrayList<>();
                        	for (int i = 1; i < result.size(); i++) {
                        		String stone = (String) result.get(i);
                        		stones.add(new Stone(stone.charAt(0),stone.charAt(1)));
                        	}
                        	Stone[] toHand = game.changeStones(stones.toArray(new Stone[stones.size()]));
                        	
                        	sendAddToHand(toHand);
                        	//skip turn
                        	game.skipTurn(this);
                        	

                        } else if (result.get(0).equals(Protocol.Client.MAKEMOVE)) {

                            // Convert result to stone array
                            ArrayList<Stone> stones = new ArrayList<>();
                            for (int i = 1; i < result.size(); i++) {

                                // Get single stone item
                                ArrayList<String> stone = (ArrayList) result.get(i);

                                // Check if stone provided
                                if (stone != null) {

                                    // Parse them into integers
                                    int x = Integer.parseInt(stone.get(1));
                                    int y = Integer.parseInt(stone.get(2));

                                    // Add new stone
                                    stones.add(new Stone(String.valueOf(stone.get(0)).charAt(0),
                                        String.valueOf(stone.get(0)).charAt(1), x, y));
                                }
                            }

                            // Ask lobby to return game where player is in
                            Game game = this.getLobby().getGame(this);
                            Stone[] stoneArray = stones.toArray(new Stone[stones.size()]);

                            // Check if move provided, or skip move
                            if (stoneArray.length != 0) {

                                // Make the move on the current game
                                int score = game.makeMove(stoneArray, this);

                                // Server says invalid move, send that to the client.
                                if (score == -1) {

                                    // Create error package
                                    ArrayList<Object> errorCode = new ArrayList<>();
                                    errorCode.add(7);

                                    // Send error package
                                    sendMessage(ProtocolHandler.createPackage(Protocol.Client
                                        .ERROR, errorCode));
                                }
                            } else {
                                // Skip this player
                                game.skipTurn(this);
                            }
                        }
                    }
                }
            }

            // Handle disconnecting client from server, game and lobby
            if (!disconnected) {
                disconnectClient();
            }

        } catch (IOException e) {

            // Remove client from game, lobby and server
            if (!disconnected) {
                disconnectClient();
            }
        }
    }

    /**
     * Acts on error input from the client.
     *
     * @param error ArrayList holding the error object
     */
    //@ requires error != null
    public void handleIncomingError(ArrayList<Object> error) {
        //TODO handle incoming errors
    }

    /**
     * Sends ADDTOHAND message to client. Takes
     * a stone array as parameter.
     *
     * @param stones Stone array holding all stones need to be send to the hand
     */
    //@ requires stones != null && stones.size() > 0
    public void sendAddToHand(Stone[] stones) {

        // Create new arraylist with parameters
        ArrayList<Object> parameters = new ArrayList<>();

        // Add each stone as a parameter
        for (int i = 0; i < stones.length; i++) {

            // Create single stone parameter
            parameters.add("" + stones[i].getColor() + stones[i].getShape());
        }
        System.out.println(parameters);

        // Send package to client to give its initial hand
        sendMessage(ProtocolHandler.createPackage(Protocol.Server.ADDTOHAND, parameters));
    }

    /**
     * Sends to the client that invite was declined.
     */
    public void sendDeclineInvite() {
        sendMessage(ProtocolHandler.createPackage(DECLINEINVITE));
    }

    /**
     * Sends a give turn message to the client, indicating whose turn
     * it is, whose it was, and what move was made.
     *
     * @param currentClient Client that performed a move
     * @param nextClient    Client that gets the turn
     * @param stones        Move that was made by the currentClient
     */
    //@requires nextClient != null && stones != null
    public void giveTurn(ClientHandler currentClient, ClientHandler nextClient, Stone[] stones) {

        // Create new arraylist with parameters
        ArrayList<Object> parameters = new ArrayList<>();

        // Add non-existing user, as no previous move was made
        if (currentClient == null) {
            parameters.add("null");
        } else {
            parameters.add(currentClient);
        }

        // Add this client to give it the turn
        parameters.add(nextClient.getClientName());

        // If stones available
        if (stones != null && stones.length > 0) {

            // Add them as parameters to the package
            for (int i = 0; i < stones.length; i++) {

                // Create single stone parameter
                parameters.add("" + stones[i].getColor() + "" + stones[i].getShape() + Protocol.Server
                    .Settings.DELIMITER2 + stones[i].getX() + Protocol.Server.Settings
                    .DELIMITER2 + stones[i].getY());
            }
        }

        // Send package to client to give it the turn
        sendMessage(ProtocolHandler.createPackage(Protocol.Server.MOVE, parameters));
    }

    /**
     * Method that initiates a server broadcast to let
     * all clients know a new client has connected.
     *
     * @throws IOException when sending fails
     */
    public void waitForHandshake() throws IOException {

        // Wait for incoming package and read it
        ArrayList<Object> incomingPackage = ProtocolHandler.readPackage(in.readLine());

        // Check if initial handshake occurs, and if valid name provided
        if (incomingPackage.get(0).equals(Protocol.Client.HALLO) && incomingPackage.get(1) !=
            null) {

            // Get name from incoming package
            String name = (String) incomingPackage.get(1);

            // Trim all spaces
            name = name.trim();

            // If a user with this name already exists on the server
            if (this.server.usernameTaken(name)) {

                // Create error package
                ArrayList<Object> errorCode = new ArrayList<>();
                errorCode.add(4);

                // Send error package
                sendMessage(ProtocolHandler.createPackage(Protocol.Client.ERROR, errorCode));

                // Remove itself
                server.removeClientHandler(this);

            } else {

                // Add all features of the client
                for (int i = 2; i < incomingPackage.size(); i++) {
                    this.features.add(String.valueOf(incomingPackage.get(i)));
                }

                // Save clientname
                this.clientName = name;

                // Log client connected
                Logger.print(name + " connected");

                // Client is ready to be added to the lobby
                server.addClientToLobby(this);
            }
        }
    }

    /**
     * Send message to client belonging
     * to this clientHandler.
     *
     * @param message Message to send
     */
    //@ requires message != null
    private void sendMessage(String message) {
        try {
            this.out.write(message);
            this.out.newLine();
            this.out.flush();
        } catch (IOException e) {

            // Remove client from game, lobby and server
            if (!disconnected) {
                disconnectClient();
            }
        }
    }

    /**
     * Handles properly removing a client from the server, lobby and games
     * it might be in.
     */
    //@ requires disconnected == false
    public void disconnectClient() {

        // Make sure client doesn't get disconnected more than once
        this.disconnected = true;

        // If user is disconnected before handshake happened
        String clientIdentifier = (this.clientName != null) ? this.clientName : "Unidentified " +
            "client";

        // Log client disconnected
        server.updateObserver(clientIdentifier + ServerLogger.CLIENT_DISCONNECTED);

        // Removes client from lobby and game (if applicable)
        Lobby lobby = getLobby();
        if (lobby != null) {
            lobby.removeClient(this);
        }

        // Remove client from server
        server.removeClientHandler(this);
    }

    /**
     * Get lobby which client is in.
     *
     * @return Lobby of client
     */
    //@ requires lobby != null
    /*@ pure */ public Lobby getLobby() {
        return this.lobby;
    }

    /**
     * Add lobby to this clientHandler to keep
     * track of the lobby a client is in.
     *
     * @param lobby Lobby object to be saved as this.lobby
     */
    //@ requires lobby != null
    //@ ensures getLobby() == lobby
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    /**
     * Send message to client that game has ended, indicate
     * how the game has ended by giving the type parameter.
     *
     * @param type Description of how game ended
     */
    //@ requires type != null && type.length > 0
    public void sendGameEnd(String type) {

        // Forward method call
        sendGameEnd(type, null);
    }

    /**
     * Send message to client that game has ended, indicate
     * how the game has ended by giving the type parameter,
     * if end is caused by a winner, also send the name of
     * the winner along.
     *
     * @param type   Description of how game ended
     * @param winner If game ended by winning, send the winner
     */
    //@ requires type != null && type.length > 0
    public void sendGameEnd(String type, String winner) {

        // Create parameters list
        ArrayList<Object> parameters = new ArrayList<>();

        // Add them to the parameters
        parameters.add(type);

        // Add winner if applicable
        if (type == "WIN" && winner != null) {
            parameters.add(winner);
        }

        // Send confirming handshake to client
        String p = ProtocolHandler.createPackage(Protocol.Server.GAME_END, parameters);
        this.sendMessage(p);
    }

    /**
     * Sends message to client to indicate game has started,
     * and specifies with whom game is being played.
     *
     * @param clients List of clients in the game
     */
    //@ requires clients != null && clients.size() > 0
    public void sendGameStarted(ArrayList<ClientHandler> clients) {

        // Create parameters list
        ArrayList<Object> parameters = new ArrayList<>();

        // Loop over all features this client supports
        for (int i = 0; i < clients.size(); i++) {

            // Add them to the parameters
            parameters.add(clients.get(i).getClientName());
        }

        // Send confirming handshake to client
        this.sendMessage(ProtocolHandler.createPackage(Protocol.Server.STARTGAME, parameters));
    }

    /**
     * Ask client to return desired game type.
     */
    public void requestGameType() {
        String[] features = server.getFeatures();

        // Create parameters list
        ArrayList<Object> parameters = new ArrayList<>();

        // Loop over all features this client supports
        for (int i = 0; i < features.length; i++) {

            // Add them to the parameters
            parameters.add(features[i]);
        }

        // Send confirming handshake to client
        this.sendMessage(ProtocolHandler.createPackage(Protocol.Server.HALLO, parameters));
    }

    /**
     * Getter for requestsGameType.
     *
     * @return int gameType
     */
    //@ ensures \result == this.requestsGameType
    /*@ pure */ public int getRequestsGameType() {
        return this.requestsGameType;
    }

    /**
     * Getter for the clientName.
     *
     * @return String clientName
     */
    //@ ensures \result == this.clientName
    /*@ pure */ public String getClientName() {
        return this.clientName;
    }

    /**
     * Return a different string representation
     * of a client than default, use the clientName.
     *
     * @return clients clientName
     */
    @Override
    //@ ensures \result == getClientName()
    /*@ pure */ public String toString() {
        return getClientName();
    }

    /**
     * Mark clientHandler is in game/or not.
     *
     * @param state in game
     */
    //@ requires state != null
    //@ ensures inGame == state
    public void setGameState(boolean state) {
        this.inGame = state;
    }

    /**
     * Returns the current game state.
     * @return inGame game state
     */
    //@ ensures \result == this.inGame
    public boolean getGameState() {
        return this.inGame;
    }

    /**
     *
     */

    /**
     * Check if clientHandler has a certain feature
     * @param feature Feature looking for
     * @return true if clientHandler has the feature
     */
    //@ requires feature != null && feature.length > 0
    /*@ pure */ public boolean hasFeature(String feature) {
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i).equals(feature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Closes the client properly.
     */
    public void closeClient() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // Assume socket already shut down
        }
    }
}
