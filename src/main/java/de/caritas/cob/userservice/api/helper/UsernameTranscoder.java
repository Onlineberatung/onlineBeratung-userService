package de.caritas.cob.userservice.api.helper;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/** Transcoder class to encode and decode username with base32. */
@Component
public class UsernameTranscoder {

  private static final String ENCODING_PREFIX = "enc.";
  private static final String BASE32_PLACEHOLDER = "=";
  private static final String BASE32_PLACEHOLDER_USERNAME_REPLACE_STRING = ".";

  private final Base32 base32 = new Base32();

  /**
   * Encodes the given username if it isn't already encoded.
   *
   * @param username the username to encode
   * @return encoded username
   */
  public String encodeUsername(String username) {
    return username.startsWith(ENCODING_PREFIX) ? username : base32EncodeUsername(username);
  }

  private String base32EncodeUsername(String username) {
    return ENCODING_PREFIX
        + base32EncodeAndReplacePlaceholder(username, BASE32_PLACEHOLDER_USERNAME_REPLACE_STRING);
  }

  public String base32EncodeAndReplacePlaceholder(String value, String replaceString) {
    return base32.encodeAsString(value.getBytes()).replace(BASE32_PLACEHOLDER, replaceString);
  }

  /**
   * Decodes the given username if it isn't already decoded.
   *
   * @param username the username to decode
   * @return the decoded username
   */
  public String decodeUsername(String username) {
    return username.startsWith(ENCODING_PREFIX) ? base32DecodeUsername(username) : username;
  }

  private String base32DecodeUsername(String username) {
    return new String(
        base32.decode(
            username
                .replace(ENCODING_PREFIX, StringUtils.EMPTY)
                .toUpperCase()
                .replace(BASE32_PLACEHOLDER_USERNAME_REPLACE_STRING, BASE32_PLACEHOLDER)));
  }

  public String transformedOf(String s) {
    return s.startsWith(ENCODING_PREFIX) ? base32DecodeUsername(s) : base32EncodeUsername(s);
  }
}
