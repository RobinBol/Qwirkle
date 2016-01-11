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

    public ClientHandler(QwirkleServer serverArg, Socket sockArg) throws IOException {
        server = serverArg;
        in = new BufferedReader(new InputStreamReader(sockArg.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(sockArg.getOutputStream()));
    }

    /**
     * This method takes care of sending messages from the Client.
     * Every message that is received, is preprended with the name
     * of the Client, and the new message is offered to the Server
     * for broadcasting. If an IOException is thrown while reading
     * the message, the method concludes that the socket connection is
     * broken and shutdown() will be called.
     */
    public void run() {
        String thisLine = null;

        try {
            while ((thisLine = in.readLine()) != null) {
                System.out.println(thisLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
