/**
 * TODO Major todo's listed below:
 * - Match features met client
 */

package qwirkle.server;

import java.io.IOException;
import java.net.*;
import java.util.*;

import qwirkle.gamelogic.Lobby;
import qwirkle.util.Input;
import qwirkle.util.Protocol;
import qwirkle.util.Validation;

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
    private static String[] features = new String[]{Protocol.Server.Features.CHALLENGE};

    /* Keep track of outstanding invites */
    private Map<ClientHandler, ClientHandler> invites = new HashMap<>();

    private Map<Timer, Map<ClientHandler, ClientHandler>> inviteTimeouts = new HashMap<>();

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
     */
    public void startServer() {

        // Let observer know server is starting
        updateObserver(ServerLogger.SETUP_STARTED);

        // Initialize and set certificate credentials for SSL connection
        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir").replace(
            "src", "") + "/certs/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "SSR0CKS");

        // If port entered by terminal, log server started
        if (port != 0) {
            updateObserver(ServerLogger.SERVER_STARTED + port);
        }

        // Check port validity, ask for a valid one if needed
        this.port = (port == 0) ? Input.askForPort(this) : port;

        // Initialize clientHandlers list
        this.clientHandlers = new ArrayList<>();

        // Initialize lobbies list
        this.lobbies = new ArrayList<>();

        try {
//            // Create SSLServerSocket
//            ServerSocketFactory sslserversocketfactory = SSLServerSocketFactory.getDefault();
//            ServerSocket ssocket = sslserversocketfactory.createServerSocket(port);

            // Use regular socket
            ServerSocket ssocket = new ServerSocket(port);

            // Keep listening for incoming client connections
            while (true) {

                // Accept incoming
                Socket socket = ssocket.accept();

                try {
                    // Create clientHandler for incoming client
                    ClientHandler clientHandler = new ClientHandler(this, socket);
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
            this.port = Input.askForPort(this);

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
     * @param clientHandler clientHandler to be added
     */
    public void addClientHandler(ClientHandler clientHandler) {
        this.clientHandlers.add(clientHandler);
    }

    /**
     * Remove clientHandler and disconnect remaining socket.
     *
     * @param clientHandler clientHandler to be removed
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
     * Return clientHandler registered at a specific
     * clientname.
     *
     * @param clientName Name of to be created clientHandler
     * @return clientHandler The created clientHandler
     */
    public ClientHandler getClientHandler(String clientName) {

        // Loop all clientHandlers
        for (int i = 0; i < clientHandlers.size(); i++) {

            // If match was found, return it
            if (clientHandlers.get(i).getClientName().equalsIgnoreCase(clientName)) {
                return clientHandlers.get(i);
            }
        }
        return null;
    }

    /**
     * Register invite from client.
     *
     * @param inviter clientHandler that invites
     * @param invitee clientHandler that gets invited
     */
    public void registerInvite(ClientHandler inviter, ClientHandler invitee) {
        invites.put(invitee, inviter);
    }

    /**
     * Remove invite from internal list, game has started
     * or invite was aborted.
     *
     * @param inviter clientHandler that invites
     * @param invitee clientHandler that gets invited
     */
    public void removeInvite(ClientHandler inviter, ClientHandler invitee) {
        invites.remove(invitee, inviter);

    }

    /**
     * Makes sure that invite gets cancelled and removed after
     * a set amount of time of no reaction.
     *
     * @param inviter clientHandler that invites
     * @param invitee clientHandler that gets invited
     */
    public void setTimeoutForInvite(ClientHandler inviter, ClientHandler invitee) {

        // Create timer
        Timer timer = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // your code her
                removeInvite(inviter, invitee);

                // Send message to opponent to decline
                inviter.sendDeclineInvite();
            }
        };
        timer.schedule(timerTask, 15000);
        Map<ClientHandler, ClientHandler> clients = new HashMap<>();
        clients.put(inviter, invitee);
        inviteTimeouts.put(timer, clients);
    }

    /**
     * When a client responses to an invite, reset the timeout,
     * to prevent duplicate method calls.
     *
     * @param inviter clientHandler that invites
     * @param invitee clientHandler that gets invited
     */
    public void resetInviteTimeout(ClientHandler inviter, ClientHandler invitee) {
        Map<ClientHandler, ClientHandler> compareMap = new HashMap<>();
        compareMap.put(inviter, invitee);
        for (int i = 0; i < inviteTimeouts.size(); i++) {
            if (inviteTimeouts.get(i) != null) {
                inviteTimeouts.get(i).equals(compareMap);
                Timer timer = (Timer) getKeyFromValue(inviteTimeouts, compareMap);
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
            }
        }
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    /**
     * Get inviter, used to start a game with an oponnent.
     *
     * @param invitee clientHandler that gets invited
     * @return clientHandler that acted as inviter
     */
    public ClientHandler getInviter(ClientHandler invitee) {
        return invites.get(invitee);
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
        Lobby lobby = new Lobby(this);

        // Add it to internal list
        addLobby(lobby);

        // Return new lobby
        return lobby;
    }

    /**
     * Add lobby to internal list of lobbies.
     *
     * @param lobby Lobby object to be added to the list
     */
    public void addLobby(Lobby lobby) {
        this.lobbies.add(lobby);
    }

    /**
     * Removes lobby.
     *
     * @param lobby Lobby that needs to be removed from the list
     */
    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
    }

    /**
     * Removes client from lobby.
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
     * or lobby is full it will create a new lobby.
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
        return this.features;
    }

    /**
     * Handles starting the server. Takes a port number as
     * argument, on which the server will be listening for
     * clients.
     *
     * @param args portNumber
     */
    public static void main(String[] args) {

        // Set default port
        int port = 0;

        // If argument provided
        if (args.length > 0) {
            if (Validation.checkPort(args[0])) {
                // Valid port number
                port = Integer.parseInt(args[0], 10);
            } else {
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