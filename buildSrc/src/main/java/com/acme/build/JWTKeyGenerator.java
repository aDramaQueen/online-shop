package com.acme.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generator to create random {@link Base64} strings for JWT token generation
 */
public class JWTKeyGenerator extends BaseBuildUtility {

    private final static String IDENTIFIER = "jwt.key = ";
    private final static int BIT_LENGTH = 512;

    /**
     * Reads given property {@link File} ;&amp searches for specific key word ({@link #IDENTIFIER}) at the beginning of
     * each line. If beginning of line matches, the line will be rewritten with a new generated Base64 key. If the
     * beginning not matches, the line stays untouched and will be rewritten as it was.
     *
     * @param properties File with default JWT key
     * @throws IOException If file does not exist, is corrupted, etc. etc.
     * @throws NoSuchAlgorithmException If no algorithm is available for key generation is available
     */
    public static void generateRandomKey(File properties) throws IOException, NoSuchAlgorithmException {
        boolean found = false;
        StringBuilder stringBuilder = new StringBuilder();
        for(String line: Files.readAllLines(properties.toPath(), charset)) {
            if(line.startsWith(IDENTIFIER)) {
                stringBuilder.append(IDENTIFIER).append(generateKey()).append("\n");
                found = true;
            } else {
                stringBuilder.append(line).append("\n");
            }
        }
        if(found) {
            writeToFileByReplace(properties, stringBuilder);
        } else {
            throw new IllegalStateException("Couldn't find identifier '%s' in property file: '%s'".formatted(IDENTIFIER, properties));
        }
    }

    /**
     * Generates random byte array with predefined length ;&amp returns converted Base64 string
     *
     * @see Base64.Encoder#encodeToString(byte[])
     * @return {@link String} representation of Base64 key with predefined bit-length
     * @throws NoSuchAlgorithmException If no algorithm is available
     */
    private static String generateKey() throws NoSuchAlgorithmException {
        byte[] bytes = new byte[BIT_LENGTH / 8];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, 0, encoded.length, charset);
    }
}
