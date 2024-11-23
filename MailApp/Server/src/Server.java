import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        SettingsConfiguration settings = new SettingsConfiguration();

        ServerSocket echoServer = null;
        Socket clientSocket = null;
        int port = settings.getPort();
        String hostName = settings.getHostName();

        try {
            System.out.println("Attempt to create a server socket");
            echoServer = new ServerSocket(port, 10, InetAddress.getByName(hostName));
            System.out.println("Socket created with port: " + port + " and with name: " + hostName);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        while(true) {
            System.out.println("Waiting for accept");
            clientSocket = echoServer.accept();
            System.out.println("A client has connected:");
            (new Thread(new ClientHandler(clientSocket))).start();
        }
    }
}