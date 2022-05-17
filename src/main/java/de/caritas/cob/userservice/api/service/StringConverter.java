package de.caritas.cob.userservice.api.service;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
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
import java.text.ParseException;
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

  public byte[] rsaBcEncrypt(final String s, final String mod) {
    try {
      var cypher = Cipher.getInstance(RSA_TRANSFORMATION, "BC");
      var oaepSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
          PSource.PSpecified.DEFAULT);

      var exponentBytes = Base64.getDecoder().decode("AQAB");
      var exponent = new BigInteger(1, exponentBytes);

      var modulusBytes = Base64.getUrlDecoder().decode(mod); // or base64URLDecode(secret);
      var modulus = new BigInteger(1, modulusBytes);

      var rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent, oaepSpec);
      var publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
      cypher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);
      var bytes = s.getBytes(StandardCharsets.UTF_8);

      return cypher.doFinal(bytes);
    } catch (GeneralSecurityException exception) {
      log.error("Could not RSA-encrypt string '{}': {}", s, exception);
      return null;
    }
  }

  public byte[] rsaNimbusEncrypt(final String s, final String publicKey) {
    try {
      var rsaKey = RSAKey.parse(publicKey);
      var encrypter = new RSAEncrypter(rsaKey);
      var header = new JWEHeader(
          JWEAlgorithm.RSA_OAEP_256,
          EncryptionMethod.A256CBC_HS512
      );

      var payload = new Payload(s);
      var jwe = new JWEObject(header, payload);
      jwe.encrypt(encrypter);

      var t = jwe.getCipherText().toString();

      return jwe.getCipherText().decode();
    } catch (ParseException | JOSEException e) {
      throw new RuntimeException(e);
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
      sb.append("'").append(i).append("':").append(intArray[i]);
      if (i != intArray.length - 1) {
        sb.append(",");
      }
    }

    return sb.append("}").toString();
  }

  public String encodeBase64Ascii(String s) { // also known as btoa in JavaScript
    var asciiBytes = s.getBytes(StandardCharsets.US_ASCII);

    return Base64.getEncoder().encodeToString(asciiBytes);
  }

  private static byte[] base64URLDecode(String base64URLEncodedString) {
    int size = base64URLEncodedString.length();
    String tempResult = base64URLEncodedString;
    tempResult = tempResult.replace("-", "+");
    tempResult = tempResult.replace("_", "/");

    int padding = size % 4;
    switch (padding) {
      case 0:
        break;
      case 2:
        tempResult = tempResult.concat("==");
        break;
      case 3:
        tempResult = tempResult.concat("=");
        break;

      default:
        throw new IllegalArgumentException("Invalid base64urlencoded string");
    }

    return tempResult.getBytes();
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

  private byte[][] generateKeyAndIV(byte[] salt, byte[] password)
      throws NoSuchAlgorithmException {
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
