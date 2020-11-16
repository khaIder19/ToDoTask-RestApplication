package com.todotask.rest.auth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;
import org.apache.log4j.Logger;

import com.todotask.data.TaskRegistry;

public class SecurityUtils {

	private static Logger log = Logger.getLogger(SecurityUtils.class);
	
	private static Properties p;
	
	public static final String ENV_SECURITY_UTIL_PROPS_PATH = "env.var.prop.security";	
	public static final String KEYSTORE_PASS_ALIAS = "env.var.keystore.pass.alias";
	public static final String KEYSTORE_PATH = "keystore.path";
	public static final String KEYSTORE_PUK = "keystore.keypair.apikey.public.alias";
	public static final String KEYSTORE_PRK = "keystore.keypair.apikey.private.alias";
	public static final String ITER_PARAM= "hash.iter";
	public static final String LENGHT_PARAM = "hash.lenght";
	public static final String HASH_ALGH_PARAM = "hash.algh";
	
	static {
		p = new Properties();
		try {
			p.load(new FileReader(System.getenv(ENV_SECURITY_UTIL_PROPS_PATH)));
		} catch (Exception e) {
			log.error("Securiy props load failed",e);
		}
	}
	
	
	//PBKDF2WithHmacSHA256
	private final int ITER;
	private final int LENGHT;
	private final String HASH_ALGH;
	
	private static SecurityUtils sInstance;
	private RandomStringGenerator generator;
	private KeyStore ks;
	private Cipher cipher ;
	private SecretKeyFactory factory;
	
	private SecurityUtils() {
			ITER = new Integer(p.getProperty(ITER_PARAM));
			LENGHT = new Integer(p.getProperty(LENGHT_PARAM));
			HASH_ALGH = p.getProperty(HASH_ALGH_PARAM);
			
		try {
			factory = SecretKeyFactory.getInstance(HASH_ALGH);
			Builder builder = new RandomStringGenerator.Builder();
			generator = builder.build();
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			cipher = Cipher.getInstance("RSA");
			char[] pass = System.getenv().get(KEYSTORE_PASS_ALIAS).toCharArray();
			ks.load(new FileInputStream(p.getProperty(KEYSTORE_PATH)),pass);
		} catch (Exception e) {
			log.error("Securiy init load failed",e);
		}
	}
	
	public synchronized static SecurityUtils getInstance() {
		if(sInstance == null) {
			sInstance = new SecurityUtils();
		}
		return sInstance;
	}
	
	public PassSalt hashPassword(char[] pass) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		byte [] encodedSecret = Base64.getEncoder().encode(genereteKey(pass, salt));
		String stringPass = new String(encodedSecret);
		String stringSalt = new String(Base64.getEncoder().encode(salt));
		return new PassSalt(stringPass,stringSalt);
	}
	
	public boolean validate(char[] inputPass,String salt,String hashedPass) {
		byte[] saltDecoded = Base64.getDecoder().decode(salt);
		byte[] toValidateKey = genereteKey(inputPass, saltDecoded);
		String encodedKey = new String(Base64.getEncoder().encode(toValidateKey));
		if(encodedKey.equals(hashedPass)) {
			return true;
		}
		return false;
	}
	
	private byte[] genereteKey(char[] pass,byte[] salt) {
		KeySpec keySpec = new PBEKeySpec(pass,salt,ITER,LENGHT);
		SecretKey key = null;
		try {
			key = factory.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			
		}
		return key.getEncoded();
	}
	
	public String randomPassword(int x,int y) {
		return generator.generate(x, y);
	}
	
	public String encryptString(char[] string) {
		String alias = p.getProperty(KEYSTORE_PUK);
		String keyAsString = null;
		try {
			Certificate pk = ks.getCertificate(alias);
			cipher.init(Cipher.ENCRYPT_MODE, pk.getPublicKey());
			byte[] encrypted = cipher.doFinal(new String(string).getBytes(StandardCharsets.UTF_8));
			byte[] encrypted64 =  Base64.getEncoder().encode(encrypted);
			keyAsString = new String(encrypted64,StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Encryption failed",e);
		}
		return keyAsString;
	}
	
	public String decrypt(String encoded64string) {
		char[] pass = System.getenv().get(KEYSTORE_PASS_ALIAS).toCharArray();
		String alias = p.getProperty(KEYSTORE_PRK);
		String keyAsString = null;
		try {
			Key prk = ks.getKey(alias, pass);
			cipher.init(Cipher.DECRYPT_MODE, prk);
			byte[] decrypted64 =  Base64.getDecoder().decode(encoded64string.getBytes(StandardCharsets.UTF_8));
			byte[] decryptedString = cipher.doFinal(decrypted64);
			keyAsString = new String(Base64.getEncoder().encode(decryptedString),StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Decryption failed",e);
		}
		return keyAsString;
	}
	
	public static class PassSalt{
		
		private final String pass;
		private final String salt;
		
		public PassSalt(String pass,String salt) {
			this.pass = pass;
			this.salt = salt;
		}
		
		public String getPass() {
			return pass;
		}
		
		public String getSalt() {
			return salt;
		}
	}
	
}
