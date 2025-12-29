package vault;

import javax.swing.*;

/**
 * Entry point
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(() -> {
            VaultGUI gui = null;
            try {
                gui = new VaultGUI();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}