package com.tydic.filehub.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加解密工具类
 * 用于解密 CONFIG_PULL_DATASOURCE 表中的用户名和密码
 */
public class AesDecryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * AES 解密
     * 密钥从配置读取，默认16位（AES-128）
     *
     * @param encrypted 加密后的 Base64 字符串
     * @param key       密钥（16/24/32位）
     * @return 解密后的明文
     */
    public static String decrypt(String encrypted, String key) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                keyBytes = padKey(keyBytes);
            }
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }

    public static String encrypt(String str, String key) throws Exception {
        if (str == null || key == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        byte[] bytes = cipher.doFinal(str.getBytes("utf-8"));
        String result = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
        return result;
    }

    public static void main(String[] args) throws Exception {
        String key = "1234567890123456";
        System.out.println(encrypt("example-user", key));
        System.out.println(encrypt("example-password", key));
    }

    private static byte[] padKey(byte[] key) {
        byte[] padded = new byte[16];
        System.arraycopy(key, 0, padded, 0, Math.min(key.length, 16));
        return padded;
    }
}
