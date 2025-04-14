// SecureFileEncryption GUI Project - Java Swing + AES Encryption
// Dependencies: Only Java SE

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.LocalDateTime;

public class SecureFileEncryptionGUI extends JFrame {
    private static final String LOG_FILE = "securefile.log";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    private JTextField fileField;
    private JPasswordField passwordField;
    private JButton encryptButton, decryptButton, browseButton;
    private JTextArea logArea;

    public SecureFileEncryptionGUI() {
        setTitle("SecureFile: Java File Encryptor");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 1));

        fileField = new JTextField();
        passwordField = new JPasswordField();

        browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> browseFile());

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(new JLabel("File Path:"), BorderLayout.WEST);
        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.add(new JLabel("Password:"), BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        topPanel.add(filePanel);
        topPanel.add(passwordPanel);

        JPanel buttonPanel = new JPanel();
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);

        topPanel.add(buttonPanel);
        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        encryptButton.addActionListener(this::encryptAction);
        decryptButton.addActionListener(this::decryptAction);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            fileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private SecretKeySpec createKey(String password) throws Exception {
        byte[] key = password.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        return new SecretKeySpec(key, ALGORITHM);
    }

    private void encryptAction(ActionEvent e) {
        processFile(true);
    }

    private void decryptAction(ActionEvent e) {
        processFile(false);
    }

    private void processFile(boolean isEncrypt) {
        String filePath = fileField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (filePath.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "File and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(this, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            SecretKeySpec key = createKey(password);
            File outputFile;

            if (isEncrypt) {
                outputFile = new File(filePath + ".enc");
                doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
                log("ENCRYPTED", inputFile.getName());
                logArea.append("File encrypted: " + outputFile.getAbsolutePath() + "\n");
            } else {
                if (!filePath.endsWith(".enc")) {
                    JOptionPane.showMessageDialog(this, "Invalid encrypted file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                outputFile = new File(filePath.replace(".enc", ".dec"));
                doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
                log("DECRYPTED", inputFile.getName());
                logArea.append("File decrypted: " + outputFile.getAbsolutePath() + "\n");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            log("FAILED", inputFile.getName());
        }
    }

    private void doCrypto(int cipherMode, SecretKeySpec key, File inputFile, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(cipherMode, key);
        byte[] inputBytes = Files.readAllBytes(inputFile.toPath());
        byte[] outputBytes = cipher.doFinal(inputBytes);
        Files.write(outputFile.toPath(), outputBytes);
    }

    private void log(String action, String fileName) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            String entry = String.format("[%s] %s : %s\n", LocalDateTime.now(), action, fileName);
            fw.write(entry);
        } catch (IOException e) {
            logArea.append("Log writing failed.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SecureFileEncryptionGUI app = new SecureFileEncryptionGUI();
            app.setVisible(true);
        });
    }
}
