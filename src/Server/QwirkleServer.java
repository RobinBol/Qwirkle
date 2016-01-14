package Server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import GameLogic.Lobby;
import Protocol.Protocol;

/**
 * Class that handles the server. It starts a server,
 * either directly from commandline, or it starts a setup
 * process where it asks the user for input. It will handle
 * all incoming clients, creating ClientHandlers for each incoming
 * client. Besides that it will direct users to proper lobbies,
 * where games can be found and started.
 */
public class QwirkleServer {

    /* Variables needed to start the server */
    private static int port;
    private static String host;

    /* Variables that keep track of the clients and lobbies */
    private List<ClientHandler> clientHandlers;
    private List<Lobby> lobbies;

    /* ServerLog instance */
    private ServerLog log;

    //TODO add features below when added
    private static String[] FEATURES = new String[]{Protocol.Server.Features.SECURITY};

    //TODO match features with client

    /**
     * QwirkleServer constructor, sets certificate credentials,
     * and port number. Then it will start the server.
     *
     * @param port Port number to listen on by server
     * @param log ServerLog instance, handling all logging
     */
    public QwirkleServer(int port, ServerLog log) {

        // Let user know server is starting
        log.serverSetupStarted();

        // Initialize and set certificate credentials for SSL connection
        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir").replace("src", "") + "/certs/key.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        // Add log middleware
        this.log = log;

        // Get/save port
        this.port = (this.port == 0) ? log.askForPort() : port;

        // Get/save host
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.log.serverStartupFailed(this.host, this.port);
        }

        // Initialize clientHandlers list
        this.clientHandlers = new ArrayList<>();

        // Initialize lobbies list
        this.lobbies = new ArrayList<>();

        // Start the server
        this.startServer();
    }

    /**
     * Method that starts the QwirkleServer and creates an SSL connection,
     * for potential clients. Returns the success value.
     *
     * @return success (of server startup)
     */
    public void startServer() {
        try {
            // Create SSLServerSocket
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket =
                    (SSLServerSocket) sslserversocketfactory.createServerSocket(port);

            // Server startup success
            log.serverStarted(this.host, this.port);

            // Keep listening for incoming client connections
            while (true) {

                // Accept incoming
                SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

                try {
                    // Create clientHandler for incoming client
                    ClientHandler clientHandler = new ClientHandler(this, sslsocket);
                    addClientHandler(clientHandler);

                    // Start it on separate thread
                    clientHandler.start();

                } catch (IOException e) {

                    // Could not make connection with client
                    log.failedConnectionWithClient();
                }
            }

        } catch (BindException e) {

            // Log port in use
            log.portInUse();

            // Ask for new port
            this.port = log.askForPort();

            // Afterwards start the server
            this.startServer();

        } catch (IOException e) {

            // Could not create ServerSocket
            log.serverStartupFailed(this.host, this.port);
        }
    }

    /**
     * Add clientHandler to internal list of handlers.
     *
     * @param clientHandler
     */
    public void addClientHandler(ClientHandler clientHandler) {
        this.clientHandlers.add(clientHandler);
    }

    /**
     * Remove clientHandler and disconnect remaining socket.
     *
     * @param clientHandler
     */
    public void removeClientHandler(ClientHandler clientHandler) {

        // Remove from lobby
        removeClientFromLobby(clientHandler);

        // Close client
        clientHandler.closeClient();

        // Remove from clientHandlers
        this.clientHandlers.remove(clientHandler);

        // Remove unnecessary lobbies
        removeEmptyLobbies();
    }

    /**
     * Sends message to all clientHandlers.
     *
     * @param message    Message to send
     * @param clientName if provided, the broadcast will skip
     *                   the client provided
     */
    public void broadcast(String message, String clientName) {
        for (int i = 0; i < clientHandlers.size(); i++) {
            if (!this.clientHandlers.get(i).getClientName().equals(clientName)) {
                this.clientHandlers.get(i).sendMessage(message);
            }
        }
    }

    /**
     * Check if there is already a clientHandler connected
     * that uses the same name.
     *
     * @param name Name user wishes to use
     * @return true if name exists in server already
     */
    public boolean usernameAlreadyExist(String name) {
        for (int i = 0; i < clientHandlers.size(); i++) {
            if (name.equals(this.clientHandlers.get(i).getClientName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new lobby, and add it to the
     * internal lobby list.
     *
     * @return Created Lobby object
     */
    public Lobby createLobby() {

        // Create new lobby with id
        Lobby lobby = new Lobby(lobbies.size() + 1, this);

        // Add it to internal list
        addLobby(lobby);

        // Return new lobby
        return lobby;
    }

    /**
     * Add lobby to internal list of lobbies.
     *
     * @param lobby
     */
    public void addLobby(Lobby lobby) {
        this.lobbies.add(lobby);
    }

    /**
     * Check if lobby is empty and can be removed.
     */
    public void removeEmptyLobbies() {
        // Loop over all existing lobbies
        for (int i = 0; i < lobbies.size(); i++) {

            // If lobby is empty, remove it
            if (lobbies.get(i).isEmpty()) {
                lobbies.remove(i);
            }
        }
    }

    /**
     * Starts search on all lobbies for a possible match.
     */
    public void checkLobbiesForGame() {

        // Loop over all existing lobbies
        for (int i = 0; i < lobbies.size(); i++) {

            // Trigger search for game
            lobbies.get(i).searchForGame();
        }
    }

    /**
     * Removes client from lobby
     *
     * @param client ClientHandler to remove
     */
    public void removeClientFromLobby(ClientHandler client) {

        // Loop over all existing lobbies
        for (int i = 0; i < lobbies.size(); i++) {

            // If room left in the lobby
            if (this.lobbies.get(i).hasClient(client)) {

                // Add client
                this.lobbies.get(i).removeClient(client);

                // End this function, were done
                return;
            }
        }
    }

    /**
     * Adds a client to a lobby, if no lobby available
     * or lobby is full it will create a new lobby
     *
     * @param client ClientHandler to be added
     */
    public void addClientToLobby(ClientHandler client) {

        // Keep track of wheter client is added to a lobby
        boolean clientIsAdded = false;

        // Loop over all existing lobbies
        for (int i = 0; i < lobbies.size(); i++) {

            // If room left in the lobby
            if (!this.lobbies.get(i).isFull() && !clientIsAdded) {

                // Add client
                this.lobbies.get(i).addClient(client);

                clientIsAdded = true;
            }
        }

        // If client still not added
        if (!clientIsAdded) {

            // If no lobby found with room left, create new one
            Lobby lobby = createLobby();

            // And add player
            lobby.addClient(client);
        }

        // Ask client what game he would play
        client.requestGameType();
    }

    /**
     * Getter for the server features.
     * @return String array holding the features
     */
    public String[] getFeatures() {
        return this.FEATURES;
    }

    /**
     * All functions below act as a gate to the logger,
     * this is necessary as lobbies, and clients need
     * to be able to trigger logging on the server.
     */
    public void clientAnnounceFailed(){
        this.log.clientFailedToAnnounce();
    }

    public void logIncomingMessage(String message, String clientName){
        this.log.incomingMessage(message, clientName);
    }

    public void logClientDisconnected(String clientName) {
        this.log.clientDisconnected(clientName);
    }

    public void logFailedToCloseClient(String clientName) {
        this.log.failedToCloseClient(clientName);
    }

    public void logGameStarted(ArrayList<ClientHandler> clients) {
        this.log.gameStarted(clients);
    }

    /**
     * Handles starting the server. Takes a port number as
     * argument, on which the server will be listening for
     * clients.
     *
     * @param args <port>
     */
    public static void main(String[] args) {

        // Create tui and log unit
        ServerTUI tui = new ServerTUI();
        ServerLog log = new ServerLog();

        // Add tui as observer
        log.addObserver(tui);

        // Set default port
        int port = 0;

        // If argument provided
        if (args.length > 0) {

            try {
                // Valid port number
                port = Integer.parseInt(args[0], 10);
            } catch (NumberFormatException e) {

                // Let user know port is incorrect
                log.portIncorrect();

                // Exit application
                System.exit(0);
            }
        }

        // Create and start new QwirkleServer
        QwirkleServer qs = new QwirkleServer(port, log);
    }
}