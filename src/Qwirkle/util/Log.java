package qwirkle.util;

import qwirkle.gamelogic.Board;
import qwirkle.gamelogic.Coordinate;
import qwirkle.gamelogic.Stone;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Observable;
import java.util.Scanner;

/**
 * This class extends observable and will be used
 * for Server- and ClientLog classes. They have shared
 * methods and are therefore inheriting from this parent
 * class.
 */
public class Log extends Observable {

    /**
     * Handles printing a message on the client.
     *
     * @param message Message to print
     */
    public void print(String message) {

        // Create date object
        Date date = new Date();

        // Get current time in millis
        long time = date.getTime();

        // Create new timestamp from current time
        Timestamp ts = new Timestamp(time);

        // Add timestamp to message
        String logItem = ts + ": " + message;

        // Notify observers of new message
        setChanged();
        notifyObservers(logItem);
    }

    /**
     * Handles asking the user on the server for
     * input.
     *
     * @param question Question to ask the user
     */
    public String askInput(String question) {

        // Print question
        print(question);

        // Create new scanner to listen for input
        Scanner sc = new Scanner(System.in);

        return sc.nextLine();
    }

    /**
     * Draw the current state of the board to System.out.
     *
     * @param board The board to draw
     */
    public static void drawBoard(Board board) {

        // Calculate boardSize
        int[] boardSize = board.getBoardWidthHeight();
        int maxSize = Math.max(boardSize[0], boardSize[1]);
        int middle = (maxSize / 2) + 4;

        // For loop that will loop over the stones on the board and print them
        for (int i = 0 - middle; i < maxSize + 11 - middle; i++) {
            System.out.print("|");
            for (int j = 0 - middle; j < maxSize + 11 - middle; j++) {
                Stone stone = board.getBoard().get(Coordinate.getCoordinateHash(j, i));
                if (stone != null) {
                    System.out.print(stone.getShape() + "" + stone.getColor());
                } else {
                    System.out.print("NN");
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }
}
