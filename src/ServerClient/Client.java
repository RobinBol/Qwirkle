package ServerClient;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client extends Thread {
    private static String name;
    private static String host;
    private static int port;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    //TODO add features below when added
    private static String[] FEATURES = new String[]{Protocol.Server.Features.SECURITY};

    /**
     * Client constructor that takes a name, host and port.
     *
     * @param name Name of client
     * @param host Host to connect client to
     * @param port Port to connect on at host
     */
    public Client(String name, InetAddress host, int port) {
        this.name = name;
        if (host != null) this.host = host.getHostAddress();
        this.port = port;

        if (this.name == null && this.host == null && this.port == 0) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Client setup started...");
            System.out.println("Please enter your username:");

            int counter = 0;
            while (true) {
                String line = sc.nextLine();

                if (counter == 0) {
                    if (line.length() > 15) {
                        System.out.println("Please use 15 characters or less, try again:");
                    } else {
                        this.name = line;
                        System.out.println("Please enter the server host IP address:");

                        counter++;

                    }
                } else if (counter == 1) {

                    // Check for valid IP
                    if (checkForValidIP(line)) {
                        this.host = line;
                        System.out.println("Please enter the server port:");
                        counter++;
                    } else {
                        System.out.println("Invalid hostname/ipaddress provided, please try again:");
                    }
                } else if (counter == 2) {

                    // Check for valid port
                    if (checkForValidPort(line)) {
                        this.port = Integer.parseInt(line, 10);
                        System.out.println("Client setup: name: " + this.name + " host: " + this.host + " port: " + this.port);
                        System.out.println("Client is starting...");
                        counter++;
                        this.start();
                    } else {

                        // Invalid input provided, let user retry
                        System.out.println("Provided incorrect port, please try again:");
                    }
                }
            }
        } else {

            // Client was already setup using cmdline parameters
            System.out.println("Client setup: name: " + this.name + " host: " + this.host + " port: " + this.port);
            this.start();
        }
    }

    /**
     * Runs on a seperate thread, and listens for incoming messages
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
            this.announce(this.name);

            // Start reading for incoming messages
            startInputStream();

        } catch (IOException e) {
            System.out.println("Failed to start client");
        }

    }

    /**
     * Sends message that announces new client to the
     * server according to protocol.
     *
     * @param name
     */
    public void announce(String name) {

        // Create parameters array
        ArrayList<Object> parameters = new ArrayList<Object>();

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
            System.out.println("It appears the server does not support SSL. Please use a regular socket connection");

            // Switch this client to use a regular socket and reconnect
            switchToRegularSocket();

        } catch (IOException e) {

            // When message could not be delivered
            System.out.println("Failed to send message: " + message);
        }
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
            this.announce(this.name);

            // Start listening for incoming messages
            // on this new socket
            startInputStream();

        } catch (IOException e) {

            // When regular socket could not be created
            System.out.println("Failed to establish socket connection to server");
        }
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
            System.out.println("Failed to establish IO streams");
        }
    }

    /**
     * Starts listening on the incoming stream, and
     * handles the incoming messages from the server.
     */
    public void startInputStream() {

        // Start reading incoming messages
        String incomingValue;
        try {

            // While there are messages to be read
            while ((incomingValue = this.in.readLine()) != null) {

                // TODO handle incoming messages from the server
                System.out.println("Incoming: " + incomingValue);
            }
        } catch (IOException e) {

            // When something failed while reading a message
            System.out.println("Could not handle message from server.");
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

            if (parsedPort != 0) {
                return true;
            } else {
                return false;
            }

        } catch (NumberFormatException e) {
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

        // Set needed trustStore properties in order to connect to SSLSocket
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir").replace("src", "") + "certs/key.jks");
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
                System.out.println("Provided too long name, now cut off to: " + name);
            }

            // Check for valid ip
            if (Client.checkForValidIP(args[1])) {

                // Use valid host
                host = args[1];
            } else {

                // Invalid input provided, let user know client is started on default port
                System.out.println("Invalid host address entered, termintaing...");
                System.exit(0);
            }

            // Check for valid port
            if (Client.checkForValidPort(args[2])) {

                // Use valid port
                port = Integer.parseInt(args[2], 10);
            } else {

                // Invalid input provided, let user know client is started on default port
                System.out.println("Provided incorrect port, terminating...");
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
            System.out.println(hostAddress);
            // Start client on new thread
            Client c = new Client(name, hostAddress, port);

        } catch (IOException e) {
            System.out.println("Failed to establish a connection with the client, probable cause: invalid host");
        }
    }
}
