package qwirkle.util;

import qwirkle.client.Client;
import qwirkle.gamelogic.Stone;
import qwirkle.server.Server;
import qwirkle.server.ServerLogger;

import java.util.ArrayList;
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

    /**
     * Ask user for a valid stone input.
     * @param asker
     * @param handSize
     * @return
     */
    public static String askForStone(Object asker, int handSize) {

        // Ask to make a move
        String stone = Input.ask("Please enter the number of the stone you would like to use (or EXIT to end your move)", asker);

        // Detect first line of valid input
        if (stone.equals("1") || stone.equals("2") || stone.equals("3") || stone.equals("4") || stone.equals("5") || stone.equals("6") || stone.equalsIgnoreCase("exit")) {

            // Check for exit input
            if (!stone.equalsIgnoreCase("exit")) {

                // While no valid input provided, keep asking
                while (!(1 <= Integer.parseInt(stone) && Integer.parseInt(stone) <= handSize)) {

                    // Ask to make a move
                    stone = Input.ask("Invalid input, please re-enter the number of the stone you would like to use (or EXIT to end your move)", asker);

                    // Keep asking till done
                    if (stone.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            }
        }

        return stone;
    }

    /**
     * Ask a user for a valid stone position.
     * @param asker
     * @return
     */
    public static String askForStonePosition(Object asker) {

        // Ask for position
        String position = Input.ask("At what position would you like to place this stone? (x, y)", asker);

        // If no delimeter is used, retry
        while (position.indexOf(',') == -1) {
            position = Input.ask("Invalid format, please use x,y as your input format, try again:", asker);
        }

        // Try to parse x position
        boolean invalidPosition = false;
        int x = 0;
        try {
            x = Integer.parseInt(position.split(",")[0]);
        } catch (NumberFormatException e) {
            Logger.print("Invalid x-position entered...");
            invalidPosition = true;
        }
        int y = 0;

        // No value entered after delimeter
        if (position.split(",").length < 1) {
            Logger.print("Invalid y-position entered...");
            invalidPosition = true;
        }

        // Try to parse y position
        try {
            y = Integer.parseInt(position.split(",")[1]);
        } catch (NumberFormatException e) {
            Logger.print("Invalid y-position entered...");
            invalidPosition = true;
        }

        // While user provides invalid position, ask for valid inptu
        while (invalidPosition) {

            // Indicate invalid format, retry
            position = Input.ask("Invalid format, please use x,y as your input format, try again:", asker);

            // While no delimeter is used, retry
            while (position.indexOf(',') == -1) {
                position = Input.ask("Invalid format, please use x,y as your input format, try again:", asker);
            }

            // Try to parse the x and y position
            boolean tempApprove = false;
            try {
                x = Integer.parseInt(position.split(",")[0]);
                tempApprove = true;
            } catch (NumberFormatException e) {
                Logger.print("Invalid x-position entered...");
                invalidPosition = true;
            }
            try {
                y = Integer.parseInt(position.split(",")[1]);
                if (tempApprove) invalidPosition = false;
            } catch (NumberFormatException e) {
                Logger.print("Invalid y-position entered...");
                invalidPosition = true;
            }
        }

        // Return x and y position as a string
        return x + "_" + y;
    }

    /**
     * Asks a user for a valid move. Using the ask for stone,
     * and ask for stone position methods.
     * @param client
     * @return Stone[] with valid stones
     */
    public static Stone[] askForMove(Client client) {

        // Get current hand of the player
        ArrayList<Stone> hand;

        // Make move array list, to compose move
        ArrayList<Stone> move = new ArrayList<>();

        while (true) {

            // Update current hand
            hand = client.getPlayer().getHand();

            // Print out the hand to read for the player
            Logger.print("Your current hand:");
            for (int i = 0; i < hand.size(); i++) {
                Logger.print("Stone " + (i + 1) + ": " + hand.get(i));
            }

            // Ask client to provide valid stone
            String stone = Input.askForStone(client, hand.size());

            // Break if input equals exit
            if (stone.equalsIgnoreCase("exit")) break;

            // Valid stone provided, ask for position
            String position = Input.askForStonePosition(client);
            int x = Integer.parseInt(position.split("_")[0]);
            int y = Integer.parseInt(position.split("_")[1]);

            // Add stone to move
            Stone handStone = hand.get(Integer.valueOf(stone) - 1);
            Stone moveStone = new Stone(handStone.getShape(), handStone.getColor(), x, y);
            move.add(moveStone);

            // Remove the chosen stone from the hand
            hand.remove(handStone);
        }

        // Convert to stone array
        Stone[] moveArray = new Stone[move.size()];

        // Return result
        return move.toArray(moveArray);
    }
}

