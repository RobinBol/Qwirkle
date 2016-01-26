package qwirkle.util;

import qwirkle.client.Client;
import qwirkle.gamelogic.Stone;
import qwirkle.server.Server;
import qwirkle.server.ServerLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input {

    /**
     * Handles asking the user on the server for
     * input.
     *
     * @param question Question to ask the user
     * @param asker    Object whom wants to ask something
     * @return String with the input
     */
    /*@ requires question != null && !question.isEmpty() && asker != null && typeof asker ==
     Client || typeof asker == Server */
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
     * @param asker Object whom wants to ask something
     * @return int valid port number
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server
     ensures \result >= 0 && 65535 > \result */
    public static int askForPort(Object asker) {
        return askForPort(asker, null);
    }

    /**
     * Methods that starts asking for a port, if
     * invalid input is provided it will keep asking
     * for valid input.
     *
     * @param asker       Object whom wants to ask something
     * @param enteredPort String used to recursively test input
     * @return int valid port number
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server */
    public static int askForPort(Object asker, String enteredPort) {
        String port = enteredPort;
        // If no provided as argument, ask for it
        if (port == null) {
            if (asker instanceof Server) {
                port = ask(ServerLogger.ENTER_PORT, asker);
            } else {
                port = ask("Please enter the server port:", asker);
            }
        }

        // While incorrect port ask for new one
        while (!Validation.checkPort(port)) {
            if (asker instanceof Server) {
                return Integer.parseInt(String.valueOf(askForPort(asker, ask(ServerLogger
                    .PORT_INVALID_RETRY, asker))));
            } else {
                port = String.valueOf(askForPort(asker, ask("Invalid port provided, please" +
                    " try again:", asker)));
            }
        }

        // Print that server is configured and starting
        if (asker instanceof Server) {
            Server server = (Server) asker;
            server.updateObserver(ServerLogger.SERVER_STARTED + port);
        }

        // Return valid port
        return Integer.parseInt(port);
    }

    /**
     * Ask user to give a valid host address for the server.
     *
     * @param asker Object whom wants to ask something
     * @return String valid host address
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server
     ensures Validation.checkIP(\result) */
    public static String askForHostAddress(Object asker) {
        return askForHostAddress(asker, null);
    }

    /**
     * Ask user to give a valid host address for the server.
     *
     * @param asker          Object whom wants to ask something
     * @param enteredAddress String used to recursively test input
     * @return String valid host address
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server */
    public static String askForHostAddress(Object asker, String enteredAddress) {
        String address = enteredAddress;

        // If not provided as argument, ask for it
        if (address == null) {
            address = ask("Please enter the server host IP address:", asker);
        }

        // While entered address is not valid ask for valid input
        while (!Validation.checkIP(address)) {
            address = askForHostAddress(asker, ask("Invalid hostname/ipaddress provided, " +
                "please try again:", asker));
        }

        // Valid input provided, return it
        return address;
    }


    /**
     * Ask user to give a username.
     *
     * @param asker Object whom wants to ask something
     * @return String valid username
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server
     ensures \result != null */
    public static String askForUsername(Object asker) {
        return askForUsername(asker, null);
    }

    /**
     * Ask user to give a username.
     *
     * @param asker           Object whom wants to ask something
     * @param enteredUsername String used to recursively test input
     * @return String valid username
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server */
    public static String askForUsername(Object asker, String enteredUsername) {
        String username = enteredUsername;

        // If not provided as argument, ask for it
        if (username == null) {
            username = ask("Please enter your username:", asker);
        }

        // Make sure no special characters are used
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(username);
        boolean b = m.find();

        // While input is too long, ask for smaller input
        while (username.length() > 15 || b) {
            username = askForUsername(asker, ask("Please use 15 characters or less and no " +
                "special characters, try again:", asker));
            p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            m = p.matcher(username);
            b = m.find();
        }

        // Correct input provided, return it
        return username;
    }

    /**
     * Ask user to give a valid game type.
     *
     * @param asker Object whom wants to ask something
     * @return int gameType
     */
     /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server
     ensures \result >= 0 && 6 <= \result */
    public static int askForGameType(Object asker) {
        return askForGameType(asker, null);
    }

    /**
     * Ask user to give a valid game type.
     *
     * @param asker           Object whom wants to ask something
     * @param enteredGameType String used to recursively test input
     * @return int gameType
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server */
    public static int askForGameType(Object asker, String enteredGameType) {
        String gameType = enteredGameType;

        // If not used recursively, ask first for input
        if (gameType == null) {
            gameType = ask("Please request a game type by typing the number of your " +
                "choice:\n[0] I don't care...\n[1] Versus AI\n[2] Versus one other " +
                "player\n[3] Versus two other players\n[4] Versus three other players\n[5] I " +
                "want to challenge someone\n[6] Wait for incoming invitation", asker);
        }

        // If the input is not correct, keep asking for correct input
        while (!gameType.trim().equals("0") && !gameType.trim().equals("1") &&
            !gameType.trim().equals("2")
            && !gameType.trim().equals("3") && !gameType.trim().equals("4") &&
            !gameType.trim().equals("5")
            && !gameType.trim().equals("6")) {
            gameType = ask("Invalid option, please try again (type number of your " +
                "choice:", asker);
        }

        // Correct input was provided, return it
        return Integer.parseInt(gameType);
    }

    /**
     * Ask user to give a valid opponent to challenge.
     *
     * @param asker Object whom wants to ask something
     * @return String playername
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server
     ensures \result != null */
    public static String askForOpponent(Object asker) {
        return ask("Please provide the name of the player you would like to challenge:", asker);
    }

    /**
     * Ask user for a valid stone input.
     *
     * @param asker    Object whom wants to ask something
     * @param handSize int used to recursively test input
     * @return String stone chosen
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server */
    public static String askForStone(Object asker, int handSize) {

        // Ask to make a move
        String stone = Input.ask("Please enter the number of the stone you would like to use (or " +
            "EXIT to end your move)", asker);

        // Check for exit input
        if (stone.equalsIgnoreCase("exit")) {
            return "exit";
        }

        // Detect first line of valid input
        if (!stone.equals("1") && !stone.equals("2") && !stone.equals("3") && !stone.equals("4")
            && !stone.equals("5") && !stone.equals("6") && !stone.equalsIgnoreCase("exit")) {

            int stoneNumber = 0;
            try {
                stoneNumber = Integer.parseInt(stone);
            } catch (NumberFormatException e) {
                stoneNumber = 0;
            }

            // While no valid input provided, keep asking
            while (!(1 <= stoneNumber && stoneNumber <= handSize)) {

                // Ask to make a move
                stone = Input.ask("Invalid input, please re-enter the number of the stone you " +
                    "would like to use (or EXIT to end your move)", asker);

                // Try to format an integer from the input
                try {
                    stoneNumber = Integer.parseInt(stone);
                } catch (NumberFormatException e) {
                    stoneNumber = 0;
                }

                // Keep asking till done
                if (stone.equalsIgnoreCase("exit")) {
                    break;
                }
            }

        }

        return stone;
    }

    /**
     * Ask a user for a valid stone position.
     *
     * @param asker Object whom wants to ask something
     * @return String containing stone position
     */
    /*@ requires asker != null && typeof asker ==
     Client || typeof asker == Server
     ensures \result != null */
    public static String askForStonePosition(Object asker) {

        // Ask for position
        String position = Input.ask("At what position would you like to place this stone? (x, y)" +
            "", asker);

        if (position.equalsIgnoreCase("exit")) {
            return position;
        }
        // If no delimeter is used, retry
        while (position.indexOf(',') == -1) {
            position = Input.ask("Invalid format, please use x,y as your input format, try " +
                "again:", asker);
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

        // While user provides invalid position, ask for valid input
        while (invalidPosition) {

            // Indicate invalid format, retry
            position = Input.ask("Invalid format, please use x,y as your input format, try " +
                "again:", asker);

            // While no delimeter is used, retry
            while (position.indexOf(',') == -1) {
                position = Input.ask("Invalid format, please use x,y as your input format, try " +
                    "again:", asker);
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
                if (tempApprove) {
                    invalidPosition = false;
                }
            } catch (NumberFormatException e) {
                Logger.print("Invalid y-position entered...");
                invalidPosition = true;
            }
        }

        // Return x and y position as a string
        return x + "_" + y;
    }
    
    public static int askForMoveOrTrade(Client client) {
    	
    	// Update current hand
        ArrayList<Stone> hand = client.getPlayer().getHand();

        // Log the board to let the player fill it in
        Logger.print("The current board:");
        Logger.print("\n" + client.getPlayer().getBoard());

        // Print out the hand to read for the player
        Logger.print("Your current hand:");
        for (int i = 0; i < hand.size(); i++) {
            Logger.print("Stone " + (i + 1) + ": " + hand.get(i));
        }
    	
    	while (true) {
    		String answer = ask("Type 1 for stone placement or 2 for trading stones:", client);
    		
    		if (answer.charAt(0) == '1') {
    			return 1;
    		}
    		if (answer.charAt(0) == '2') {
    			return 2;
    		} else {
    			return -1;
    		}
    	}
    }
    
    public static Stone[] askForTradeStones(Client client) {
    	List<Stone> stones = new ArrayList<>();
    	String answer = ask("Enter the numbers of the stones you want to trade with the bag, use spaces.", client);
    	List<Stone> hand = client.getPlayer().getHand(); 
    	int count = 0;
    	
    	Scanner scanner = new Scanner(answer);
    	List<Integer> indexes = new ArrayList<>(); 
    	while (scanner.hasNextInt() && count < hand.size()) {
    		int index = scanner.nextInt();
    		if (index <= hand.size() && index > 0 && !indexes.contains(index)) {
    			indexes.add(index);
    		}
    		
    	}
    	int count2 = 0;
    	for (Integer i : indexes) {
    		stones.add(hand.get(i - 1 - count2));
    		hand.remove(i - 1 - count2);
    		count2++;
    	}
    	scanner.close();
    	return stones.toArray(new Stone[stones.size()]);
    }

    /**
     * Asks a user for a valid move. Using the ask for stone,
     * and ask for stone position methods.
     *
     * @param client Client that needs to be asked for a move
     * @return Stone[] with valid stones
     */
    /*@ requires client != null
    ensures \result != null */
    public static Stone[] askForMove(Client client) {

        // Get current hand of the player
        ArrayList<Stone> hand;

        // Make move array list, to compose move
        ArrayList<Stone> move = new ArrayList<>();

        while (true) {

            // Update current hand
            hand = client.getPlayer().getHand();

            // Log the board to let the player fill it in
            Logger.print("The current board:");
            Logger.print("\n" + client.getPlayer().getBoard());

            // Print out the hand to read for the player
            Logger.print("Your current hand:");
            for (int i = 0; i < hand.size(); i++) {
                Logger.print("Stone " + (i + 1) + ": " + hand.get(i));
            }

            // Ask client to provide valid stone
            String stone = Input.askForStone(client, hand.size());

            // Break if input equals exit
            if (stone.equalsIgnoreCase("exit")) {
                break;
            }

            // Valid stone provided, ask for position
            String position = Input.askForStonePosition(client);

            // Detect break
            if (position.equalsIgnoreCase("exit")) {
                break;
            }

            // Store positions
            int x = Integer.parseInt(position.split("_")[0]);
            int y = Integer.parseInt(position.split("_")[1]);

            // Add stone to move
            Stone handStone = hand.get(Integer.valueOf(stone) - 1);
            Stone moveStone = new Stone(handStone.getColor(), handStone.getShape(), x, y);
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