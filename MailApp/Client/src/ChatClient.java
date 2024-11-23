import javax.swing.*;

public class ChatClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9999;

        SwingUtilities.invokeLater(() -> {
            Client client = new Client(host, port);
            client.start();
        });
    }
}