
/**
 * TODO Major todo's listed below:
 * - Fix niet arriverende END_GAME
 * - Fix crash wanneer client met foute SSL connect
 */

package qwirkle.server;

import qwirkle.gamelogic.Board;
import qwirkle.gamelogic.Lobby;
import qwirkle.util.Protocol;
import qwirkle.util.ProtocolHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

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
    private ArrayList<String> FEATURES = new ArrayList<>();

    /* Keep track of in game mode */
    private boolean inGame = false;

    /**
     * ClientHandler constructor, takes a QwirkleServer and Socket,
     * then handles all incoming messages from the client.
     *
     * @param server QwirkleServer on which the client connects
     * @param sock   Socket used to read from/write to client
     * @throws IOException
     */
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

                    // Check if properly parsed data is present
                    if (!result.isEmpty()) {

                        // Handle incoming packages
                        if (result.get(0).equals(Protocol.Client.ERROR)) {
                            handleIncomingError(result);
                        } else if (result.get(0).equals(Protocol.Client.REQUESTGAME) && result.size() == 2) {

                            // Client requested a gameType
                            this.requestsGameType = Integer.valueOf((String) result.get(1));

                            // Check updated lobby for matches
                            this.getLobby().checkForGame();
                        }
                    }
                }
            }

            // Handle disconnecting client from server, game and lobby
            if (!disconnected) disconnectClient();

        } catch (IOException e) {

            // Remove client from game, lobby and server
            if (!disconnected) disconnectClient();
        }
    }

    /**
     * Acts on error input from the client.
     *
     * @param error ArrayList holding the error object
     */
    public void handleIncomingError(ArrayList<Object> error) {
        //TODO handle incoming errors
    }

    /**
     * Method that initiates a server broadcast to let
     * all clients know a new client has connected.
     *
     * @throws IOException
     */
    public void waitForHandshake() throws IOException {

        // Wait for incoming package and read it
        ArrayList<Object> incomingPackage = ProtocolHandler.readPackage(in.readLine());

        // Check if initial handshake occurs, and if valid name provided
        if (incomingPackage.get(0).equals(Protocol.Client.HALLO) && incomingPackage.get(1) != null) {

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
                    this.FEATURES.add(String.valueOf(incomingPackage.get(i)));
                }

                // Save clientname
                this.clientName = name;

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
    private void sendMessage(String message) {
        try {
            this.out.write(message);
            this.out.newLine();
            this.out.flush();
        } catch (IOException e) {

            // Remove client from game, lobby and server
            if (!disconnected) disconnectClient();
        }
    }

    /**
     * Handles properly removing a client from the server, lobby and games
     * it might be in.
     */
    public void disconnectClient() {

        // Make sure client doesn't get disconnected more than once
        this.disconnected = true;

        // If user is disconnected before handshake happened
        String clientIdentifier = ((this.clientName != null) ? this.clientName : "Unidentified client");

        // Log client disconnected
        server.updateObserver(clientIdentifier + ServerLogger.CLIENT_DISCONNECTED);

        // Removes client from lobby and game (if applicable)
        Lobby lobby = getLobby();
        lobby.removeClient(this);

        // Remove client from server
        server.removeClientHandler(this);
    }

    /**
     * Get lobby which client is in.
     *
     * @return Lobby of client
     */
    public Lobby getLobby() {
        return this.lobby;
    }

    /**
     * Add lobby to this clientHandler to keep
     * track of the lobby a client is in.
     *
     * @param lobby
     */
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    /**
     * Send message to client that game has ended, indicate
     * how the game has ended by giving the type parameter.
     *
     * @param type
     */
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
     * @param type
     * @param winner
     */
    public void sendGameEnd(String type, String winner) {

        // Create parameters list
        ArrayList<Object> parameters = new ArrayList<>();

        // Add them to the parameters
        parameters.add(type);

        // Add winner if applicable
        if (type == "WIN" && winner != null) parameters.add(winner);

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
        String[] FEATURES = server.getFeatures();

        // Create parameters list
        ArrayList<Object> parameters = new ArrayList<>();

        // Loop over all features this client supports
        for (int i = 0; i < FEATURES.length; i++) {

            // Add them to the parameters
            parameters.add(FEATURES[i]);
        }

        // Send confirming handshake to client
        this.sendMessage(ProtocolHandler.createPackage(Protocol.Server.HALLO, parameters));
    }

    /**
     * Getter for requestsGameType
     *
     * @return int gameType
     */
    public int getRequestsGameType() {
        return this.requestsGameType;
    }

    /**
     * Getter for the clientName
     *
     * @return String clientName
     */
    public String getClientName() {
        return this.clientName;
    }

    /**
     * Return a different string representation
     * of a client than default, use the clientName.
     *
     * @return
     */
    @Override
    public String toString() {
        return getClientName();
    }

    /**
     * Method that forwards a request from Player
     * to log the board to the console.
     */
    public void showBoard(Board board) {
        server.updateObserver(board);
    }

    /**
     * Mark clientHandler is in game/or not.
     *
     * @param state in game
     */
    public void setGameState(boolean state) {
        this.inGame = state;
    }

    /**
     * Closes the client properly
     */
    public void closeClient() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // Assume socket already shut down
        }
    }
}
