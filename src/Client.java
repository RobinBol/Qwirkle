import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client extends Thread {
    private static String name;
    private static String host;
    private static int port;
    private BufferedReader in;
    private BufferedWriter out;
    private static String[] FEATURES = new String[] {"security"};

    /**
     * Client constructor that takes a name, host and port.
     *
     * @param name Name of client
     * @param host Host to connect client to
     * @param port Port to connect on at host
     */
    public Client(String name, InetAddress host, int port) {
        this.name = name;
        this.host = host.getHostAddress();
        this.port = port;
    }

    /**
     * Runs a seperate thread, and listens for incoming messages
     * and logs that to console. Besides
     */
    public void run() {
        try {

            // Create SSLSocket
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(this.host, this.port);

            // Setup input and output streams
            InputStreamReader inputstreamreader = new InputStreamReader(sslsocket.getInputStream());
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(sslsocket.getOutputStream());

            // Store them for later reference
            this.in = new BufferedReader(inputstreamreader);
            this.out = new BufferedWriter(outputstreamwriter);

            // Announce client to server
            this.announce(this.name);

            // Start reading incoming messages
            String string;
            try {
                while ((string = in.readLine()) != null) {
                    System.out.println("Incoming: " + string);
                }
            } catch (IOException e) {
                System.out.println("Could not read from client, assume disconnected");
                e.printStackTrace();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Sends message that announces new client.
     *
     * @param name
     */
    public void announce(String name) {

        // Create parameters array
        ArrayList<String> parameters = new ArrayList<String>();
        parameters.add(this.name);

        for (int i = 0; i < this.FEATURES.length; i++) {
            parameters.add(this.FEATURES[i]);
        }

        // Create package according to protocol
        sendMessage(ProtocolHandler.createPackage("HALLO", parameters));
    }

    /**
     * Sends a message to the output of the socket.
     *
     * @param message Message to send
     */
    public void sendMessage(String message) {
        try {
            out.write(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message: " + message);
        }
    }


    public static void main(String[] args) {

        // Set needed trustStore properties in order to connect to SSLSocket
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir").replace("src","") + "certs/key.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        // Instantiate default values
        String name = "Henk";
        String host = "localhost";
        int port = 8080;

        // Check if proper amount of arguments is provided
        if (args.length == 3) {

            // If name passed in as arg use that
            name = (args[0] instanceof String) ? args[0] : "Henk";

            // Check if valid host is provided, else use default localhost
            if (args[1].matches("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])") == true) {

                // Valid ipv4
                host = args[1];
            } else if (args[1].matches("([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)") == true) {

                // Valid ipv6
                host = null;

                // Let user know this is not supported
                System.out.println("This application does not support IPV6, please use IPV4.");
            }


            // Check if valid port provided
            try {

                // Valid port number
                port = Integer.parseInt(args[2], 10);
            } catch (NumberFormatException e) {

                // Let user know client is started on default port
                System.out.println("Provided incorrect port, using default " + port);
            }
        } else {

            // Inform user about defaults
            System.out.println("Using default values for name, host and port. If you wish to provide custom values please add the arguments: <name> <host> <port>");
        }

        try {

            // Start client on new thread
            Client c = new Client(name, InetAddress.getByName(host), port);
            c.start();

        } catch (IOException e) {
            System.out.println("Failed to establish a connection with the client:");
            e.printStackTrace();
        }
    }
}
