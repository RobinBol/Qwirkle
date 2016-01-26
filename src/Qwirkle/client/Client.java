/**
 * TODO Major todo's listed below:
 * - Match features met server
 * - Als invalid certs voor SSL worden gebruikt, fallback to Socket
 */

package qwirkle.client;

import qwirkle.gamelogic.Player;
import qwirkle.gamelogic.Stone;
import qwirkle.util.Input;
import qwirkle.util.Logger;
import qwirkle.util.Protocol;
import qwirkle.util.ProtocolHandler;
import qwirkle.util.Validation;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Client class that can work standalone. It can connect
 * to a qwirkle server and using the protocol communicate.
 * It will try to connect over SSL, it that fails it falls
 * back to a regular socket. This client runs on a separate
 * thread.
 */
public class Client extends Observable implements Runnable {

    /* Variables that identify a client, and its host*/
    private String name;
    private static String host;
    private static int port;

    /* Instance variables of Socket, I/O and logger*/
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    /* State of client */
    private boolean inGame = false;

    /* Features variable, holding the features this client supports */
    private static String[] features = new String[]{Protocol.Server.Features.CHALLENGE};
    private static List<String> commonFeatures = new ArrayList<>();

    /* If client is within game, it has a player object */
    private Player player;
    private boolean skippedTurn = false;

    /**
     * Client constructor that takes a name, host and port.
     *
     * @param name Name of client
     * @param host Host to connect client to
     * @param port Port to connect on at host
     */
    public Client(String name, InetAddress host, int port) {

        // If variables provided as parameters use them
        if (name != null) {
            this.name = name;
        }
        if (host != null) {
            this.host = host.getHostAddress();
        }
        if (port != 0) {
            this.port = port;
        }
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
            name = Input.askForUsername(this);
            host = Input.askForHostAddress(this);
            port = Input.askForPort(this);
        }

        try {

            // Create SSLSocket
            // SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            // socket = sslsocketfactory.createSocket(host, port);

            // Use regular socket
            socket = new Socket(host, port);

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
        for (int i = 0; i < this.features.length; i++) {

            // Add them to the parameters
            parameters.add(this.features[i]);
        }

        // Send package according to protocol
        sendMessage(ProtocolHandler.createPackage("HALLO", parameters));
    }

    /**
     * This method takes a socket, and then sets the
     * instance variables; in and out, to be the incoming
     * and outgoing buffer.
     *
     * @param socket Socket from which i/o should be created
     */
    public void setupIOStreams(Socket socket) {
        try {

            // Setup input and output streams
            InputStreamReader inputstreamreader =
                new InputStreamReader(socket.getInputStream());
            OutputStreamWriter outputstreamwriter =
                new OutputStreamWriter(socket.getOutputStream());

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
     * Stores matched features with server.
     *
     * @param feature String representing feature that needs to be saved
     */
    public void saveMatchedFeature(String feature) {
        this.commonFeatures.add(feature);
    }

    /**
     * Checks if client has certain feature.
     *
     * @param feature String representing feature that has to be found
     * @return true if has feature
     */
    public boolean hasFeature(String feature) {
        for (int i = 0; i < this.features.length; i++) {
            if (this.features[i].equals(feature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Store player object.
     *
     * @param player Player object that has to be stored
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Return player object.
     *
     * @return Player object belonging to client
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Start game on client, creates a new player
     * and stores it internally.
     */
    public void startGame() {
        this.setPlayer(new Player(this));
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
    
    public boolean skippedTurn() {
    	return skippedTurn;
    }

    /**
     * Updates observer with messages.
     *
     * @param message String that holds message to be printed
     */
    public void updateObserver(String message) {
        setChanged();
        notifyObservers(message);
    }

    /**
     * Send a move made by the client to the server.
     *
     * @param stones holding all stones in the move
     */
    public void sendMove(Stone[] stones) {
        // Create parameters array
        ArrayList<Object> parameters = new ArrayList<>();

        // Loop over all stones
        for (int i = 0; i < stones.length; i++) {

            // Add them properly formatted as parameter
            parameters.add("" + stones[i].getColor() + stones[i].getShape()
                + Protocol.Server.Settings.DELIMITER2 + stones[i].getX()
                + Protocol.Server.Settings.DELIMITER2 + stones[i].getY());
        }

        // Send package according to protocol
        sendMessage(ProtocolHandler.createPackage(Protocol.Client.MAKEMOVE, parameters));
    }
    
    public void sendTrade(Stone[] stones) {
    	 // Create parameters array
        ArrayList<Object> parameters = new ArrayList<>();

        // Loop over all stones
        for (int i = 0; i < stones.length; i++) {

            // Add them properly formatted as parameter
            parameters.add("" + stones[i].getColor() + stones[i].getShape());
        }

        // Send package according to protocol
        sendMessage(ProtocolHandler.createPackage(Protocol.Client.CHANGESTONE, parameters));
    }

    /**
     * Gets name of client.
     *
     * @return String name of this client
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Handles making a move, asks for correct
     * input and validates the move locally,
     * if move is valid send it to the server.
     */
    public void makeMove() {

    	//reset skipped turn.
    	skippedTurn = false;
        // Store the hand to be able to reset
        getPlayer().saveHand();
        
        int turnType = Input.askForMoveOrTrade(this);
        
        if (turnType == 1) {
        	// Ask user to input move
            Stone[] move = Input.askForMove(this);

            if (move.length != 0) {

                // Make the move locally, and get score
                int score = getPlayer().makeMove(move);

                // TODO handle cases below
                if (score != -1) {
                    // Move is valid on local board, now send to server
                    sendMove(move);
                    
                } else {
                    // Locally placed an invalid move
                    Logger.print("Invalid move entered, retry:");

                    // Make sure hand and board are reset to prev state
                    getPlayer().undoLastMove();

                    // Recursively call this method till valid move
                    makeMove();
                }
            } else {
                // No move made, skip turn
                sendMove(move);
                skippedTurn = true;
            }
        }
        else if (turnType == 2) {
        	Stone[] stones = Input.askForTradeStones(this);
        	sendTrade(stones);
        }
    }

    /**
     * Handles starting of the client, parses the args
     * given to the program (name host port) or uses
     * default values if none are provided.
     *
     * @param args name host port
     */
    public static void main(String[] args) {

        // Set needed trustStore properties in order to connect to SSLSocket
        String projectDir = System.getProperty("user.dir").replace("src", "");
        System.setProperty("javax.net.ssl.trustStore", projectDir + "/certs/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "SSR0CKS");

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
            if (Validation.checkIP(args[1])) {

                // Use valid host
                host = args[1];
            } else {

                // Invalid input provided, let user know client is started on default port
                System.out.println(ClientLogger.HOSTNAME_INVALID);
                System.exit(0);
            }

            // Check for valid port
            if (Validation.checkPort(args[2])) {

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
