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
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;
    private int requestsGameType = 0;

    /**
     * ClientHandler constructor, takes a QwirkleServer and Socket,
     * then handles all incoming messages from the client.
     * @param server QwirkleServer on which the client connects
     * @param sock Socket used to read from/write to client
     * @throws IOException
     */
    public ClientHandler(QwirkleServer server, Socket sock) throws IOException {
        this.server = server;
        this.socket = sock;
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
            System.out.println("Client failed to announce according to protocol:");
        }

        // Then read all incoming messages from client
        try {
            while ((thisLine = in.readLine()) != null) {
                if (!thisLine.isEmpty()) {
                    System.out.println(this.clientName + " send to server: " + thisLine);

                    // Parse package
                    ArrayList<Object> result = ProtocolHandler.readPackage(thisLine);

                    // Check if properly parsed data is present
                    if (!result.isEmpty()) {

                        // Handle incoming requests
                        if (result.get(0).equals(Protocol.Client.REQUESTGAME) && result.size() == 2) {
                            this.requestsGameType = Integer.valueOf((String) result.get(1));
                            server.checkLobbiesForGame();
                        }
                    }
                }
            }

            // No more input stream, assume client disconnected
            System.out.println(this.clientName + " disconnected...");

            // Remove client from server
            server.removeClientHandler(this);

        } catch (IOException e) {

            // If user is disconnected before handshake happened
            String clientIdentifier = ((this.clientName != null) ? this.clientName : "Unidentified client");

            // No more input stream, assume client disconnected
            System.out.println(clientIdentifier + " disconnected...");

            // Remove client from server
            server.removeClientHandler(this);

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

            // Trim all spaces
            name = name.trim();

            // If a user with this name already exists on the server
            if (this.server.doesUsernameAlreadyExist(name)) {

                // Create error package
                ArrayList<Object> errorCode = new ArrayList<>();
                errorCode.add(4);

                // Send error package
                sendMessage(ProtocolHandler.createPackage(Protocol.Client.ERROR, errorCode));

                // Remove itself
                server.removeClientHandler(this);

            } else {

                // Save clientname
                this.clientName = name;

                // Broadcast to all clients that client has entered
                server.broadcast("[" + clientName + " has entered]", this.clientName);

                System.out.println("Run listen for: " + this.getClientName());
                // Client is ready to be added to the lobby
                server.addClientToLobby(this);
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
            this.out.write(message);
            this.out.newLine();
            this.out.flush();
        } catch (IOException e) {
            System.out.println(this.clientName + " disconnected");
            server.removeClientHandler(this);
        }
    }

    /**
     * Getter for requestsGameType
     * @return int gameType
     */
    public int getRequestsGameType() {
        return this.requestsGameType;
    }

    /**
     * Getter for the clientName
     * @return String clientName
     */
    public String getClientName() {
        return this.clientName;
    }

    @Override
    public String toString(){
        return getClientName();
    }

    /**
     * Closes the client properly
     */
    public void closeClient() {
        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close client");
        }
    }
}
