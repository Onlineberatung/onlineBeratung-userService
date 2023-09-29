package de.caritas.cob.userservice.api.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MD5Utils {

  public static String toMd5(String input) {
    return input == null ? null : toMd5NullSafe(input);
  }

  private static String toMd5NullSafe(String input) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      log.error("Error while creating MD5 instance", e);
      return null;
    }
    md.update(input.getBytes());
    byte[] digest = md.digest();
    return DatatypeConverter.printHexBinary(digest).toUpperCase();
  }
}
