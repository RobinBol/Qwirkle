package qwirkle.client;

import qwirkle.gamelogic.Board;
import qwirkle.util.Logger;

import java.util.Observable;
import java.util.Observer;

/**
 * ClientTUI, acts as an observer to the ClientLog.
 * On new updated input it will log this input to
 * the console.
 */
public class ClientTUI implements Observer {

    public ClientTUI() {

    }

    /**
     * Incoming update, log it to the system out.
     *
     * @param obs Observable instance
     * @param x   Updated data
     */
    public void update(Observable obs, Object x) {

        // Draw board
        if (x instanceof Board) {
            Logger.print(x);
        }

        // Log regular message
        Logger.print(String.valueOf(x));
    }
}
