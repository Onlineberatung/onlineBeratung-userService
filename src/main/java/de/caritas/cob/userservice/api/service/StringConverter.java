package de.caritas.cob.userservice.api.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StringConverter {

  private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
  private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

  public StringConverter() {
    Security.addProvider(new BouncyCastleProvider());
  }

  public String hashOf(String chatUserId) {
    return DigestUtils.sha256Hex(chatUserId);
  }

  public byte[] rsaEncrypt(final String s, final String mod) {
    try {
      var cypher = Cipher.getInstance(RSA_TRANSFORMATION);
      var oaepSpec =
          new OAEPParameterSpec(
              "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

      var exponentBytes = Base64.getDecoder().decode("AQAB");
      var exponent = new BigInteger(1, exponentBytes);

      var modulusBytes = Base64.getUrlDecoder().decode(mod);
      var modulus = new BigInteger(1, modulusBytes);

      var rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent, oaepSpec);
      var publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
      cypher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);
      var bytes = s.getBytes(StandardCharsets.UTF_8);

      return cypher.doFinal(bytes);
    } catch (GeneralSecurityException exception) {
      log.error("Could not RSA-encrypt string", exception);
      return new byte[0];
    }
  }

  public int[] int8Array(byte[] bytes) {
    var intArray = new int[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      intArray[i] = bytes[i] & 0xff;
    }

    return intArray;
  }

  public String jsonStringify(int[] intArray) {
    var sb = new StringBuilder("{");
    for (int i = 0; i < intArray.length; i++) {
      sb.append("\"").append(i).append("\":").append(intArray[i]);
      if (i != intArray.length - 1) {
        sb.append(",");
      }
    }

    return sb.append("}").toString();
  }

  public String base64AsciiEncode(String s) { // also known as btoa in JavaScript
    var asciiBytes = s.getBytes(StandardCharsets.US_ASCII);

    return Base64.getEncoder().encodeToString(asciiBytes);
  }

  public String aesDecrypt(String s, String secret) {
    try {
      var cipherData = Base64.getDecoder().decode(s);
      var saltData = Arrays.copyOfRange(cipherData, 8, 16);
      var secretBytes = secret.getBytes(StandardCharsets.UTF_8);
      var keyAndIV = generateKeyAndIV(saltData, secretBytes);
      var key = new SecretKeySpec(keyAndIV[0], "AES");
      var iv = new IvParameterSpec(keyAndIV[1]);
      var encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
      var aesCBC = Cipher.getInstance(AES_TRANSFORMATION);
      aesCBC.init(Cipher.DECRYPT_MODE, key, iv);

      var decryptedData = aesCBC.doFinal(encrypted);

      return new String(decryptedData, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("java:S4790") // Using weak hashing algorithms is security-sensitive
  private byte[][] generateKeyAndIV(byte[] salt, byte[] password) throws NoSuchAlgorithmException {
    var md = MessageDigest.getInstance("MD5");
    var digestLength = md.getDigestLength();
    var requiredLength = (48 + digestLength - 1) / digestLength * digestLength;
    var generatedData = new byte[requiredLength];
    var generatedLength = 0;

    try {
      md.reset();
      while (generatedLength < 48) {
        if (generatedLength > 0) {
          md.update(generatedData, generatedLength - digestLength, digestLength);
        }
        md.update(password);
        md.update(salt, 0, 8);
        md.digest(generatedData, generatedLength, digestLength);

        generatedLength += digestLength;
      }

      var result = new byte[2][];
      result[0] = Arrays.copyOfRange(generatedData, 0, 32);
      result[1] = Arrays.copyOfRange(generatedData, 32, 48);

      return result;
    } catch (DigestException e) {
      throw new RuntimeException(e);
    } finally {
      Arrays.fill(generatedData, (byte) 0);
    }
  }
}
