package vault;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Core Vault manager
 * each file has own password
 * Automatic secure deletion after encryption
 */
public class VaultManager {
    private static final String VAULT_DIR = "vault_data";
    private static final String FILES_DIR = "vault_data/files";
    private static final String VAULT_FILE = "vault_data/vault.dat";

    private List<VaultEntry> entries;

    /**
     * Initialize vault
     * @throws Exception
     */
    public VaultManager() throws Exception {
        Files.createDirectories(Paths.get(FILES_DIR));
        entries = loadMetaData();
    }

    /**
     * Load metadata
     * @return
     */
    private List<VaultEntry> loadMetaData() {
        Path valtPath = Paths.get(VAULT_FILE);

        if (!Files.exists(valtPath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream(valtPath.toFile()))) {
            return (List<VaultEntry>) objectInputStream.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Save metadata
     * @throws IOException
     */
    private void saveMetadata() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(VAULT_FILE))) {
            oos.writeObject(entries);
        }
    }

    public String addFile(File inputFile, char[] password) throws Exception {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password required");
        }

        String id = UUID.randomUUID().toString();
        String encryptedName = id + ".enc";
        Path encryptedPath = Paths.get(FILES_DIR, encryptedName);

        byte[] salt = CryptoUtil.encryptFile(inputFile.toPath(), encryptedPath, password);

        VaultEntry entry = new VaultEntry(
                id,
                inputFile.getName(),
                inputFile.length(),
                encryptedName,
                salt
        );

        entries.add(entry);
        saveMetadata();

        SecureDelete.delete(inputFile);

        CryptoUtil.clearPassword(password);

        return id;
    }

    public void removeFile(String id) throws Exception{
        VaultEntry entry = findEntry(id);
        if (entry == null) {
            throw new IllegalArgumentException("File not found: " + id);
        }

        Path encryptedPath = Paths.get(FILES_DIR, entry.getEncryptedName());
        Files.deleteIfExists(encryptedPath);

        entries.remove(entry);
        saveMetadata();
    }

    /**
     * export a file
     * and decrypt a fil
     * @param id
     * @param outputFile
     * @param password
     * @throws Exception
     */
    public void exportFile(String id, File outputFile, char[] password) throws Exception {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password required");
        }

        VaultEntry entry = findEntry(id);
        if (entry == null) {
            throw new IllegalArgumentException("File not found: " + id);
        }

        Path encryptedPath = Paths.get(FILES_DIR, entry.getEncryptedName());

        CryptoUtil.decryptFile(
                encryptedPath,
                outputFile.toPath(),
                password,
                entry.getSalt()
        );
        removeFile(id);
        CryptoUtil.clearPassword(password);
    }

    /**
     * return a entry by index
     * @param index
     * @return
     */
    public VaultEntry getEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            return entries.get(index);
        }
        return null;
    }

    /**
     * find an entry by id
     * @param identifier
     * @return
     */
    public VaultEntry findEntry(String identifier) {
        try {
            int index = Integer.parseInt(identifier);
            if (index >= 0 && index < entries.size()) {
                return entries.get(index);
            }
        } catch (NumberFormatException ignored) {}

        // Try as ID
        for (VaultEntry entry : entries) {
            if (entry.getId().equals(identifier) ||
                    entry.getId().startsWith(identifier)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * returns all the entries
     * @return
     */
    public List<VaultEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }
}
