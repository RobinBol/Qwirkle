package qwirkle.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import qwirkle.client.Client;
import qwirkle.gamelogic.Lobby;
import qwirkle.server.ClientHandler;
import qwirkle.server.Server;
import qwirkle.util.Protocol;

import java.net.InetAddress;
import java.util.ArrayList;


public class ClientHandlerTest {
    int port = 6000;
    Server server;
    ClientHandler clientHandler;
    Client client;
    Client client2;
    String clientName = "Henk";
    String host = "localhost";

    @Before
    public void setUp() throws Exception {
        Thread one = new Thread() {
            public void run() {
                server = new Server(port);
                server.startServer();
            }
        };

        one.start();
        client = new Client(clientName, InetAddress.getByName(host), port);

        // Start client on new thread
        new Thread(client).start();
        
        Thread.sleep(2000);
        
        client2 = new Client("Pieter", InetAddress.getByName(host), port);
        new Thread(client2).start();

        
        Thread.sleep(2000);
        
        clientHandler = server.getClientHandler(clientName);
        assertNotNull(clientHandler);
    }

    @Test
    public void clientShouldHaveFeature() throws Exception {
        clientHandler = server.getClientHandler(clientName);
        assertTrue(clientHandler.hasFeature(Protocol.Server.Features.CHALLENGE));
    }

    @Test
    public void shouldSetGameState() throws Exception {
        clientHandler = server.getClientHandler(clientName);
        assertFalse(clientHandler.getGameState());
        clientHandler.setGameState(true);
        assertTrue(clientHandler.getGameState());
        clientHandler.setGameState(false);
        assertFalse(clientHandler.getGameState());
    }

    @Test
    public void shouldPrintClientName() throws Exception {
        clientHandler = server.getClientHandler(clientName);
        assertEquals(clientHandler.toString(), clientName);
    }

    @Test
    public void shouldGetLobby() throws Exception {
        clientHandler = server.getClientHandler(clientName);
        assertNotNull(clientHandler.getLobby());
    }

    @Test
    public void shouldSendMessage() throws Exception {
        clientHandler = server.getClientHandler(clientName);
        clientHandler.sendDeclineInvite();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {

        }
    }


    @Test
    public void shouldRemoveClient() throws Exception {
        clientHandler = server.getClientHandler(clientName);
        Lobby lobby = clientHandler.getLobby();
        assertNotNull(lobby);
        assertTrue(lobby.hasClient(clientHandler));
        clientHandler.disconnectClient();
        assertFalse(lobby.hasClient(clientHandler));
        assertNull(server.getClientHandler(clientName));
    }
}
