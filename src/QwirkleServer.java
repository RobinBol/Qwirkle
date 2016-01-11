
public class QwirkleServer(){

        public QwirkleServer();
        public boolean startServer();
        public Lobby createLobby();
        public void listenForClients(){
                //create Protocolhandler to listen on socket
        };
        public Player createPlayer();
}