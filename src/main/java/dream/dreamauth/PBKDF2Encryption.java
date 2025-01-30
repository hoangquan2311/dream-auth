package dream.dreamauth;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

import static dream.dreamauth.DreamAuth.*;

public class PBKDF2Encryption {
    private static final String password = "con cac";
    private static final int iterations = 9999;
    private static final int keyLength = 256;

    //pbkdf2 + sha256 + aes + base64
    public static String ultraEncrypt(String plaintext) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeySpec secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        byte[] combined = new byte[salt.length + iv.length + ciphertext.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(ciphertext, 0, combined, salt.length + iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(combined);
    }
    public static String ultraDecrypt(String encrypted) throws Exception{
        byte[] combined = Base64.getDecoder().decode(encrypted);
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[combined.length - salt.length - iv.length];
        System.arraycopy(combined, 0, salt, 0, salt.length);
        System.arraycopy(combined, salt.length, iv, 0, iv.length);
        System.arraycopy(combined, salt.length + iv.length, ciphertext, 0, ciphertext.length);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeySpec secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return new String(cipher.doFinal(ciphertext));
    }
    public static String dreamdistortion(String plaintext, int i, int j){
        if(i==j) return plaintext;
        plaintext = Base64.getEncoder().encodeToString(plaintext.getBytes());
        return dreamdistortion(plaintext,++i,j);
    }
    public static String dreamstabilization(String encrypted, int i, int j){
        if(i==j) return encrypted;
        encrypted = new String(Base64.getDecoder().decode(encrypted));
        return dreamstabilization(encrypted,++i,j);
    }
    public static int dreamIteration() throws Exception {
        return Integer.parseInt(dreamDecrypt(DREAM_ITERATION,0,3));
    }
    public static String g_p() throws Exception {
        return dreamDecrypt(p_ay91,0,Integer.parseInt(dreamDecrypt(DREAM_ITERATION,0,3)));
    }
    public static String g_i() throws Exception {
        return ultraDecrypt(awcs_1lk);
    }
    public static String dreamEncrypt(String plaintext, int i, int j) throws Exception {
        if(i==j) return plaintext;
        if(i%2==0){
            plaintext = ultraEncrypt(plaintext);
        }
        else plaintext = dreamdistortion(plaintext,0,1);
        return dreamEncrypt(plaintext,++i,j);
    }
    public static String dreamDecrypt(String encrypted, int i, int j) throws Exception{
        if(i==j) return encrypted;
        if(j%2!=0){
            if(i%2==0) encrypted = ultraDecrypt(encrypted);
            else encrypted = dreamstabilization(encrypted,0,1);
        }
        else {
            if(i%2==0) encrypted = dreamstabilization(encrypted,0,1);
            else encrypted = ultraDecrypt(encrypted);
        }
        return dreamDecrypt(encrypted,++i,j);
    }
}
