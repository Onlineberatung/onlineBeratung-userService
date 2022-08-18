package de.caritas.cob.userservice.api.helper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.ws.rs.InternalServerErrorException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Component;

/** Helper class */
@Component
public class Helper {

  public static final long ONE_DAY_IN_HOURS = 24L;
  public static final Date UNIXTIME_0 = new Date(0);

  /**
   * Convert a date to a unix timestamp
   *
   * @param date
   * @return the unix timestamp for the given date
   */
  public static Long getUnixTimestampFromDate(Date date) {
    return (date != null) ? date.getTime() / 1000 : null;
  }

  /**
   * Remove HTML code from a text (XSS-Protection)
   *
   * @param text
   * @return the given text without html
   */
  public static String removeHTMLFromText(String text) {

    OutputSettings outputSettings = new OutputSettings();
    outputSettings.prettyPrint(false);

    try {

      text = Jsoup.clean(text, StringUtils.EMPTY, Whitelist.none(), outputSettings);
    } catch (Exception exception) {
      throw new InternalServerErrorException("Error while removing HTML from text", exception);
    }
    return text;
  }

  /**
   * Url decoding for a given string
   *
   * @param stringToDecode
   * @return the decoded string or null on error
   */
  public String urlDecodeString(String stringToDecode) {
    try {
      return java.net.URLDecoder.decode(stringToDecode, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException ex) {
      return null;
    }
  }
}
