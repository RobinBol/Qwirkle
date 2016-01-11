import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private QwirkleServer server;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;

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
        this.clientName = in.readLine();
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
            e.printStackTrace();
        }
    }
}
