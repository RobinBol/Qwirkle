package Server;

import java.util.Observable;
import java.util.Observer;

/**
 * ServerTUI, acts as an observer to the ServerLog.
 * On new updated input it will log this input to
 * the console.
 */
public class ServerTUI implements Observer{

    public ServerTUI (){

    }

    /**
     * Incoming update, log it to the system out.
     * @param obs Observable instance
     * @param x Updated data
     */
    public void update(Observable obs, Object x){
        System.out.println(x);
    }
}
