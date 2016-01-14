package Server;

import Protocol.Protocol;
import Protocol.ProtocolHandler;

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
    private QwirkleServer server;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;
    private int requestsGameType = 999;

    /**
     * ClientHandler constructor, takes a QwirkleServer and Socket,
     * then handles all incoming messages from the client.
     *
     * @param server QwirkleServer on which the client connects
     * @param sock   Socket used to read from/write to client
     * @throws IOException
     */
    public ClientHandler(QwirkleServer server, Socket sock) throws IOException {
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
        String thisLine;

        // First wait for client to announce itself
        try {
            listenForAnnounce();
        } catch (IOException e) {
            server.clientAnnounceFailed();
        }

        // Then read all incoming messages from client
        try {
            while ((thisLine = in.readLine()) != null) {
                if (!thisLine.isEmpty()) {
                    server.logIncomingMessage(thisLine, this.clientName);

                    // Parse package
                    ArrayList<Object> result = ProtocolHandler.readPackage(thisLine);

                    // Check if properly parsed data is present
                    if (!result.isEmpty()) {

                        // Handle incoming requests
                        if (result.get(0).equals(Protocol.Client.REQUESTGAME) && result.size() == 2) {
                            this.requestsGameType = Integer.valueOf((String) result.get(1));
                            server.checkLobbiesForGame();
                        }
                    }
                }
            }

            // No more input stream, assume client disconnected
            server.logClientDisconnected(this.clientName);

            // Remove client from server
            server.removeClientHandler(this);

        } catch (IOException e) {

            // If user is disconnected before handshake happened
            String clientIdentifier = ((this.clientName != null) ? this.clientName : "Unidentified client");

            // No more input stream, assume client disconnected
            server.logClientDisconnected(clientIdentifier);

            // Remove client from server
            server.removeClientHandler(this);
        }
    }

    /**
     * Method that initiates a server broadcast to let
     * all clients know a new client has connected.
     *
     * @throws IOException
     */
    public void listenForAnnounce() throws IOException {

        // Wait for incoming package and read it
        ArrayList<Object> incomingPackage = ProtocolHandler.readPackage(in.readLine());

        // Check if initial handshake occurs, and if valid name provided
        if (incomingPackage.get(0).equals(Protocol.Client.HALLO) && incomingPackage.get(1) != null) {

            // Get name from incoming package
            String name = (String) incomingPackage.get(1);

            // Trim all spaces
            name = name.trim();

            // If a user with this name already exists on the server
            if (this.server.usernameAlreadyExist(name)) {

                // Create error package
                ArrayList<Object> errorCode = new ArrayList<>();
                errorCode.add(4);

                // Send error package
                sendMessage(ProtocolHandler.createPackage(Protocol.Client.ERROR, errorCode));

                // Remove itself
                server.removeClientHandler(this);

            } else {

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
    public void sendMessage(String message) {
        try {
            this.out.write(message);
            this.out.newLine();
            this.out.flush();
        } catch (IOException e) {

            // If user is disconnected before handshake happened
            String clientIdentifier = ((this.clientName != null) ? this.clientName : "Unidentified client");

            // Log client disconnected
            server.logClientDisconnected(clientIdentifier);

            // Remove client from server
            server.removeClientHandler(this);
        }
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
        this.sendMessage(ProtocolHandler.createPackage(Protocol.Server.STARTGAME, parameters));
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
     * @return
     */
    @Override
    public String toString() {
        return getClientName();
    }

    /**
     * Closes the client properly
     */
    public void closeClient() {
        try {
            this.socket.close();
        } catch (IOException e) {

            // If user is disconnected before handshake happened
            String clientIdentifier = ((this.clientName != null) ? this.clientName : "Unidentified client");

            // Log user could not be closed
            server.logFailedToCloseClient(clientIdentifier);
        }
    }
}
