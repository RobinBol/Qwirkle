import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StandaloneClient {

    private static int port;
    private static String host;

    public StandaloneClient(InetAddress host, int port) {
        this.port = port;
        this.host = host.getHostAddress();

        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(this.host, this.port);

            InputStream inputstream = System.in;
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            String string = null;
            while ((string = bufferedreader.readLine()) != null) {
                bufferedwriter.write(string + '\n');
                bufferedwriter.flush();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore","../keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","password");

        try {
            StandaloneClient c = new StandaloneClient(InetAddress.getLocalHost(), 6090);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
