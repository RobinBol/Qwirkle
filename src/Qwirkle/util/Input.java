package qwirkle.util;

import qwirkle.client.Client;
import qwirkle.server.Server;
import qwirkle.server.ServerLogger;

import java.util.Scanner;

public class Input {

    /**
     * Handles asking the user on the server for
     * input.
     *
     * @param question Question to ask the user
     */
    public static String ask(String question, Object asker) {

        if (asker instanceof Server) {
            Server server = (Server) asker;

            // Print question
            server.updateObserver(question);
        } else if (asker instanceof Client) {
            Client client = (Client) asker;

            // Print question
            client.updateObserver(question);
        }


        // Create new scanner to listen for input
        Scanner sc = new Scanner(System.in);

        return sc.nextLine();
    }

    /**
     * Method that starts asking for a port, if
     * invalid input is provided it will keep asking
     * for valid input.
     *
     * @return int valid port number
     */
    public static int askForPort(Object asker) {
        return askForPort(asker, null);
    }

    /**
     * Methods that starts asking for a port, if
     * invalid input is provided it will keep asking
     * for valid input.
     *
     * @return int valid port number
     */
    public static int askForPort(Object asker, String enteredPort) {

        // If no provided as argument, ask for it
        if (enteredPort == null) {
            if (asker instanceof Server) enteredPort = ask(ServerLogger.ENTER_PORT, asker);
            else enteredPort = ask("Please enter the server port:", asker);
        }

        // While incorrect port ask for new one
        while (!Validation.checkPort(enteredPort)) {
            if (asker instanceof Server)
                return Integer.parseInt(String.valueOf(askForPort(asker, ask(ServerLogger.PORT_INVALID_RETRY, asker))));
            else
                enteredPort = String.valueOf(askForPort(asker, ask("Invalid port provided, please try again:", asker)));
        }

        // Print that server is configured and starting
        if (asker instanceof Server) {
            Server server = (Server) asker;
            server.updateObserver(ServerLogger.SERVER_STARTED + enteredPort);
        }

        // Return valid port
        return Integer.parseInt(enteredPort);
    }

    /**
     * Ask user to give a valid host address for the server.
     *
     * @return String valid host address
     */
    public static String askForHostAddress(Object asker) {
        return askForHostAddress(asker, null);
    }

    /**
     * Ask user to give a valid host address for the server.
     *
     * @return String valid host address
     */
    public static String askForHostAddress(Object asker, String enteredAddress) {

        // If not provided as argument, ask for it
        if (enteredAddress == null) {
            enteredAddress = ask("Please enter the server host IP address:", asker);
        }

        // While entered address is not valid ask for valid input
        while (!Validation.checkIP(enteredAddress)) {
            enteredAddress = askForHostAddress(asker, ask("Invalid hostname/ipaddress provided, please try again:", asker));
        }

        // Valid input provided, return it
        return enteredAddress;
    }


    /**
     * Ask user to give a username.
     *
     * @return String valid username
     */
    public static String askForUsername(Object asker) {
        return askForUsername(asker, null);
    }

    /**
     * Ask user to give a username.
     *
     * @return String valid username
     */
    public static String askForUsername(Object asker, String enteredUsername) {

        // If not provided as argument, ask for it
        if (enteredUsername == null) {
            enteredUsername = ask("Please enter your username:", asker);
        }

        // While input is too long, ask for smaller input
        while (enteredUsername.length() > 15) {
            enteredUsername = askForUsername(asker, ask("Please use 15 characters or less, try again:", asker));
        }

        // Correct input provided, return it
        return enteredUsername;
    }

    /**
     * Ask user to give a valid game type.
     *
     * @return int gameType
     */
    public static int askForGameType(Object asker) {
        return askForGameType(asker, null);
    }

    /**
     * Ask user to give a valid game type.
     *
     * @param enteredGameType gameType, to check if valid
     * @return int gameType
     */
    public static int askForGameType(Object asker, String enteredGameType) {

        // If not used recursively, ask first for input
        if (enteredGameType == null) {
            enteredGameType = ask("Please request a game type by typing the number of your choice:\n[0] I don't care...\n[1] Versus AI\n[2] Versus one other player\n[3] Versus two other players\n[4] Versus three other players\n[5] I want to challenge someone\n[6] Wait for incoming invitation", asker);
        }

        // While the input is not correct, keep asking for correct input
        if (!enteredGameType.trim().equals("0") && !enteredGameType.trim().equals("1") && !enteredGameType.trim().equals("2")
                && !enteredGameType.trim().equals("3") && !enteredGameType.trim().equals("4") && !enteredGameType.trim().equals("5")
                && !enteredGameType.trim().equals("6")) {
            enteredGameType = ask("Invalid option, please try again (type number of your choice:", asker);
        }

        // Correct input was provided, return it
        return Integer.parseInt(enteredGameType);
    }

    /**
     * Ask user to give a valid opponent to challenge.
     *
     * @return String playername
     */
    public static String askForOpponent(Object asker) {
        return ask("Please provide the name of the player you would like to challenge:", asker);
    }

}
