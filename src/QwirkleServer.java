import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class QwirkleServer {

    private static int port;
    private static String host;
    /**
     * QwirkleServer constructor, sets certificate credentials,
     * and port number. Then it will start the server.
     *
     * @param port Port number to listen on by server
     */
    public QwirkleServer(int port) {

        // Initialize and set certificate credentials for SSL connection
        System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        // Save port
        this.port = port;

        // Save host
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
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
//
//    public Lobby createLobby() {
//        return null;
//    }

//    public Player createPlayer() {
//        return null;
//    }


    public int getPort() {
        return this.port;
    }

    public String getHost() {
        return this.host;
    }

    public static void main(String[] args) {

        QwirkleServer qs = new QwirkleServer(6090);
        qs.startServer();
    }
}