import java.io.BufferedReader;
import java.io.IOException;

class ServerListener extends Thread {
    private final BufferedReader in;
    private final ChatWindow chatWindow;

    public ServerListener(BufferedReader in, ChatWindow chatWindow) {
        this.in = in;
        this.chatWindow = chatWindow;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                chatWindow.appendMessage(message);
            }
        } catch (IOException e) {
            chatWindow.appendMessage("Error while reading from server: " + e.getMessage());
        }
    }
}
