package de.caritas.cob.userservice.api.adapters.web.controller;

import static com.google.common.collect.Lists.newArrayList;

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import com.google.common.base.Splitter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchQueryDecoder {

  private static final String SEPARATOR = "+";

  /* Allows the query string to contain plus sign, but not change it to space.
  See https://bugs.openjdk.org/browse/JDK-8179507 */
  public static String decode(String query) {
    if (StringUtils.isBlank(query)) {
      return StringUtils.EMPTY;
    }
    var parts = Splitter.on(SEPARATOR).split(query);
    return newArrayList(parts).stream()
        .map(SearchQueryDecoder::decodePart)
        .collect(Collectors.joining(SEPARATOR)).trim();
  }

  private static String decodePart(String s) {
    return URLDecoder.decode(s, StandardCharsets.UTF_8);
  }
}
