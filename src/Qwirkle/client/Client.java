/**
 * TODO Major todo's listed below:
 * - Match features met server
 * - Als invalid certs voor SSL worden gebruikt, fallback to Socket
 */

package qwirkle.client;

import qwirkle.util.Protocol;
import qwirkle.util.ProtocolHandler;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;

/**
 * Client class that can work standalone. It can connect
 * to a qwirkle server and using the protocol communicate.
 * It will try to connect over SSL, it that fails it falls
 * back to a regular socket. This client runs on a separate
 * thread.
 */
public class Client extends Observable implements Runnable {

    /* Variables that identify a client, and its host*/
    private static String name;
    private static String host;
    private static int port;

    /* Instance variables of Socket, I/O and logger*/
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    /* State of client */
    private boolean inGame = false;

    /* Features variable, holding the features this client supports */
    private static String[] FEATURES = new String[]{Protocol.Server.Features.SECURITY, Protocol.Server.Features.CHALLENGE};

    /**
     * Client constructor that takes a name, host and port.
     *
     * @param name Name of client
     * @param host Host to connect client to
     * @param port Port to connect on at host
     */
    public Client(String name, InetAddress host, int port) {

        // If variables provided as parameters use them
        if (name != null) this.name = name;
        if (host != null) this.host = host.getHostAddress();
        if (port != 0) this.port = port;
    }

    /**
     * Runs on a separate thread, and listens for incoming messages
     * and logs that to console. Besides that it announces itself
     * to the server according to protocol.
     */
    public void run() {

        // Check if parameters are already set on commandline, if not ask for them
        if (name == null && host == null && port == 0) {

            // Update observer, setup has started
            updateObserver(ClientLogger.SETUP_STARTED);

            // Start setup process, and ask for correct input
            name = askForUsername();
            host = askForHostAddress();
            port = askForPort();
        }

        // Try to create SSLSocket
        try {

            // Create SSLSocket
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = sslsocketfactory.createSocket(host, port);

            // Create in/out for socket
            setupIOStreams(socket);

            // Announce client to server
            shakeHand();

            // Show in TUI client has started
            updateObserver(ClientLogger.CLIENT_STARTED + host + ":" + port);

            // Start reading for incoming messages
            startInputStream();

        } catch (IOException e) {

            // Update observer, setup has failed
            updateObserver(ClientLogger.SETUP_FAILED);
        }
    }

    /**
     * Sends a message the clientHandler on the server server.
     *
     * @param message Message to send
     */
    public void sendMessage(String message) {
        try {
            out.write(message);
            out.flush();
        } catch (SSLException e) {

            // When Client uses SSL socket but server doesn't
            updateObserver(ClientLogger.NO_SSL);

            // Switch this client to use a regular socket and reconnect
            switchToRegularSocket();

        } catch (IOException e) {

            // When message could not be delivered
            updateObserver(ClientLogger.CLIENT_DISCONNECTED);
        }
    }

    /**
     * Sends message that announces new client to the
     * server according to protocol.
     */
    public void shakeHand() {

        // Create parameters array
        ArrayList<Object> parameters = new ArrayList<>();

        // Add name as first parameter
        parameters.add(this.name);

        // Loop over all features this client supports
        for (int i = 0; i < this.FEATURES.length; i++) {

            // Add them to the parameters
            parameters.add(this.FEATURES[i]);
        }

        // Send package according to protocol
        sendMessage(ProtocolHandler.createPackage("HALLO", parameters));
    }

    /**
     * This method takes a socket, and then sets the
     * instance variables; in and out, to be the incoming
     * and outgoing buffer.
     *
     * @param socket
     */
    public void setupIOStreams(Socket socket) {
        try {

            // Setup input and output streams
            InputStreamReader inputstreamreader = new InputStreamReader(socket.getInputStream());
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(socket.getOutputStream());

            // Store them to be used by instance
            this.in = new BufferedReader(inputstreamreader);
            this.out = new BufferedWriter(outputstreamwriter);

        } catch (IOException e) {

            // Could not setup IO streams
            updateObserver(ClientLogger.SETUP_FAILED);
        }
    }

    /**
     * Starts listening on the incoming stream, and
     * handles the incoming messages from the server.
     */
    public void startInputStream() {
        InputHandler inputHandler = new InputHandler(this, in);
        inputHandler.start();
    }

    /**
     * Replaces the current SSLSocket with a
     * regular socket.
     */
    public void switchToRegularSocket() {
        try {
            // Close current socket
            this.socket.close();

            // Create a new regular socket
            this.socket = new Socket(this.host, this.port);

            // Setup the new IO streams
            setupIOStreams(this.socket);

            // Announce client to server
            this.shakeHand();

            // Start listening for incoming messages
            // on this new socket
            startInputStream();

        } catch (IOException e) {

            // Could not setup IO streams
            updateObserver(ClientLogger.SETUP_FAILED);
        }
    }

    /**
     * Checks a string to be a valid ip address
     *
     * @param host ip address String
     * @return
     */
    public static boolean checkForValidIP(String host) {

        // Localhost is valid
        if (host.equals("localhost")) {
            return true;

        } else if (host.matches("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])") == true) {

            // Valid ipv4 address
            return true;

        } else if (host.matches("([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)") == true) {

            // Valid ipv6, but not supported
            return false;
        }

        // Default false
        return false;
    }

    /**
     * Checks a string to be a valid port
     *
     * @param port
     * @return
     */
    public static boolean checkForValidPort(String port) {
        try {

            // Valid port number
            int parsedPort = Integer.parseInt(port, 10);

            // Port can not be 0
            if (parsedPort != 0) {
                return true;
            } else {
                return false;
            }

        } catch (NumberFormatException e) {

            // Could not parse int from string
            return false;
        }
    }

    /**
     * Marks client as in game, or out of game.
     *
     * @param state in game or noet
     */
    public void setInGame(boolean state) {
        this.inGame = state;
    }

    /**
     * Getter for in game state.
     *
     * @return true if in game
     */
    public boolean inGame() {
        return this.inGame;
    }

    /**
     * Updates observer with messages.
     *
     * @param message
     */
    public void updateObserver(String message) {
        setChanged();
        notifyObservers(message);
    }

    /**
     * Handles asking the user on the client for
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
     * Ask user to give a valid game type.
     *
     * @return int gameType
     */
    public int askForGameType() {
        return askForGameType(null);
    }

    /**
     * Ask user to give a valid game type.
     *
     * @param enteredGameType gameType, to check if valid
     * @return int gameType
     */
    public int askForGameType(String enteredGameType) {

        // If not used recursively, ask first for input
        if (enteredGameType == null) {
            enteredGameType = ask("Please request a game type by typing the number of your choice:\n[0] I don't care...\n[1] Versus AI\n[2] Versus one other player\n[3] Versus two other players\n[4] Versus three other players\n[5] I want to challenge someone\n[6] Wait for incoming invitation");
        }

        // While the input is not correct, keep asking for correct input
        if (!enteredGameType.trim().equals("0") && !enteredGameType.trim().equals("1") && !enteredGameType.trim().equals("2")
                && !enteredGameType.trim().equals("3") && !enteredGameType.trim().equals("4") && !enteredGameType.trim().equals("5")
                && !enteredGameType.trim().equals("6")) {
            enteredGameType = ask("Invalid option, please try again (type number of your choice:");
        }

        // Correct input was provided, return it
        return Integer.parseInt(enteredGameType);
    }

    /**
     * Ask user to give a valid opponent to challenge.
     *
     * @return String playername
     */
    public String askForOpponent() {
        return ask("Please provide the name of the player you would like to challenge:");
    }

    /**
     * Ask user to give a username.
     *
     * @return String valid username
     */
    public String askForUsername() {
        return askForUsername(null);
    }

    /**
     * Ask user to give a username.
     *
     * @return String valid username
     */
    public String askForUsername(String enteredUsername) {

        // If not provided as argument, ask for it
        if (enteredUsername == null) {
            enteredUsername = ask("Please enter your username:");
        }

        // While input is too long, ask for smaller input
        while (enteredUsername.length() > 15) {
            enteredUsername = askForUsername(ask("Please use 15 characters or less, try again:"));
        }

        // Correct input provided, return it
        return enteredUsername;
    }

    /**
     * Ask user to give a valid host address for the server.
     *
     * @return String valid host address
     */
    public String askForHostAddress() {
        return askForHostAddress(null);
    }

    /**
     * Ask user to give a valid host address for the server.
     *
     * @return String valid host address
     */
    public String askForHostAddress(String enteredAddress) {

        // If not provided as argument, ask for it
        if (enteredAddress == null) {
            enteredAddress = ask("Please enter the server host IP address:");
        }

        // While entered address is not valid ask for valid input
        while (!Client.checkForValidIP(enteredAddress)) {
            enteredAddress = askForUsername(ask("Invalid hostname/ipaddress provided, please try again:"));
        }

        // Valid input provided, return it
        return enteredAddress;
    }

    /**
     * Ask user to give a valid port of the server.
     *
     * @return int valid port number
     */
    public int askForPort() {
        return askForPort(null);
    }

    public int askForPort(String enteredPort) {

        // If not provided as argument, ask for it
        if (enteredPort == null) {
            enteredPort = ask("Please enter the server port:");
        }

        // While incorrect port ask for new one
        while (!Client.checkForValidPort(enteredPort)) {
            enteredPort = String.valueOf(askForPort(ask("Invalid port provided, please try again:")));
        }

        // Return valid port
        return Integer.parseInt(enteredPort);
    }

    /**
     * Handles starting of the client, parses the args
     * given to the program (<name> <host> <port>) or uses
     * default values if none are provided.
     *
     * @param args <name> <host> <port>
     */
    public static void main(String[] args) {

        // Set needed trustStore properties in order to connect to SSLSocket
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir").replace("src", "") + "/certs/key.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        // Define default values
        String name = null;
        String host = null;
        int port = 0;

        // Check if proper amount of arguments is provided
        if (args.length == 3) {

            // If name passed in as arg use that
            name = args[0];

            // Cut string off after 15 chars
            if (name.length() > 15) {
                name = name.substring(0, 15);
                System.out.println(ClientLogger.NAME_TOO_LONG);
            }

            // Check for valid ip
            if (Client.checkForValidIP(args[1])) {

                // Use valid host
                host = args[1];
            } else {

                // Invalid input provided, let user know client is started on default port
                System.out.println(ClientLogger.HOSTNAME_INVALID);
                System.exit(0);
            }

            // Check for valid port
            if (Client.checkForValidPort(args[2])) {

                // Use valid port
                port = Integer.parseInt(args[2], 10);
            } else {

                // Invalid input provided, let user know client is started on default port
                System.out.println(ClientLogger.PORT_INVALID);
                System.exit(0);
            }
        }

        try {

            // Make sure InetAddress does not create
            // 127.0.0.1 when host is null
            InetAddress hostAddress = null;
            if (host != null) {
                hostAddress = InetAddress.getByName(host);
            }

            // Create new client
            Client c = new Client(name, hostAddress, port);

            // Create tui and attach observer
            ClientTUI tui = new ClientTUI();
            c.addObserver(tui);

            // Start client on new thread
            new Thread(c).start();

        } catch (IOException e) {
            System.out.println(ClientLogger.SETUP_FAILED);
        }
    }
}
