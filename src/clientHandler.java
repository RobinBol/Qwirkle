import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private QwirkleServer server;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;
    ArrayList<String> enabledFeatures = new ArrayList<String>();

    /**
     * ClientHandler constructor, takes a QwirkleServer and Socket,
     * then handles all incoming messages from the client.
     * @param server QwirkleServer on which the client connects
     * @param sock Socket used to read from/write to client
     * @throws IOException
     */
    public ClientHandler(QwirkleServer server, Socket sock) throws IOException {
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
    }

    /**
     * Runs on a seperate thread to handle all incoming/outgoing
     * messages to this client.
     */
    public void run() {
        String thisLine;

        // First wait for client to announce itself
        try {
            announce();
        } catch (IOException e) {
            System.out.println("Client failed to announce according to protocol:");
            e.printStackTrace();
        }

        // Then read all incoming messages from client
        try {
            while ((thisLine = in.readLine()) != null) {
                System.out.println("ClientHandler: " + thisLine);
            }
        } catch (IOException e) {
            System.out.println("Could not read from client, assume disconnected");
            e.printStackTrace();
        }
    }

    /**
     * Method that initiates a server broadcast to let
     * all clients know a new client has connected.
     * @throws IOException
     */
    public void announce() throws IOException {

        // Wait for icoming package and read it
        ArrayList<Object> incomingPackage = ProtocolHandler.readPackage(in.readLine());

        // Check if initial handshake occurs, and if valid name provided
        if (incomingPackage.get(0).equals(Protocol.Client.HALLO) && incomingPackage.get(1) != null) {

            //TODO check for duplicate names
            // Save clientname
            this.clientName = (String) incomingPackage.get(1);
        }

        //TODO let ProtocolHandler handle this
        // Broadcast to all clients that client has entered
        server.broadcast("[" + clientName + " has entered]");
    }

    /**
     * Send message to client belonging
     * to this clientHandler.
     * @param message Message to send
     */
    public void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.println(this.clientName + " disconnected");
            server.removeClientHandler(this);
        }
    }
}
