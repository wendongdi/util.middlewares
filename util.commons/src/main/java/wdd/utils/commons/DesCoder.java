package wdd.utils.commons;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;

public class DesCoder {
    public static final String UTF8_CHARSET = "UTF-8";
    public static final String ALGORITHM = "DES";
    public static final String ALGORITHM_PATTERN = "DES/ECB/PKCS5Padding";
    static final String DES_KEY = "8bqtDYX4pNA=";
    static final String TBE_UID = "zzyapiv1";

    private static Key toKey(byte[] key)
            throws Exception {
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(dks);
        return secretKey;
    }

    public static String encrypt(String data, String key)
            throws Exception {
        Key k = toKey(Base64.decodeBase64(key.getBytes("UTF-8")));
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(1, k);
        return new String(Base64.encodeBase64(cipher.doFinal(data.getBytes("UTF-8"))), "UTF-8");
    }

    public static String decrypt(String data, String key)
            throws Exception {
        Key k = toKey(Base64.decodeBase64(key.getBytes("UTF-8")));
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(2, k);
        return new String(cipher.doFinal(Base64.decodeBase64(data.getBytes("UTF-8"))), "UTF-8");
    }

    public static String initKey()
            throws Exception {
        return initKey(null);
    }

    public static String initKey(String seed)
            throws Exception {
        SecureRandom secureRandom = null;
        if (seed != null)
            secureRandom = new SecureRandom(seed.getBytes("UTF-8"));
        else {
            secureRandom = new SecureRandom();
        }

        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(secureRandom);

        SecretKey secretKey = kg.generateKey();
        return new String(Base64.encodeBase64(secretKey.getEncoded()), "UTF-8");
    }

    public static String showToken(String s) {
        try {
            return encrypt(s + new Date().getTime(), "8bqtDYX4pNA=");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}