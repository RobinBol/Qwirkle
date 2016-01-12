import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class QwirkleServer {

    private static int port;
    private static String host;
    private List<ClientHandler> clientHandlers;

    /**
     * QwirkleServer constructor, sets certificate credentials,
     * and port number. Then it will start the server.
     *
     * @param port Port number to listen on by server
     */
    public QwirkleServer(int port) {

        // Initialize and set certificate credentials for SSL connection
        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir").replace("src","") + "/certs/key.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        // Save port
        this.port = port;

        // Save host
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Initialize clientHandlers list
        clientHandlers = new ArrayList<ClientHandler>();
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
                    System.out.println("Failed to establish a connection with the client:");
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println("Failed to start QwirkleServer at:" + this.port);
            e.printStackTrace();
        }
    }
    //TODO see method stubs below
//
//    public Lobby createLobby() {
//        return null;
//    }

//    public Player createPlayer() {
//        return null;
//    }


    /**
     * Sends message to all clientHandlers
     * @param message Message to send
     */
    public void broadcast(String message) {
        for (int i = 0; i < clientHandlers.size(); i++) {
            clientHandlers.get(i).sendMessage(message);
        }
    }

    public void addClientHandler(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    public void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public static void main(String[] args) {

        // Check if valid port provided
        int port = 6090;
        if(args.length > 0) {
            try {

                // Valid port number
                port = Integer.parseInt(args[0], 10);
            } catch (NumberFormatException e) {

                // Let user know client is started on default port
                System.out.println("Provided incorrect port, using default " + port);
            }
        }

        // Create new QwirkleServer
        QwirkleServer qs = new QwirkleServer(port);
        qs.startServer();
    }
}