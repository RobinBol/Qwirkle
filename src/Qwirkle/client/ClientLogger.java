package qwirkle.client;

import qwirkle.util.Logger;

/**
 * This class holds logging variables for the client.
 */
public class ClientLogger extends Logger {

    public static final String CLIENT_STARTED = "Client started at ";
    public static final String SETUP_STARTED = "Client setup started...";
    public static final String SETUP_FAILED = "Failed to start client, check if " +
        "server is running and if host and port are correct";
    public static final String NO_SSL = "It appears the server does not support SSL. " +
        "Please use a regular socket connection";
    public static final String CLIENT_DISCONNECTED = " disconnected";
    public static final String HOSTNAME_INVALID = "Invalid host address entered, terminating...";
    public static final String PORT_IN_USE = "The port you entered is already in use";
    public static final String PORT_INVALID = "The port you entered is invalid, terminating...";
    public static final String PORT_INVALID_RETRY = "The port you entered is invalid, " +
        "please try again:";
    public static final String ENTER_PORT = "Please enter a port number:";
    public static final String INCOMING_MESSAGE = "Incoming message: ";
    public static final String NAME_TOO_LONG = "Name that was provided is too long, " +
        "has been cut off at 15 characters";
    public static final String NOT_CHALLENGABLE = "Player you challenged was not able " +
        "to respond to your challenge...";
    public static final String WAITING = "Waiting...";
    public static final String SEARCHING_GAME = "Busy looking for a matching game...";
    public static final String USERNAME_EXISTS = "Username already exists, please " +
        "reconnect with a different name";
    public static final String GAME_ENDED = "Game has ended...";
    public static final String GAME_ENDED_BECAUSE = "Game has ended because of ";
    public static final String SOCKET_ERROR = "Something went wrong reading or writing the socket";
    public static final String ASK_CHALLENGE = "You are being challenged, type " +
        "YES to accept or NO to decline:";
    public static final String ANSWER_INVALID = "Please provide a valid answer, yes or no:";
}
