package project.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.Map;

public class secure {
	public static String encryptAES(String key, String targetValue) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		String IV = key.substring(0,16); // 16byte = 128bit
		byte[] keyBytes = key.getBytes();
		SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV.getBytes()));

		byte[] targetedBytes = targetValue.getBytes("UTF-8");
		byte[] encryptedBytes = cipher.doFinal(targetedBytes);
		String encryptedValue = new String(Base64.encodeBase64(encryptedBytes, false, true), "UTF-8");
		return encryptedValue;
	}

	public static String decryptAES(String key, String securedValue) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		String IV = key.substring(0,16); // 16byte = 128bit
		byte[] keyBytes = key.getBytes();
		SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV.getBytes("UTF-8")));

		byte[] encryptedBytes = Base64.decodeBase64(securedValue.getBytes());
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		String decryptedValue = new String(decryptedBytes,"UTF-8");
		return decryptedValue;
	}

	public static String encryptRSA(PublicKey publicKey, String targetValue) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		byte[] targetedBytes = targetValue.getBytes();
	    byte[] encryptedBytes = cipher.doFinal(targetedBytes);
	    String encryptedValue = new String(encryptedBytes, "UTF-8");
	    return encryptedValue;
	}

	public static String decryptRSA(PrivateKey privateKey, String securedValue) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = hexToByteArray(securedValue);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decryptedValue = new String(decryptedBytes, "UTF-8");
        return decryptedValue;
    }

    /**
     * hex string을 byte 배열로 변환
     */
    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return new byte[]{};
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            byte value = (byte)Integer.parseInt(hex.substring(i, i + 2), 16);
            bytes[(int) Math.floor(i / 2)] = value;
        }
        return bytes;
    }

    /**
     * hex to public key
     */
    public static PublicKey hexStringToPublicKey (String publicKeyModulus, String publicKeyExponent) {
    	KeyFactory keyFactory = null;
    	PublicKey publicKey = null;
		try {
	    	BigInteger modulus = new BigInteger(publicKeyModulus, 16);
	    	BigInteger exponent = new BigInteger(publicKeyExponent, 16);
	    	RSAPublicKeySpec pubks = new RSAPublicKeySpec(modulus, exponent);

			keyFactory = KeyFactory.getInstance("RSA");
	    	publicKey = keyFactory.generatePublic(pubks);
		} catch (Exception e) {
			System.out.println("hexStringToPublicKey.e="+ e.toString());
		}

    	return publicKey;
    }

    /**
     * hex to private key
     */
    public static PrivateKey hexStringToPrivateKey (String privateKeyModulus, String privateKeyExponent) {
    	KeyFactory keyFactory = null;
    	PrivateKey privateKey = null;
		try {
	    	BigInteger modulus = new BigInteger(privateKeyModulus, 16);
	    	BigInteger exponent = new BigInteger(privateKeyExponent, 16);
	    	RSAPrivateKeySpec priks = new RSAPrivateKeySpec(modulus, exponent);

			keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(priks);
		} catch (Exception e) {
			System.out.println("hexStringToPrivateKey.e="+e.toString());
		}

    	return privateKey;
    }

    /**
     * generate rsa key
     */
	public static Map<String, String> generateRSAKey(int keysize) {
		Map<String, String> map = new HashMap<String, String>();

		try{
		    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		    generator.initialize(keysize);

		    KeyPair keyPair = generator.genKeyPair();
		    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		    PublicKey publicKey = keyPair.getPublic();
		    PrivateKey privateKey = keyPair.getPrivate();
		    RSAPublicKeySpec publicSpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
		    RSAPrivateKeySpec privateSpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);

		    // 문자열로 변환
		    String publicKeyModulus   = util.trim(publicSpec.getModulus().toString(16));
		    String publicKeyExponent  = util.trim(publicSpec.getPublicExponent().toString(16));
		    String privateKeyModulus  = util.trim(privateSpec.getModulus().toString(16));
		    String privateKeyExponent = util.trim(privateSpec.getPrivateExponent().toString(16));

		    map.put("privateKeyModulus" , privateKeyModulus );
		    map.put("privateKeyExponent", privateKeyExponent);
		    map.put("publicKeyModulus"  , publicKeyModulus  );
		    map.put("publicKeyExponent" , publicKeyExponent );

		} catch(Exception e) {
			System.out.println("getRSAKey.e="+ e.toString());
			return null;
		}

	    return map;
	}
}
