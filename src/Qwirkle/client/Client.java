/**
 * TODO Major todo's listed below:
 * - Match features met server
 * - Als invalid certs voor SSL worden gebruikt, fallback to Socket
 */

package qwirkle.client;

import qwirkle.gamelogic.Board;
import qwirkle.gamelogic.Player;
import qwirkle.util.Input;
import qwirkle.util.Protocol;
import qwirkle.util.ProtocolHandler;
import qwirkle.util.Validation;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
    private static String[] FEATURES = new String[]{Protocol.Server.Features.CHALLENGE};
    private static List<String> COMMON_FEATURES = new ArrayList<>();

    private Player player;

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
        }  catch (IOException e) {

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
     * Stores matched features with server.
     */
    public void saveMatchedFeature(String feature) {
        this.COMMON_FEATURES.add(feature);
    }

    /**
     * Checks if client has certain feature.
     * @param feature
     */
    public boolean hasFeature(String feature) {
        for (int i = 0; i < this.FEATURES.length; i++) {
            if(this.FEATURES[i].equals(feature)){
                return true;
            }
        }
        return false;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void startGame(){
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
     * Gets name of client.
     */
    public String getName() {
        return this.name;
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
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir").replace("src", "") + "/certs/keystore.jks");
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
