package vault;

import java.io.*;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Secure file deletion
 * 3-pass overwrite before deletion
 */
public class SecureDelete {

    /**
     * Securely delete a file
     * @param file
     * @return
     */
    public static boolean delete(File file) {
        if (!file.exists()) {
            return false;
        }

        try {
            long size = file.length();

            overwrite(file, size, true);

            overwrite(file, size, false);

            overwrite(file, size, true);

            return file.delete();

        } catch (Exception e) {
            System.err.println("Secure delete failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Overwrite file with data
     * @param file
     * @param size
     * @param random
     * @throws IOException
     */
    public static void overwrite(File file, long size, boolean random) throws IOException {

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rws")) {
            randomAccessFile.seek(0);

            byte[] buffer = new byte[4096];
            SecureRandom secureRandom = new SecureRandom();

            long remaining = size;
            while (remaining > 0) {
                int chunk = (int) Math.min(buffer.length, remaining);

                if (random) {
                    secureRandom.nextBytes(buffer);
                } else {
                    Arrays.fill(buffer, (byte) 0xFF);
                }

                randomAccessFile.write(buffer, 0, chunk);
                remaining -= chunk;
            }
            randomAccessFile.getFD().sync();

        }
    }

}
