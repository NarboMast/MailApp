import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private static final HashMap<String, ClientHandler> clients = new HashMap<>();
    String[] bannedPhrases = SettingsConfiguration.getBannedPhrases();

    Socket clientSocket = null;
    BufferedReader in = null;
    PrintWriter out = null;

    ClientHandler(Socket socket) {
        clientSocket=socket;
    }

    @Override
    public void run() {

        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            //Registration
            out.println("Type a username to continue");
            String username;
            while (true) {
                username = in.readLine();
                synchronized (clients) {
                    if (!clients.containsKey(username)) {
                        clients.put(username, this);
                        broadcastJoinLeave(username, true);
                        break;
                    } else {
                        out.println("Username already taken. Please choose another:");
                    }
                }
            }

            //Connected Client List provided to client

            //Greeting and introduction
            out.println(greeting(username));

            //List of connected clients
            getConnectedClientsList();

            //Listener for inputs
            String input;
            while((input = in.readLine()) != null){
                messageHandler(input);
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void getConnectedClientsList(){
        out.print("Connected clients you may chat with: ");
        synchronized (clients) {
            out.println(clients.keySet());
        }
    }

    private String greeting(String username){
        return "Welcome, " + username + "!\n"
                + "By default you send messages to all. PLease type \"/c\" to get useful commands.";
    }

    private String usefulCommands(){
        return "Useful commands: \n" +
                "1. \"/q\" - to quit program\n" +
                "2. \"/one\" - to send a message to a specific person using their username\n" +
                "3. \"/multi\" - to send a message to multiple specific people\n" +
                "4. \"/not\" - to send a message to every other connected client, with exception to some people\n" +
                "5. \"/rules\" - to get list of banned phrases\n" +
                "6. \"/list\" to get list of connected clients\n";
    }

    private void broadcastJoinLeave(String username, boolean isJoining) {
        String message = isJoining ? username + " has joined the chat." : username + " has left the chat.";
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                client.out.println(message);
            }
        }
    }

    public String getKeyByValue() {
        synchronized (clients) {
            for (HashMap.Entry<String, ClientHandler> entry : clients.entrySet()) {
                if (entry.getValue() == this) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void toAll(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                if (client != this) {
                    client.out.println(getKeyByValue() + ": " + message);
                }
            }
        }
    }

    private boolean checkForUser(String username){
        synchronized (clients) {
            if(clients.containsKey(username)) return true;
        }
        return false;
    }

    private void disconnect() {
        out.println("Bye bye!");
        synchronized (clients) {
            broadcastJoinLeave(getKeyByValue(), false);
            clients.remove(getKeyByValue());
        }
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing client resources: " + e.getMessage());
        }
    }


    private void toOne(String username, String message) {
        synchronized (clients) {
            clients.get(username).out.println(getKeyByValue() + ": " + message);
        }
    }

    private void toMulti(String[] usernames, String message) {
        synchronized (clients) {
            for (String username : usernames) {
                clients.get(username).out.println(getKeyByValue() + ": " + message);
            }
        }
    }

    private void toNot(String[] excludedUsernames, String message) {
        Set<String> excludedSet = new HashSet<>(Arrays.asList(excludedUsernames));

        synchronized (clients) {
            for (String key : clients.keySet()) {
                if (!excludedSet.contains(key)) {
                    clients.get(key).out.println(getKeyByValue() + ": " + message);
                }
            }
        }
    }

    public boolean checkBannedPhrases(String message){
        String normalizedMessage = message.toLowerCase();
        for (int i = 0; i < bannedPhrases.length; i++) {
            if (normalizedMessage.contains(bannedPhrases[i].toLowerCase())) {
                out.println("Banned phrase detected: " + bannedPhrases[i].toLowerCase() + ". Message is not delivered");
                return false;
            }
        }
        return true;
    }

    private boolean usernamesValid(String[] usernames){
        boolean valid = true;

        for(String username : usernames){
            if(!checkForUser(username)){
                out.println("Invalid username: " + username);
                valid = false;
            }
        }
        return valid;
    }

    private void messageHandler(String message) throws IOException {
        String[] words = message.split(" ", 2);

        switch (words[0]){
            case "/q":
                disconnect();
                break;
            case "/one":
                out.println("Provide the username of the recipient:");
                String recipient;

                while (true) {
                    recipient = in.readLine();
                    if (recipient.equals("/b")) {
                        break;
                    }
                    if (checkForUser(recipient)) {
                        out.println("Now, you may type your message to " + recipient + ". Type \"/b\" to go back to default.");
                        String input1;
                        while (!(input1 = in.readLine()).equals("/b")) {
                            if (checkBannedPhrases(input1)) {
                                toOne(recipient, input1);
                            }
                        }
                        break;
                    } else {
                        out.println("Invalid username: " + recipient + ". Try again or type \"/b\" to go back to default.");
                    }
                }
                out.println("Back to default mode.");
                break;
            case "/multi":
                out.println("Provide usernames of recipient divided by space");
                String[] multi = in.readLine().split(" ", clients.size()+1);
                if (!usernamesValid(multi)) {
                    out.println("Some usernames are invalid. Going back to default...");
                    out.println("Back to default mode.");
                    break;
                }

                out.println("Now, you may type your message to " + Arrays.toString(multi) + ". Type \"/b\" to go back to default");
                String input2;
                while(!(input2 = in.readLine()).equals("/b")){
                    if(checkBannedPhrases(input2)) {
                        toMulti(multi, input2);
                    }
                }
                out.println("Back to default mode.");
                break;
            case "/not":
                out.println("Provide usernames of removed recipients divided by space");
                String[] not = in.readLine().split(" ", clients.size()+1);
                if (!usernamesValid(not)) {
                    out.println("Some usernames are invalid. Going back to default...");
                    out.println("Back to default mode.");
                    break;
                }
                out.println("Now, you may type your message to everyone except "+ Arrays.toString(not) +". Type \"/b\" to go back to default");
                String input3;
                while(!(input3 = in.readLine()).equals("/b")){
                    if(checkBannedPhrases(input3)) {
                        toNot(not, input3);
                    }
                }
                out.println("Back to default mode.");
                break;
            case "/rules":
                this.out.println(Arrays.toString(bannedPhrases));
                break;
            case "/c":
                this.out.println(usefulCommands());
                break;
            case "/list":
                getConnectedClientsList();
                break;
            default:
                if(checkBannedPhrases(message)) {
                    toAll(message);
                }
                break;
        }
    }
}
