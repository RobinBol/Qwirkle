package ServerClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

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
            listenForAnnounce();
        } catch (IOException e) {
            System.out.println("ServerClient.Client failed to announce according to protocol:");
            e.printStackTrace();
        }

        // Then read all incoming messages from client
        try {
            while ((thisLine = in.readLine()) != null && !thisLine.isEmpty()) {
                System.out.println(this.clientName + "send to server: " + thisLine);
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
    public void listenForAnnounce() throws IOException {

        // Wait for incoming package and read it
        ArrayList<Object> incomingPackage = ProtocolHandler.readPackage(in.readLine());

        // Check if initial handshake occurs, and if valid name provided
        if (incomingPackage.get(0).equals(Protocol.Client.HALLO) && incomingPackage.get(1) != null) {

            // Get name from incoming package
            String name = (String) incomingPackage.get(1);

            // If the name already exists in the server
            if (this.server.doesNameExist(name)) {

                // Create error package
                ArrayList<Object> errorCode = new ArrayList<Object>();
                errorCode.add(4);

                // Send error package
                sendMessage(ProtocolHandler.createPackage(Protocol.Client.ERROR, errorCode));

            } else {

                // Save clientname
                this.clientName = name;


                // Broadcast to all clients that client has entered
                server.broadcast("[" + clientName + " has entered]");
            }
        }
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

    public String getClientName() {
        return this.clientName;
    }
}
