package cargomanage.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hash(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainText, BCrypt.gensalt(10));
    }

    public static boolean verify(String plainText, String hashed) {
        if (plainText == null || hashed == null) return false;
        try {
            if (hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$")) {
                return BCrypt.checkpw(plainText, hashed);
            }
            return plainText.equals(hashed);
        } catch (Exception e) {
            return false;
        }
    }
}

