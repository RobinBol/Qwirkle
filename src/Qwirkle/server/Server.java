/**
 * TODO Major todo's listed below:
 * - Match features met client
 */

package qwirkle.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import qwirkle.client.Client;
import qwirkle.gamelogic.Lobby;
import qwirkle.util.Protocol;

/**
 * Class that handles the server. It starts a server,
 * either directly from commandline, or it starts a setup
 * process where it asks the user for input. It will handle
 * all incoming clients, creating ClientHandlers for each incoming
 * client. Besides that it will direct users to proper lobbies,
 * where games can be found and started.
 */
public class Server extends Observable {

    /* Port variable needed to start the server */
    private static int port;

    /* Variables that keep track of the clients and lobbies */
    private List<ClientHandler> clientHandlers;
    private List<Lobby> lobbies;

    /* Keep track of features of the server */
    private static String[] FEATURES = new String[]{Protocol.Server.Features.SECURITY, Protocol.Server.Features.CHALLENGE};

    /**
     * Server constructor, sets certificate credentials,
     * and port number. Then it will start the server.
     *
     * @param port Port number to listen on by server
     */
    public Server(int port) {

        // Store port
        this.port = port;
    }

    /**
     * Updates listening observers, this makes sure
     * it is easy to switch TUIs/GUIs by implementing
     * a different observer.
     *
     * @param parameter Object to print to TUI
     */
    public void updateObserver(Object parameter) {
        setChanged();
        notifyObservers(parameter);
    }

    /**
     * Method that starts the Server and creates an SSL connection,
     * for potential clients. Returns the success value.
     *
     * @return success (of server startup)
     */
    public void startServer() {

        // Let observer know server is starting
        updateObserver(ServerLogger.SETUP_STARTED);

        // Initialize and set certificate credentials for SSL connection
        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir").replace("src", "") + "/certs/key.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        // Check port validity, ask for a valid one if needed
        this.port = (port == 0) ? askForPort() : port;

        // Get host address
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {

            // Unknown host, update observer
            updateObserver(ServerLogger.SETUP_FAILED);
        }

        // Initialize clientHandlers list
        this.clientHandlers = new ArrayList<>();

        // Initialize lobbies list
        this.lobbies = new ArrayList<>();

        // Try to create SSLSocket
        try {
            // Create SSLServerSocket
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket =
                    (SSLServerSocket) sslserversocketfactory.createServerSocket(port);

            // Server startup success
            updateObserver(ServerLogger.SERVER_STARTED + port);

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
                    updateObserver(ServerLogger.FAILED_CONN);
                }
            }
        } catch (BindException e) {

            // Update observer, port in use
            updateObserver(ServerLogger.PORT_IN_USE);

            // Ask for new valid port
            this.port = askForPort();

            // Afterwards re-start the server
            this.startServer();

        } catch (IOException e) {

            // Could not create ServerSocket
            updateObserver(ServerLogger.SETUP_FAILED);
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
    }

    /**
     * Check if there is already a clientHandler connected
     * that uses the same name.
     *
     * @param name Name user wishes to use
     * @return true if name exists in server already
     */
    public boolean usernameTaken(String name) {
        for (int i = 0; i < clientHandlers.size(); i++) {
            if (name.equalsIgnoreCase(this.clientHandlers.get(i).getClientName())) {
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
     * Removes lobby.
     */
    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
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

        // Keep track of whether client is added to a lobby
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
     *
     * @return String array holding the features
     */
    public String[] getFeatures() {
        return this.FEATURES;
    }

    /**
     * Handles asking the user on the server for
     * input.
     *
     * @param question Question to ask the user
     */
    public String ask(String question) {

        // Print question
        updateObserver(question);

        // Create new scanner to listen for input
        Scanner sc = new Scanner(System.in);

        return sc.nextLine();
    }

    /**
     * Method that starts asking for a port, if
     * invalid input is provided it will keep asking
     * for valid input.
     *
     * @return int valid port number
     */
    public int askForPort() {
        return askForPort(null);
    }

    /**
     * Methods that starts asking for a port, if
     * invalid input is provided it will keep asking
     * for valid input.
     *
     * @return int valid port number
     */
    public int askForPort(String enteredPort) {
        // If no provided as argument, ask for it
        if (enteredPort == null) {
            enteredPort = ask(ServerLogger.ENTER_PORT);
        }

        // While incorrect port ask for new one
        while (!Client.checkForValidPort(enteredPort)) {
            return Integer.parseInt(String.valueOf(askForPort(ask(ServerLogger.PORT_INVALID_RETRY))));
        }

        // Print that server is configured and starting
        updateObserver(ServerLogger.SERVER_STARTED + enteredPort);

        // Return valid port
        return Integer.parseInt(enteredPort);
    }

    /**
     * Handles starting the server. Takes a port number as
     * argument, on which the server will be listening for
     * clients.
     *
     * @param args <port>
     */
    public static void main(String[] args) {

        // Set default port
        int port = 0;

        // If argument provided
        if (args.length > 0) {
            try {
                // Valid port number
                port = Integer.parseInt(args[0], 10);
            } catch (NumberFormatException e) {

                // Let user know port is incorrect
                System.out.println(ServerLogger.PORT_INVALID);

                // Exit application
                System.exit(0);
            }
        }

        // Create and start new Server
        Server qs = new Server(port);

        // Create tui and log unit
        ServerTUI tui = new ServerTUI();

        // Add tui as observer
        qs.addObserver(tui);

        // Start server after observer is set
        qs.startServer();
    }
}