
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsConfiguration {
    final String settingsFilePath = "D://MailApp//Server//Settings//ServerSettings.txt";
    final String bannedPhrasesFilePath = "D://MailApp//Server//Settings//BannedPhrases.txt";
    private String hostName;
    private int port;
    private static String[] bannedPhrases;

    public SettingsConfiguration(){
        loadSettings();
        loadBannedPhrases();
    }

    private void loadSettings(){
        try (BufferedReader br = new BufferedReader(new FileReader(settingsFilePath))) {
            hostName = br.readLine();
            if (hostName == null) {
                System.out.println("Error: Host name is missing or empty.");
                System.exit(1);
            }

            String portLine = br.readLine();
            if (portLine == null || portLine.trim().isEmpty()) {
                System.out.println("Error: Port is missing or empty.");
                System.exit(1);
            } else {
                port = Integer.parseInt(portLine);
            }
        } catch (IOException e) {
            System.out.println("Error reading server settings file: " + e.getMessage());
        }
    }

    private void loadBannedPhrases(){
        try (BufferedReader br = new BufferedReader(new FileReader(bannedPhrasesFilePath))) {
            bannedPhrases = br.readLine().split(" ");
        } catch (IOException e) {
            System.out.println("Error reading banned phrases file: " + e.getMessage());
        }
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public static String[] getBannedPhrases() {
        return bannedPhrases;
    }
}
