package com.example.photosharing;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Zander on 2015/6/8.
 */
public class Encryption {
    static String IV = "0123456789abcdef";
    public static String encrypt(String plainText, String encryptionKey) throws Exception {
        encryptionKey = encryptionKeyPadding(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes()));
        String ret = new String(cipher.doFinal(plainText.getBytes()));
        return ret;
    }

    public static byte[] encryptByte(byte [] plainText, String encryptionKey) throws Exception {
        encryptionKey = encryptionKeyPadding(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes()));
        //String ret = new String();
        return cipher.doFinal(plainText);
    }

    public static String decrypt(String cipherText, String encryptionKey) throws Exception{
        encryptionKey = encryptionKeyPadding(encryptionKey);
        byte[] cipherTextByte = cipherText.getBytes();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes()));
        return new String(cipher.doFinal(cipherTextByte));
    }

    public static byte [] decryptByte(byte [] cipherText, String encryptionKey) throws Exception{
        encryptionKey = encryptionKeyPadding(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes()));
        return cipher.doFinal(cipherText);
    }

    private static String encryptionKeyPadding(String encryptionKey){
        String ret = encryptionKey;
        for (int i = encryptionKey.length() ;i < 16; i ++){
            ret = ret + 'A';
        }
        return ret;
    }
}
