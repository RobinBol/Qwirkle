package Server;

import Client.Client;
import Util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Scanner;

/**
 * This class acts as middleware for the logging of the server.
 * When something has to be logged, the ServerLog will notify
 * its observer (for now a ServerTUI) so that it can handle this
 * input properly (e.g. logging it the console, displaying it in a
 * gui).
 */
public class ServerLog extends Log {

    public void serverSetupStarted() {
        print("Server setup started...");
    }

    public void serverStarted(String host, int port) {
        print("Server started at " + host + ":" + port);
    }

    public void serverStartupFailed(String host, int port) {
        print("Failed to create ServerSocket at " + host + ":" + port);
    }

    public void failedConnectionWithClient() {
        print("Failed to establish a connection with the client:");
    }

    public void incomingMessage(String message, String clientName) {
        print("[Client " + clientName + "] send: " + message);
    }

    public void clientFailedToAnnounce() {
        print("Client failed to announce according to protocol:");
    }

    public void clientDisconnected(String clientName) {
        print("Client: " + clientName + " disconnected...");
    }

    public void failedToCloseClient(String clientName) {
        print("Failed to close client: " + clientName);
    }

    public void gameStarted(ArrayList<ClientHandler> clients) {
        print("Game was started with: " + clients);
    }

    public void portInUse() {
        print("The port you entered is already in use.");
    }

    public void portIncorrect() {
        print("Provided incorrect port, terminating...");
    }

    public int askForPort() {
        return askForPort(null);
    }

    public int askForPort(String enteredPort) {

        // If no provided as argument, ask for it
        if (enteredPort == null) {
            enteredPort = this.askInput("Please enter port to listen on:");
        }

        // While incorrect port ask for new one
        while (!Client.checkForValidPort(enteredPort)) {
            enteredPort = String.valueOf(askForPort(this.askInput("Invalid port provided, please try again:")));
        }

        // Print that server is configured and starting
        this.print("Server configured at localhost:" + enteredPort);
        this.print("Server is starting...");

        // Return valid port
        return Integer.parseInt(enteredPort);
    }
}
