package org.openmrs.module.cdrsync.utils;

import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class AppUtils {
	
	private static SecretKeySpec secretKey;
	
	private final static String SECRET = Context.getRuntimeProperties().getProperty("secret");
	
	private static void setKey() {
		MessageDigest sha;
		try {
			byte[] key = SECRET.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static String encrypt(String strToEncrypt) {
        try
        {
            setKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException |
               IllegalBlockSizeException | NoSuchPaddingException e)
        {
            System.out.println("Error while encrypting: " + e.getMessage());
        }
        return null;
    }
	
	public static String decrypt(String strToDecrypt) {
        try {
            setKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException |
               IllegalBlockSizeException | NoSuchPaddingException e)
        {
            System.out.println("Error while decrypting: " + e.getMessage());
        }
        return null;
    }
}
