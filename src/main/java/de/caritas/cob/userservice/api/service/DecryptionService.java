package de.caritas.cob.userservice.api.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.exception.NoMasterKeyException;

@Service
public class DecryptionService {

  private static final String CIPHER_METHODS = "AES/ECB/PKCS5PADDING";
  private static final String SECRET_KEY_SPEC_METHOD = "AES";
  private static final String MESSAGE_DIGEST_METHOD = "SHA-1";

  private static final String INITIAL_MASTER_KEY = "initialMasterKey";

  private static final String ENCRYPTED_MESSAGE_FLAG = "enc:";

  @Value("${service.encryption.appkey}")
  private String fragment_applicationKey;

  private String fragment_masterKey = INITIAL_MASTER_KEY;

  /**
   * Updates the master key fragment
   *
   * @param masterKey The master key fragment
   */
  public void updateMasterKey(String masterKey) {
    this.fragment_masterKey = masterKey;
  }

  /**
   * Returns the current master key
   *
   * @return the current master key
   */
  public String getMasterKey() {
    return this.fragment_masterKey;
  }

  /**
   * Returns the current application key
   *
   * @return the current application key
   */
  public String getApplicationKey() {
    return this.fragment_applicationKey;
  }

  /**
   * Prepares the SecretKeySpec for a given key
   *
   * @param secret the key for the generation
   * @return The SecretKeySpec based on the key or <null> in case of an error
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   */
  private SecretKeySpec generateSecretKeySpec(String secret)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {

    if (getMasterKey().equals(INITIAL_MASTER_KEY)) {
      throw new NoMasterKeyException("No MasterKey found - please provide a MasterKey!");
    }

    byte[] keyByte = (getMasterKey() + secret + getApplicationKey()).getBytes("UTF-8");
    MessageDigest sha = MessageDigest.getInstance(MESSAGE_DIGEST_METHOD);
    keyByte = sha.digest(keyByte);
    keyByte = Arrays.copyOf(keyByte, 16);
    return new SecretKeySpec(keyByte, SECRET_KEY_SPEC_METHOD);
  }

  /**
   * Decrypts a given message with the given secret
   *
   * @param messageToDecrypt The message to decrypt
   * @param secret           The secret to be used
   * @return The decrypted message
   */
  public String decrypt(String messageToDecrypt, String secret) throws CustomCryptoException {

    if (messageToDecrypt == null || !messageToDecrypt.startsWith(ENCRYPTED_MESSAGE_FLAG)) {
      return messageToDecrypt;
    }

    messageToDecrypt = messageToDecrypt.substring(ENCRYPTED_MESSAGE_FLAG.length());

    try {
      SecretKeySpec keySpec = generateSecretKeySpec(secret);
      Cipher cipher = Cipher.getInstance(CIPHER_METHODS);
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      return new String(cipher.doFinal(Base64.getDecoder().decode(messageToDecrypt)));
    } catch (BadPaddingException e) {
      LogService.logEncryptionPossibleBadKeyError(e);
      throw new CustomCryptoException(e);
    } catch (Exception e) {
      LogService.logEncryptionServiceError(e);
      throw new CustomCryptoException(e);
    }
  }

}
