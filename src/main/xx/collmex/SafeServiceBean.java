/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.modules.collmex;


import org.apache.commons.codec.binary.Base64;
import org.xhtmlrenderer.util.Configuration;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

@Register(classes = {SafeService.class })
public class SafeServiceBean implements SafeService {

	private static final String ALGORITHM = "AES";
	private static final String UTF8 = "UTF8";
	private static final String UTF_8 = "UTF-8";
	private static final int MAXSALTLENGTH = 32;
	private static final int PREKEY = 1;

	private HashMap<String, String> keyForGroup = new HashMap<String, String>();

	public static byte[] fromBase64(String base64) throws IOException {
		return Base64.decodeBase64(base64.getBytes(UTF_8));
	}

	/**
	 * Converts a given byte-array to a BASE64 representation.
	 */
	public static String toBase64(byte[] array) {
		return Base64.encodeBase64String(array).trim();
	}


	@Override
	public String decodeByKey(String keyword, String encodedText) {
		if (Strings.isEmpty(encodedText) || Strings.isEmpty(keyword)) {
			return null;
		}
		try {

			// decode the encoded Base64-text.
			byte[] decodedValue = fromBase64(encodedText);
			// get the saltLength
			int saltLength = decodedValue[0];
			// get the text to decode
			byte[] toDecode = new byte[decodedValue.length - saltLength
					- PREKEY];
			for (int i = 0; i < decodedValue.length - saltLength - PREKEY; i++) {
				toDecode[i] = decodedValue[i + saltLength + PREKEY];
			}
			// get the salt
			byte[] saltAsBytes = new byte[saltLength];
			for (int i = 0; i < saltLength; i++) {
				saltAsBytes[i] = decodedValue[i + PREKEY];
			}
			String salt = toBase64(saltAsBytes);
			// build the secret key
			SecretKeySpec secretKeySpec = buildSecretKeySpec(ALGORITHM, salt
					+ keyword);
			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			// decrypt the text
			byte[] decryptedVal = cipher.doFinal(toDecode);
			// transform to a string
			return new String(decryptedVal);
		} catch (Exception e) {
	//		ApplicationController.handle(e);
			Exceptions.handle(e);
		}
		return null;
	}

	@Override
	public String encodeByKey(String keyword, String text) {

		int minKeyLength = Configuration.valueAsInt("MinSafeKey", 2);
		if (keyword.length() < minKeyLength) {
//			throw new BusinessException(
//					Formatter
//							.create("Der Schlüssel ist zu kurz, notwendig sind ${minLength} Zeichen.")
//							.set("minLength", minKeyLength).format());
			throw Exceptions.createHandled().handle();
		}
		try {

			// get a random pattern as "salt"
			SecureRandom random = new SecureRandom();
			int saltLength = random.nextInt(MAXSALTLENGTH);
			if (saltLength < 10) {
				saltLength = saltLength + 10;
			}
			byte seed[] = random.generateSeed(saltLength);
			// transform the text in a byte-field with the salt like
			// <saltLength (one Byte)><salt><encryptedText>
			byte[] textAsByte = text.getBytes(UTF8);
			// build the secret key
			String seedAsString = toBase64(seed);
			SecretKeySpec secretKeySpec = buildSecretKeySpec(ALGORITHM,
					seedAsString + keyword);
			// instatiate cipher
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			// encrypt the byte-field
			byte[] encrypted = cipher.doFinal(textAsByte);
			// encode the encrypted to Base64
			byte[] toEnCode = new byte[encrypted.length + saltLength + PREKEY];
			toEnCode[0] = (byte) saltLength;
			for (int i = 0; i < seed.length; i++) {
				toEnCode[i + PREKEY] = seed[i];
			}
			for (int i = 0; i < encrypted.length; i++) {
				toEnCode[i + seed.length + PREKEY] = encrypted[i];
			}
			String encoded = toBase64(toEnCode);
			return encoded;
		} catch (Exception e) {
//			ApplicationController.handle(e);
			Exceptions.handle(e);
		}
		return null;
	}

	/**
	 * builds a secret key with the given keyword and the given algorithm
	 */
	private SecretKeySpec buildSecretKeySpec(String algorithm, String keyword)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {

		byte[] key = (keyword).getBytes(UTF8);
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16); // use only first 16 * 8 = 128 bit
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
		return secretKeySpec;
	}

	@Override
	public void writeKeyword(String keyword, String group) {
		if (Strings.isEmpty(group)) {
			return;
		}
		this.keyForGroup.put(group, keyword);

	}

	@Override
	public String readKeyword(String group) {
		if (keyForGroup.isEmpty()) {
			return null;
		}
		if (Strings.isEmpty(group)) {
			return null;
		}
		if (!keyForGroup.containsKey(group)) {
			return null;
		}

		return keyForGroup.get(group);
	}

	@Override
	public void forgetKeyForGroup(String group) {
		if (keyForGroup.isEmpty()) {
			return;
		}
		if (Strings.isEmpty(group)) {
			return;
		}
		if (keyForGroup.containsKey(group)) {
			keyForGroup.remove(group);
		}

	}

}
