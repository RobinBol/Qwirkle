package qwirkle.server;

import qwirkle.gamelogic.Board;
import qwirkle.util.Logger;

import java.util.Observable;
import java.util.Observer;

/**
 * ServerTUI, acts as an observer to the ServerLog.
 * On new updated input it will log this input to
 * the console.
 */
public class ServerTUI implements Observer {

    public ServerTUI() {

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
