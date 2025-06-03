package com.example.SGHS4.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AESUtil {

    private static final Logger logger = Logger.getLogger(AESUtil.class.getName());

    private static final String MASTER_KEY = "master-key-very-secret"; // à sécuriser ailleurs si possible
    private static final byte[] SALT = "sghs-hospital-salt".getBytes(); // sel fixe

    private static SecretKeySpec getKey(String baseKey) throws Exception {
        int iterations = 65536;
        int keyLength = 256;

        PBEKeySpec spec = new PBEKeySpec(baseKey.toCharArray(), SALT, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encrypt(String strToEncrypt, String key) throws Exception {
        if (strToEncrypt == null || strToEncrypt.isEmpty()) return null;

        SecretKeySpec secretKey = getKey(key);
        IvParameterSpec iv = generateIv();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));

        byte[] ivAndEncrypted = new byte[iv.getIV().length + encrypted.length];
        System.arraycopy(iv.getIV(), 0, ivAndEncrypted, 0, iv.getIV().length);
        System.arraycopy(encrypted, 0, ivAndEncrypted, iv.getIV().length, encrypted.length);

        return Base64.getEncoder().encodeToString(ivAndEncrypted);
    }

    public static String decrypt(String strToDecrypt, String key) throws Exception {
        if (strToDecrypt == null || strToDecrypt.isEmpty()) {
            logger.warning("Chaîne à déchiffrer vide.");
            return null;
        }

        byte[] ivAndEncrypted = Base64.getDecoder().decode(strToDecrypt);
        if (ivAndEncrypted.length < 17) throw new IllegalArgumentException("Données invalides.");

        byte[] iv = new byte[16];
        System.arraycopy(ivAndEncrypted, 0, iv, 0, 16);

        byte[] encryptedBytes = new byte[ivAndEncrypted.length - 16];
        System.arraycopy(ivAndEncrypted, 16, encryptedBytes, 0, encryptedBytes.length);

        SecretKeySpec secretKey = getKey(key);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] original = cipher.doFinal(encryptedBytes);
        return new String(original, "UTF-8");
    }

    // Utilitaires spécifiques à la CNI
    public static String encryptCNI(String cni) throws Exception {
        return encrypt(cni, MASTER_KEY);
    }

    public static String decryptCNI(String encryptedCni) throws Exception {
        return decrypt(encryptedCni, MASTER_KEY);
    }




}
