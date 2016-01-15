package GameLogic;

import Server.ClientHandler;

import java.util.ArrayList;


public class Game {

    public static final int MAXHANDSIZE = 6;

    private Player[] players;
    private Board board;

    public Game(ArrayList<ClientHandler> clients, Lobby lobby) {

        // Let lobby know game started
        lobby.gameStarted(clients);

        // TODO check if socket connection to all clients in the game still works
        // TODO if not make sure game ends properly
        // TODO handle properly when a client disconnects:
        //clients.get(i).sendGameEnd("DISCONNECT");


        players = new Player[clients.size()];
        board = new Board();

        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(clients.get(i), board);
        }

        // TODO: create players from the clients here and start the game

    }

    public void startGame() {
    }

    ;

    public void setTurn() {
    }

    ;

    public int getScore(Player player) {
        return 0;
    }

    ;

    public void terminateGame() {
    }

    ; // Players back to lobby

    public Player getFirstPlayer() {
        for (int i = 0; i < players.length; i++) {

        }

        return null;
    }
}
