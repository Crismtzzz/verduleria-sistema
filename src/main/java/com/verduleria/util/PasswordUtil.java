package com.verduleria.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash seguro de contraseñas con PBKDF2-HMAC-SHA256 (sin dependencias externas). */
public final class PasswordUtil {
    private static final int ITERACIONES = 120_000;
    private static final int LONG_SALT = 16;
    private static final int LONG_HASH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    /** Formato: pbkdf2$iteraciones$salt(base64)$hash(base64). */
    public static String hash(String password) {
        byte[] salt = new byte[LONG_SALT];
        RANDOM.nextBytes(salt);
        byte[] h = derivar(password.toCharArray(), salt, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(h);
    }

    public static boolean verificar(String password, String almacenado) {
        try {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 4 || !partes[0].equals("pbkdf2")) return false;
            int it = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] obtenido = derivar(password.toCharArray(), salt, it);
            return MessageDigest.isEqual(esperado, obtenido);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] pass, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pass, salt, iteraciones, LONG_HASH * 8);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        }
    }
}
