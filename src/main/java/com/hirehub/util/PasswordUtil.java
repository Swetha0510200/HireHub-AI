package com.hirehub.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {}

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return ENCODER.encode(password);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        // Support standard BCrypt
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return ENCODER.matches(rawPassword, encodedPassword);
        }
        // Fallback backward-compatibility check if old PBKDF2 format exists
        if (encodedPassword.contains(":")) {
            try {
                String[] parts = encodedPassword.split(":", 2);
                byte[] salt = java.util.Base64.getDecoder().decode(parts[0]);
                byte[] expected = java.util.Base64.getDecoder().decode(parts[1]);

                javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                        rawPassword.toCharArray(), salt, 120000, 256);

                byte[] actual = javax.crypto.SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .getEncoded();

                return java.security.MessageDigest.isEqual(expected, actual);
            } catch (Exception ignored) {}
        }
        return false;
    }
}
