package Qwirkle.client;

import Qwirkle.util.Log;

/**
 * This class acts as middleware for the logging of the client.
 * When something has to be logged, the ClientLog will notify
 * its observer (for now a ClientTUI) so that it can handle this
 * input properly (e.g. logging it the console, displaying it in a
 * gui).
 */
public class ClientLog extends Log {

    public void clientStarted(String host, int port) {
        print("Client started at " + host + ":" + port);
    }

    public void clientSetupStarted() {
        print("Client setup started...");
    }

    public void clientSetupStarted(String name, String host, int port) {
        print("Client setup: name: " + name + " host: " + host + " port: " + port);
        print("Client is starting...");
    }

    public void clientStartupFailed() {
        print("Failed to start client, check if server is running and if host and port are correct.");
    }

    public void noSSLSupport() {
        print("It appears the server does not support SSL. Please use a regular socket connection");
    }

    public void failedToSendMessage(String message) {
        print("Failed to send message: " + message);
    }

    public void failedToSetupIOStreams() {
        print("Failed to establish I/O streams");
    }

    public void failedToCreateSocket() {
        print("Failed to establish socket connection to the server");
    }

    public void nameCutOff(String name) {
        print("Provided too long name, now cut off to: " + name);
    }

    public void invalidHostAddress() {
        print("Invalid host address entered, terminating...");
    }

    public void incorrectPort() {
        print("Provided incorrect port, terminating...");
    }

    public void failedToConnectToServer() {
        print("Failed to establish a connection with the client, probable cause: invalid host");
    }

    public void incomingMessage(String message) {
        print("Incoming: " + message);
    }

    public void lookingForGameType(String gameTypeName) {
        print("Looking for a" + ((gameTypeName.equals("random")) ? " " + gameTypeName : "") + " game" + ((gameTypeName.equals("random")) ? "" : " " + gameTypeName) + "...");
    }

    public void failedToParseMessage(String message) {
        print("Could not parse message: " + message);
    }

    public void usernameExists() {
        print("Username already exists, please reconnect with a different name");
    }

    public void gameEnded() {
        print("Game has ended...");
    }
    public void gameEnded(String type) {
        print("Game has ended because: " + type);
    }
    //TODO implement asking for oppoonent
//    public String askForOpponent() {
//        return null;
//    }

    public int askForGameType() {
        return askForGameType(null);
    }

    public int askForGameType(String enteredGameType) {

        // If not used recursively, ask first for input
        if (enteredGameType == null) {
            enteredGameType = this.askInput("Please request a game type by typing the number of your choice:\n[0] I don't care...\n[1] Versus AI\n[2] Versus one other player\n[3] Versus two other players\n[4] Versus three other players\n[5] I want to challenge someone...");
        }

        // While the input is not correct, keep asking for correct input
        while (!enteredGameType.trim().equals("0") && !enteredGameType.trim().equals("1") && !enteredGameType.trim().equals("2")
                && !enteredGameType.trim().equals("3") && !enteredGameType.trim().equals("4") && !enteredGameType.trim().equals("5")) {
            enteredGameType = this.askInput("Invalid option, please try again (type number of your choice:");
        }

        // Correct input was provided, return it
        return Integer.parseInt(enteredGameType);
    }

    public String askForUsername() {
        return askForUsername(null);
    }

    public String askForUsername(String enteredUsername) {

        // If not provided as argument, ask for it
        if (enteredUsername == null) {
            enteredUsername = this.askInput("Please enter your username:");
        }

        // While input is too long, ask for smaller input
        while (enteredUsername.length() > 15) {
            enteredUsername = askForUsername(this.askInput("Please use 15 characters or less, try again:"));
        }

        // Correct input provided, return it
        return enteredUsername;
    }

    public String askForHostAddress() {
        return askForHostAddress(null);
    }

    public String askForHostAddress(String enteredAddress) {

        // If not provided as argument, ask for it
        if (enteredAddress == null) {
            enteredAddress = this.askInput("Please enter the server host IP address:");
        }

        // While entered address is not valid ask for valid input
        while (!Client.checkForValidIP(enteredAddress)) {
            enteredAddress = askForUsername(this.askInput("Invalid hostname/ipaddress provided, please try again:"));
        }

        // Valid input provided, return it
        return enteredAddress;
    }

    public int askForPort() {
        return askForPort(null);
    }

    public int askForPort(String enteredPort) {

        // If not provided as argument, ask for it
        if (enteredPort == null) {
            enteredPort = this.askInput("Please enter the server port:");
        }

        // While incorrect port ask for new one
        while (!Client.checkForValidPort(enteredPort)) {
            enteredPort = String.valueOf(askForPort(this.askInput("Invalid port provided, please try again:")));
        }

        // Return valid port
        return Integer.parseInt(enteredPort);
    }
}
