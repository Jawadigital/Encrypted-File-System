package vault;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class VaultGUI extends JFrame {

    private VaultManager vault;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private static final String[] COLUMNS = {"File Name", "Size", "Encrypted Date"};

    public VaultGUI() throws Exception {
        vault = new VaultManager();
        setupUI();
        loadFiles();
    }

    private void setupUI() {
        setTitle("Encrypted File Vault");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Encrypted File Vault", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        setupTable();
        add(new JScrollPane(fileTable), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.EAST);

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(25);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton encryptBtn = new JButton("Encrypt");
        JButton decryptBtn = new JButton("Decrypt");
        JButton removeBtn = new JButton("Remove");
        JButton refreshBtn = new JButton("Refresh");
        JButton exitBtn = new JButton("Exit");

        encryptBtn.addActionListener(e -> encryptFile());
        decryptBtn.addActionListener(e -> decryptFile());
        removeBtn.addActionListener(e -> removeFile());
        refreshBtn.addActionListener(e -> loadFiles());
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(encryptBtn);
        panel.add(decryptBtn);
        panel.add(removeBtn);
        panel.add(refreshBtn);
        panel.add(exitBtn);

        return panel;
    }

    private void loadFiles() {
        tableModel.setRowCount(0);

        for (VaultEntry entry : vault.getAllEntries()) {
            tableModel.addRow(new Object[]{
                    entry.getOriginalName(),
                    entry.getFormattedSize(),
                    entry.getFormattedTime()
            });
        }

        statusLabel.setText("Files: " + vault.getAllEntries().size());
    }

    private void encryptFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(4,1,5, 5));
        panel.add(new JLabel("Enter Password"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm password"));
        panel.add(confirmField);

        int ok = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Set Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (ok != JOptionPane.OK_OPTION) return;

        char[] password = passwordField.getPassword();
        char[] confirm  = confirmField.getPassword();

        if (password.length == 0 || !java.util.Arrays.equals(password, confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            CryptoUtil.clearPassword(password);
            CryptoUtil.clearPassword(confirm);
            return;
        }

        try {
            vault.addFile(file, password);
            loadFiles();
            statusLabel.setText("Encrypted: " + file.getName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Encryption failed");
        } finally {
            CryptoUtil.clearPassword(password);
            CryptoUtil.clearPassword(confirm);
        }
    }

    private void decryptFile() {
        int row = fileTable.getSelectedRow();
        if (row == -1) return;

        VaultEntry entry = vault.getEntry(row);

        JPasswordField passwordField = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(
                this,
                passwordField,
                "Enter Your Password",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (ok != JOptionPane.OK_OPTION) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(entry.getOriginalName()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        char[] password = passwordField.getPassword();

        try {
            vault.exportFile(String.valueOf(row), chooser.getSelectedFile(), password);
            statusLabel.setText("Decrypted: " + entry.getOriginalName());
            loadFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Decryption failed");
        } finally {
            CryptoUtil.clearPassword(password);
        }
    }

    private void removeFile() {
        int row = fileTable.getSelectedRow();
        if (row == -1) return;

        try {
            vault.removeFile(String.valueOf(row));
            loadFiles();
            statusLabel.setText("File removed");
        } catch (Exception ignored) {
        }
    }
}
