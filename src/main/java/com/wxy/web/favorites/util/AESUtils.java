package com.wxy.web.favorites.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author wangxiaoyuan
 * 2021/4/16 14:57
 **/
public class AESUtils {
    // 加密
    public static String encrypt(String message, String secretKey) throws Exception {
        if (secretKey == null) {
            throw new IllegalArgumentException("Key为空null");
        }
        // 判断Key是否为16位
        if (secretKey.length() != 16) {
            throw new IllegalArgumentException("Key长度不是16位");
        }
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    public static String decrypt(String message, String secretKey) throws Exception {
        // 判断Key是否正确
        if (secretKey == null) {
            throw new IllegalArgumentException("Key为空null");
        }
        // 判断Key是否为16位
        if (secretKey.length() != 16) {
            throw new IllegalArgumentException("Key长度不是16位");
        }
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] encrypted1 = new BASE64Decoder().decodeBuffer(message);//先用base64解密
        byte[] original = cipher.doFinal(encrypted1);
        return new String(original, StandardCharsets.UTF_8);
    }
}
