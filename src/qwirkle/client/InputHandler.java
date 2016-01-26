package qwirkle.client;

import qwirkle.gamelogic.Player;
import qwirkle.gamelogic.Stone;
import qwirkle.util.Input;
import qwirkle.util.Logger;
import qwirkle.util.Protocol;
import qwirkle.util.ProtocolHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that assists the Client in parsing and acting upon
 * incoming messages. Runs on a seperate thread.
 */
public class InputHandler extends Thread {

    /* Keep reference to client */
    private Client client;

    /* Input to listen on */
    private BufferedReader input;

    /* Keep track of whether a move was made */
    private boolean madeMove = false;

    /**
     * Constructor takes a client an input.
     *
     * @param client Client from which input handler is created
     * @param input  BufferedReader holding the input
     */
    public InputHandler(Client client, BufferedReader input) {
        this.client = client;
        this.input = input;
    }

    /**
     * Listen on separate thread to avoid blocking the client.
     */
    public void run() {
        String incomingValue;

        // Start reading incoming messages
        try {

            // While there are messages to be read
            incomingValue = input.readLine();
            while (incomingValue != null
                && incomingValue != ""
                && incomingValue != "\\n"
                && incomingValue != "\\n\\n") {

                // Log message if present
                if (!incomingValue.isEmpty()) {
                    client.updateObserver(ClientLogger.INCOMING_MESSAGE + incomingValue);
                }

                // Read to package to a useable ArrayList
                ArrayList<Object> result = ProtocolHandler.readPackage(incomingValue);

                // Check if properly parsed data is present
                if (!result.isEmpty()) {

                    // Handle incoming messages
                    if (result.get(0).equals(Protocol.Client.ERROR)) {

                        if (result.size() > 1) {
                            handleIncomingError(Integer.valueOf((String) result.get(1)));
                        } else {
                            handleIncomingError();
                        }
                    }
                    if (result.get(0).equals(Protocol.Server.HALLO)) {

                        // Save all matching features
                        for (int i = 1; i < result.size(); i++) {
                            if (client.hasFeature(String.valueOf(result.get(i)))) {
                                client.saveMatchedFeature(String.valueOf(result.get(i)));
                            }
                        }

                        // Ask for desired game
                        getGameType();
                    } else if (result.get(0).equals(Protocol.Server.STARTGAME)) {
                        client.startGame();
                        client.setInGame(true);
                    } else if (result.get(0).equals(Protocol.Server.GAME_END)) {
                        client.setInGame(false);

                        if (result.size() >= 1) {
                            client.updateObserver(ClientLogger.GAME_ENDED_BECAUSE +
                                String.valueOf(result.get(1)));
                        } else {
                            client.updateObserver(ClientLogger.GAME_ENDED);
                        }

                        // Ask for a new game
                        getGameType();

                    } else if (result.get(0).equals(Protocol.Server.MOVE) && result.size() >= 3) {
                    	ArrayList<Stone> stones = new ArrayList<>();
                        // You made a move, (initial move?) but it was not used, reset hand
                        if (madeMove && !client.skippedTurn()
                            && !((String) result.get(1)).equalsIgnoreCase(client.getName())) {

                            // Undo last move, reset hand and board
                            client.getPlayer().undoLastMove();                          
                            
                        } 
                        if (!((String) result.get(1)).equalsIgnoreCase(client.getName())
                            && !((String) result.get(1)).equalsIgnoreCase("null")) {
                            // Loop over all stones
                            for (int i = 3; i < result.size(); i++) {

                                // Fetch positions
                                ArrayList<String> data = (ArrayList<String>) result.get(i);
                                int x = Integer.parseInt(data.get(1));
                                int y = Integer.parseInt(data.get(2));

                                // Add to arraylist
                                stones.add(new Stone(data.get(0).charAt(0), data.get(0).charAt(1), x, y));
                            }

                            // Other player made a move, add it to your board
                            if (stones.size() > 0) {
                            	 client.getPlayer().updateBoard(
                                         stones.toArray(new Stone[stones.size()]));
                            }                
                        }

                        // Check if client is his turn
                        if (((String) result.get(2)).equalsIgnoreCase(client.getName())) {

                            // You have to make move
                            client.makeMove();
                            //TODO doesnt work when skipped.
                            madeMove = true;
                            // Indicate that the last move was made by this client. 
                        } else {

                            // Other clients turn
                            madeMove = false;
                        }

                        //TODO handle input from other players
                    } else if (result.get(0).equals(Protocol.Client.INVITE) && result.size() == 2) {

                        // Only act on invite if client is not in game
                        if (!client.inGame()) {

                            int timeout = 14000;
                            // Create timer
                            Timer timer = new java.util.Timer();
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    Logger.print("Timeout expired, automatically declined. " +
                                        "Please type 'exit' to continue...");
                                }
                            };
                            timer.schedule(timerTask, timeout);

                            // Incoming invite
                            String answer = respondToChallenge();

                            // Check answer, yes or no
                            if (answer.equalsIgnoreCase("yes")) {

                                // Reset timer
                                timer.cancel();
                                timer.purge();

                                // Send accept invite to server
                                client.sendMessage(ProtocolHandler.createPackage(
                                    Protocol.Client.ACCEPTINVITE));

                            } else {
                                // Reset timer
                                timer.cancel();
                                timer.purge();

                                // Send decline invite to server
                                client.sendMessage(ProtocolHandler.createPackage(
                                    Protocol.Client.DECLINEINVITE));

                                // Ask for new game
                                getGameType();
                            }
                        } else { // In game, decline

                            // Send decline invite to server
                            client.sendMessage(ProtocolHandler.createPackage(
                                Protocol.Client.DECLINEINVITE));
                        }
                    } else if (result.get(0).equals(Protocol.Client.DECLINEINVITE)
                        && !client.inGame()) {
                        getGameType();
                    } else if (result.get(0).equals(Protocol.Server.ADDTOHAND)) {
                        for (int i = 1; i < result.size(); i++) {
                        	String string = (String)result.get(i); 
                            Stone stone = new Stone(string.charAt(0), string.charAt(1));
                            client.getPlayer().addStoneToHand(stone);
                        }
                        //make sure stones aren't undone when traded. Usually getting stones mean u didn't do a move.
                        client.getPlayer().getBoard().removeLastMoves();
                        client.getPlayer().saveHand();
                    }
                }

                // Read new input
                incomingValue = input.readLine();
            }
        } catch (IOException | NoSuchElementException e) {

            // Something went wrong when reading
            client.updateObserver(ClientLogger.SOCKET_ERROR);
        }
    }

    //TODO: moved make move away from here, make sure this is replaced if it didnt work out in player.

    /**
     * Let the client enter a preferred game type and
     * send it to the server.
     */
    public void getGameType() {
        int gameType = Input.askForGameType(client);

        // Handle making a challenge
        if (gameType == 5) {

            // Get desired opponent
            String opponent = Input.askForOpponent(client);

            // Update observer, looking for game
            client.updateObserver(ClientLogger.SEARCHING_GAME);

            // Create and send package to request a game at the server, against a specific player
            ArrayList<Object> parameters = new ArrayList<>();
            parameters.add(opponent);
            client.sendMessage(ProtocolHandler.createPackage(Protocol.Client.INVITE, parameters));
        } else if (gameType == 6) {
            client.updateObserver(ClientLogger.WAITING);
        } else {

            // Update observer, looking for game
            client.updateObserver(ClientLogger.SEARCHING_GAME);

            // Create and send package to request a game at the server, of this game type
            ArrayList<Object> parameters = new ArrayList<>();
            parameters.add(String.valueOf(gameType));
            client.sendMessage(ProtocolHandler.createPackage(
                Protocol.Client.REQUESTGAME, parameters));
        }
    }

    /**
     * Get users choice, whether he wants to engage or not.
     *
     * @return String yes/no
     */
    public String respondToChallenge() {
        String answer = Input.ask(ClientLogger.ASK_CHALLENGE, client);

        while (!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")
            && !answer.equalsIgnoreCase("exit")) {
            answer = Input.ask(ClientLogger.ANSWER_INVALID, client);
        }

        return answer;
    }

    /**
     * This method handles all incoming errors.
     */
    public void handleIncomingError() {
        handleIncomingError(-1);
    }

    /**
     * This method handles all incoming errors.
     *
     * @param errorCode specific error code of error
     */
    public void handleIncomingError(int errorCode) {

        // Username already exists
        if (errorCode == 4) {
            client.updateObserver(ClientLogger.USERNAME_EXISTS);
            System.exit(0);
        } else if (errorCode == 5) {

            // Log that opponent is not challengable
            client.updateObserver(ClientLogger.NOT_CHALLENGABLE);

            // Ask for new game
            getGameType();

        } else if (errorCode == 7) {

            // Invalid move, try to undo
            Player player = client.getPlayer();
            if (player != null) {
                if (player.hasTurn()) {
                    player.undoLastMove();
                }
            }
        } else if (errorCode == -1) {
            System.out.println("Undefined error incoming");
        }
    }
}
