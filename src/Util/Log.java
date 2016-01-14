package Util;

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
}
