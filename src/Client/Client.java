package client;

import protocol.Protocol;
import protocol.ProtocolHandler;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Client class that can work standalone. It can connect
 * to a qwirkle server and using the protocol communicate.
 * It will try to connect over SSL, it that fails it falls
 * back to a regular socket. This client runs on a separate
 * thread.
 */
public class Client extends Thread {

    /* Variables that identify a client, and its host*/
    private static String name;
    private static String host;
    private static int port;

    /* Instance variables of Socket, I/O and logger*/
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private ClientLog log;

    //TODO add features below when added
    /* Features variable, holding the features this client supports*/
    private static String[] FEATURES = new String[]{Protocol.Server.Features.SECURITY};

    /**
     * Client constructor that takes a name, host and port.
     *
     * @param name Name of client
     * @param host Host to connect client to
     * @param port Port to connect on at host
     */
    public Client(String name, InetAddress host, int port, ClientLog log) {

        // If variables provided as parameters use them
        if (log != null) this.log = log;
        if (name != null) this.name = name;
        if (host != null) this.host = host.getHostAddress();
        if (port != 0) this.port = port;

        // Check if parameters are already set on commandline, if not ask for them
        if (this.name == null && this.host == null && this.port == 0) {

            // Start setup process, and ask for correct input
            this.log.clientSetupStarted();
            this.name = this.log.askForUsername();
            this.host = this.log.askForHostAddress();
            this.port = this.log.askForPort();
        }

        // Let user know client setup started
        this.log.clientSetupStarted(this.name, this.host, this.port);

        // All parameters set, start client thread
        this.start();
    }

    /**
     * Runs on a separate thread, and listens for incoming messages
     * and logs that to console. Besides that it announces itself
     * to the server according to protocol.
     */
    public void run() {
        try {

            // Create SSLSocket
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.socket = sslsocketfactory.createSocket(this.host, this.port);

            // Create in/out for socket
            setupIOStreams(this.socket);

            // Announce client to server
            this.announce();

            // Show in TUI client has started
            this.log.clientStarted(this.host, this.port);

            // Start reading for incoming messages
            startInputStream();

        } catch (IOException e) {
            this.log.clientStartupFailed();
        }
    }

    /**
     * Sends a message the server.
     *
     * @param message Message to send
     */
    public void sendMessage(String message) {
        try {
            out.write(message);
            out.flush();
        } catch (SSLException e) {

            // When Client uses SSL socket but server doesn't
            this.log.noSSLSupport();

            // Switch this client to use a regular socket and reconnect
            switchToRegularSocket();

        } catch (IOException e) {

            // When message could not be delivered
            this.log.failedToSendMessage(message);
        }
    }

    /**
     * Sends message that announces new client to the
     * server according to protocol.
     */
    public void announce() {

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

            // When i/o could not be created
            this.log.failedToSetupIOStreams();
        }
    }

    /**
     * Starts listening on the incoming stream, and
     * handles the incoming messages from the server.
     */
    public void startInputStream() {

        // Start reading incoming messages
        String incomingValue = null;
        try {
            // While there are messages to be read
            while ((incomingValue = in.readLine()) != null && incomingValue != "" && incomingValue != "\\n" && incomingValue != "\\n\\n") {

                // Log message if present
                if (!incomingValue.isEmpty()) this.log.incomingMessage(incomingValue);

                // TODO handle incoming messages from the server
                // TODO readPackage and parse errors

                ArrayList<Object> result = ProtocolHandler.readPackage(incomingValue);

                // Check if properly parsed data is present
                if (!result.isEmpty()) {
                    System.out.println(result.get(0));


                    // Handle incoming errors
                    if (result.get(0).equals(Protocol.Client.ERROR)) {
                        handleIncomingError(Integer.valueOf((String) result.get(1)));
                    } else if (result.get(0).equals(Protocol.Server.HALLO)) {
                        askForGameType();
                    } else if (result.get(0).equals(Protocol.Server.GAME_END)) {
                        //TODO handle game end
                        if(result.size() >= 1) {
                            log.gameEnded(String.valueOf(result.get(1)));
                        } else {
                            log.gameEnded();
                        }
                    }
                }
            }
        } catch (IOException | NoSuchElementException e) {

            // When something failed while reading a message
            this.log.failedToParseMessage(incomingValue);
        }
    }

    /**
     * This method handles all incoming errors
     *
     * @param errorCode specific error code of error
     */
    public void handleIncomingError(int errorCode) {

        // Username already exists
        if (errorCode == 4) {
            this.log.usernameExists();
            System.exit(0);
        }
    }

    /**
     * Let the client enter a preferred game type and
     * send it to the server.
     */
    public void askForGameType() {
        int gameType = this.log.askForGameType();

        //TODO implement gameType 5
//        if (gameType == 5) {
//            this.log.askForOpponent();
//        }

        // Parse gameTyp to string representation
        String gameTypeName = "none";
        if (gameType == 0) {
            gameTypeName = "random";
        } else if (gameType == 1) {
            gameTypeName = "against a computer oponent";
        } else if (gameType == 2) {
            gameTypeName = "against one human player";
        } else if (gameType == 3) {
            gameTypeName = "against two human players";
        } else if (gameType == 4) {
            gameTypeName = "against three human players";
        }
//        else if (gameType == 5) {
//            gameTypeName = "against a challenged opponent";
//        }

        // Log looking for client
        this.log.lookingForGameType(gameTypeName);

        // Create and send package to request a game at the server, of this game type
        ArrayList<Object> parameters = new ArrayList<>();
        parameters.add(String.valueOf(gameType));
        sendMessage(ProtocolHandler.createPackage(Protocol.Client.REQUESTGAME, parameters));
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
            this.announce();

            // Start listening for incoming messages
            // on this new socket
            startInputStream();

        } catch (IOException e) {

            // When regular socket could not be created
            this.log.failedToCreateSocket();
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
     * Handles starting of the client, parses the args
     * given to the program (<name> <host> <port>) or uses
     * default values if none are provided.
     *
     * @param args <name> <host> <port>
     */
    public static void main(String[] args) {

        // Create tui and log unit
        ClientTUI tui = new ClientTUI();
        ClientLog log = new ClientLog();
        log.addObserver(tui);

        //TODO if certs could not be found fall back to regular socket
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
                log.nameCutOff(name);
            }

            // Check for valid ip
            if (Client.checkForValidIP(args[1])) {

                // Use valid host
                host = args[1];
            } else {

                // Invalid input provided, let user know client is started on default port
                log.invalidHostAddress();
                System.exit(0);
            }

            // Check for valid port
            if (Client.checkForValidPort(args[2])) {

                // Use valid port
                port = Integer.parseInt(args[2], 10);
            } else {

                // Invalid input provided, let user know client is started on default port
                log.incorrectPort();
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

            // Start client on new thread
            Client c = new Client(name, hostAddress, port, log);

        } catch (IOException e) {
            log.failedToConnectToServer();
        }
    }
}
