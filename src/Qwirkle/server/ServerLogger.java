package qwirkle.server;

import qwirkle.util.Logger;

/**
 * This class holds logging variables for the server.
 */
public class ServerLogger extends Logger {
    public static final String SETUP_STARTED = "Server setup started...";
    public static final String SERVER_STARTED = "Server started, listening on port ";
    public static final String SETUP_FAILED = "Server setup failed";
    public static final String FAILED_CONN = "Failed to establish a connection with the client";
    public static final String CLIENT_DISCONNECTED = " disconnected";
    public static final String GAME_STARTED = "Game was started";
    public static final String PORT_IN_USE = "The port you entered is already in use";
    public static final String PORT_INVALID = "The port you entered is invalid, terminating...";
    public static final String PORT_INVALID_RETRY = "The port you entered is invalid, please try again:";
    public static final String ENTER_PORT = "Please enter a port number:";
    public static final String INCOMING_MESSAGE = "Incoming message: ";
}
