package de.caritas.cob.userservice.api.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class StringConverter {

  private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

  public String hashOf(String chatUserId) {
    return DigestUtils.sha256Hex(chatUserId);
  }

  public String encrypt(final String s, final String secret) {
    try {
      var cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, key(secret));
      var encryptedBytes = cipher.doFinal(s.getBytes(StandardCharsets.UTF_8));

      return Base64.getEncoder().encodeToString(encryptedBytes);
    } catch (GeneralSecurityException e) {

      return null;
    }
  }

  public String decrypt(final String s, final String secret) {
    try {
      var cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, key(secret));
      var decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(s));

      return new String(decryptedBytes);
    } catch (GeneralSecurityException e) {

      return null;
    }
  }

  private SecretKeySpec key(final String secret) throws NoSuchAlgorithmException {
    var key = secret.getBytes(StandardCharsets.UTF_8);
    var sha = MessageDigest.getInstance("SHA-1");
    key = sha.digest(key);
    key = Arrays.copyOf(key, 16);

    return new SecretKeySpec(key, "AES");
  }
}
