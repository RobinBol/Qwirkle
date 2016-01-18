package qwirkle.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class extends observable and will be used
 * for Server- and ClientLog classes. They have shared
 * methods and are therefore inheriting from this parent
 * class.
 */
public class Logger {

    /**
     * Handles printing a message.
     *
     * @param parameter Message to print
     */
    public static void print(Object parameter) {

        // Create date object
        Date date = new Date();

        // Get current time in millis
        long time = date.getTime();

        // Create new timestamp from current time
        Timestamp ts = new Timestamp(time);
        String logItem = new SimpleDateFormat("HH:mm:ss").format(ts) + " " + parameter;

        System.out.println(logItem);
    }
}
