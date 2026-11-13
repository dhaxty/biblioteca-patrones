package edu.biblioteca.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class HashContrasena {
    private static final int ITERACIONES = 600_000;
    private static final int BITS_HASH = 256;
    private static final int BYTES_SALT = 16;
    private static final String ALGORITMO = "pbkdf2-sha256";

    private HashContrasena() {
    }

    public static String crearHash(char[] contrasena) {
        if (contrasena == null || contrasena.length == 0) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia");
        }

        byte[] salt = new byte[BYTES_SALT];
        new SecureRandom().nextBytes(salt);

        char[] copia = contrasena.clone();
        byte[] hash = derivarHash(copia, salt);

        try {
            Base64.Encoder codificador = Base64.getUrlEncoder().withoutPadding();
            return ALGORITMO + "$" + ITERACIONES + "$"
                    + codificador.encodeToString(salt) + "$"
                    + codificador.encodeToString(hash);
        } finally {
            Arrays.fill(copia, '\0');
            Arrays.fill(hash, (byte) 0);
        }
    }

    public static boolean verificar(char[] contrasena, String hashGuardado) {
        if (contrasena == null || hashGuardado == null) {
            return false;
        }
        String[] partes = hashGuardado.split("\\$");
        if (partes.length != 4
                || !ALGORITMO.equals(partes[0])
                || !String.valueOf(ITERACIONES).equals(partes[1])) {
            return false;
        }

        try {
            Base64.Decoder decodificador = Base64.getUrlDecoder();
            byte[] salt = decodificador.decode(partes[2]);
            byte[] hashEsperado = decodificador.decode(partes[3]);

            if (salt.length != BYTES_SALT || hashEsperado.length != BITS_HASH / 8) {
                return false;
            }

            char[] copia = contrasena.clone();
            byte[] hashCalculado = derivarHash(copia, salt);

            try {
                return MessageDigest.isEqual(hashEsperado, hashCalculado);
            } finally {
                Arrays.fill(copia, '\0');
                Arrays.fill(hashCalculado, (byte) 0);
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derivarHash(char[] contrasena, byte[] salt) {
        PBEKeySpec especificacion =
                new PBEKeySpec(contrasena, salt, ITERACIONES, BITS_HASH);

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return fabrica.generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo calcular el hash.", ex);
        } finally {
            especificacion.clearPassword();
        }
    }
}
