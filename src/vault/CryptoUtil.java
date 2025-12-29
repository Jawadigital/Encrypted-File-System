package vault;

import javax.crypto.Cipher;
import java.nio.file.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int TAG_SIZE = 128;
    private static final int IV_SIZE = 12;
    private static final int SALT_SIZE = 16;
    private static final int ITERATIONS = 100000;

    /**
     * Generate random salt
     * @return salt
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Derive key from password + salt
     * @param password
     * @param salt
     * @return
     * @throws Exception
     */
    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypt data
     * @param plaintext
     * @param key
     * @return encrypted data
     * @throws Exception
     */
    public static byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

        return result;
    }

    /**
     * decrypt data
     * @param encrypted
     * @param key
     * @return decrypted data
     * @throws Exception
     */
    public static byte[] decrypt(byte[] encrypted, SecretKey key) throws Exception {
        byte[] iv = Arrays.copyOfRange(encrypted, 0, IV_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(encrypted, IV_SIZE, encrypted.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        return cipher.doFinal(ciphertext);
    }

    /**
     * Encrypt file
     * @param input
     * @param output
     * @param password
     * @return salt that must be saved
     * @throws Exception
     */
    public static byte[] encryptFile(Path input, Path output, char[] password) throws Exception {

        byte[] salt = generateSalt();
        SecretKey key = deriveKey(password, salt);

        byte[] data = Files.readAllBytes(input);
        byte[] encrypted = encrypt(data, key);
        Files.write(output, encrypted);

        return salt;
    }

    /**
     * decrypt file
     * @param input
     * @param output
     * @param password
     * @param salt
     * @throws Exception
     */
    public static void decryptFile(Path input, Path output, char[] password, byte[] salt) throws Exception {
        SecretKey key = deriveKey(password, salt);

        byte[] encrypted = Files.readAllBytes(input);
        byte[] decrypted = decrypt(encrypted, key);
        Files.write(output, decrypted);
    }

    /**
     * clear password from memory
     * @param password
     */
    public static void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
