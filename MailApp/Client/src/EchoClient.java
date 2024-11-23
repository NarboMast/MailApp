import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        Socket echoSocket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        String hostname = "localhost";
        if(args.length > 0) hostname = args[0];

        try {
            System.out.println("Trying to connect with " + hostname);
            echoSocket = new Socket(hostname, 9999);
            System.out.println("Creating communication streams");
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + hostname + ".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Connection error with " + hostname + ".");
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            System.out.println(in.readLine());
        }

        // end the job
        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}
