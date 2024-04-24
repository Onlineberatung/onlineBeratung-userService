package de.caritas.cob.userservice.api.service.helper;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class EmailUrlDecoder {

  private static final String PLUS = "+";

  public static String decodeEmailQuery(String query) {
    List<String> strings = Splitter.on(PLUS).splitToList(query);
    List<String> decodedList =
        strings.stream()
            .map(part -> URLDecoder.decode(part, StandardCharsets.UTF_8).trim())
            .collect(Collectors.toList());
    return Joiner.on(PLUS).join(decodedList).trim();
  }
}
