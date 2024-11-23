import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ChatWindow extends JFrame {
    private final JTextArea messageArea;
    private final JTextField inputField;
    private final PrintWriter out;

    public ChatWindow(PrintWriter out) {
        this.out = out;

        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    out.println(message);
                    inputField.setText("");

                    if (message.equals("/q")) {
                        System.exit(0);
                    }
                }
            }
        });
    }

    public void appendMessage(String message) {
        messageArea.append(message + "\n");
    }
}