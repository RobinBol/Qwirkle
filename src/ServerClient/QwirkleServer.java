package ServerClient;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import GameLogic.Lobby;

public class QwirkleServer {

    private static int port;
    private static String host;
    private List<ClientHandler> clientHandlers;
    private List<Lobby> lobbies;

    //TODO add features below when added
    private static String[] FEATURES = new String[]{Protocol.Server.Features.SECURITY};

    //TODO match features with client

    /**
     * QwirkleServer constructor, sets certificate credentials,
     * and port number. Then it will start the server.
     *
     * @param port Port number to listen on by server
     */
    public QwirkleServer(int port) {

        // Initialize and set certificate credentials for SSL connection
        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir").replace("src", "") + "/certs/key.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        // Save port
        this.port = port;

        if (this.port == 0) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Server setup started...");
            System.out.println("Please enter port to listen on:");

            while (true) {
                String line = sc.nextLine();
                try {
                    // Valid port number
                    int parsedPort = Integer.parseInt(line, 10);

                    if (parsedPort != 0) {
                        this.port = parsedPort;
                        System.out.println("Server configured at localhost:" + this.port);
                        System.out.println("Server is starting...");
                        break;
                    } else {
                        System.out.println("Invalid port provided, please try again:");
                    }

                } catch (NumberFormatException e) {

                    // Could not parse int from string
                    System.out.println("Invalid port provided, please try again:");
                }
            }
        }

        // Save host
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
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
            System.out.println("QwirkleServer started at " + this.host + ":" + this.port);

            // Keep listening for incoming client connections
            while (true) {

                // Accept incoming
                SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

                // Create clientHandler for incoming client
                try {
                    ClientHandler clientHandler = new ClientHandler(this, sslsocket);
                    addClientHandler(clientHandler);
                    clientHandler.start();

                } catch (IOException e) {

                    // Could not make connection with client
                    System.out.println("Failed to establish a connection with the client:");
                }
            }

        } catch (BindException e) {
            Scanner sc = new Scanner(System.in);
            System.out.println("The port you entered is already in use, please choose a different one:");

            // Listen for new port
            while (true) {
                String line = sc.nextLine();
                try {

                    // Valid port number
                    int parsedPort = Integer.parseInt(line, 10);

                    if (parsedPort != 0) {
                        this.port = parsedPort;
                        System.out.println("Server configured at localhost:" + this.port);
                        System.out.println("Server is starting...");
                        this.startServer();
                        break;
                    } else {
                        System.out.println("Invalid port provided, please try again:");
                    }

                } catch (NumberFormatException e2) {

                    // Could not parse int from string
                    System.out.println("Invalid port provided, please try again:");
                }
            }
        } catch (IOException e) {

            // Could not create ServerSocket
            System.out.println("Failed to create ServerSocket at:" + this.port);
            e.printStackTrace();
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
     *                   this client
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
    public boolean doesUsernameAlreadyExist(String name) {
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
        Lobby lobby = new Lobby(lobbies.size() + 1);

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

            System.out.println("Run addClientToLobby for: " + client.getClientName());
            // And add player
            lobby.addClient(client);
        }

        // Create parameters list
        ArrayList<Object> parameters = new ArrayList<>();

        // Loop over all features this client supports
        for (int i = 0; i < this.FEATURES.length; i++) {

            // Add them to the parameters
            parameters.add(this.FEATURES[i]);
        }

        // Send confirming handshake to client
        client.sendMessage(ProtocolHandler.createPackage(Protocol.Server.HALLO, parameters));
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

                // Let user know client is started on default port
                System.out.println("Provided incorrect port, terminating...");
                System.exit(0);
            }
        }

        // Create and start new QwirkleServer
        QwirkleServer qs = new QwirkleServer(port);
    }
}