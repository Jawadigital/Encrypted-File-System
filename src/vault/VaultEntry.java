package vault;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Metadata for one encrypted file
 */
public class VaultEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String originalName;
    private long originalSize;
    private String encryptedName;
    private LocalDateTime timestamp;
    private byte[] salt;

    public VaultEntry(String id, String originalName, long originalSize,
                      String encryptedName, byte[] salt) {
        this.id = id;
        this.originalName = originalName;
        this.originalSize = originalSize;
        this.encryptedName = encryptedName;
        this.timestamp = LocalDateTime.now();
        this.salt = salt;
    }

    public String getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public String getEncryptedName() {
        return encryptedName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public byte[] getSalt() {
        return salt;
    }

    public String getFormattedSize() {
        if (originalSize < 1024)
            return originalSize + " B";
        if (originalSize < 1024 * 1024)
            return String.format("%.2f KB", originalSize / 1024.0);
        if (originalSize < 1024 * 1024 * 1024)
            return String.format("%.2f MB", originalSize / (1024.0 * 1024));
        return String.format("%.2f GB", originalSize / (1024.0 * 1024 * 1024));
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
