package de.caritas.cob.UserService.api.repository.session;

import java.util.Arrays;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Consulting types enum containing the consulting type id and the URL name
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum ConsultingType {
  SUCHT(0, "suchtberatung"), U25(1, "u25"), PREGNANCY(2, "schwangerschaftsberatung"), PARENTING(3,
      "eltern-familie"), CURE(4, "kurberatung"), DEBT(5, "schuldnerberatung"), SOCIAL(6,
          "allgemeine-soziale-beratung"), SENIORITY(7, "leben-im-alter"), DISABILITY(8,
              "behinderung-und-psychische-erkrankung"), PLANB(9, "mein-planb"), LAW(10,
                  "rechtliche-betreuung"), OFFENDER(11, "straffaelligkeit"), AIDS(12,
                      "hiv-aids-beratung"), REHABILITATION(13, "kinder-reha"), CHILDREN(14,
                          "kinder-jugendliche"), KREUZBUND(15, "kb-sucht-selbsthilfe"), MIGRATION(
                              16, "migration"), EMIGRATION(17, "rw-auswanderung"), HOSPICE(18,
                                  "hospiz-palliativ"), REGIONAL(19, "regionale-angebote");

  private final int value;
  private final String urlName;

  public static Optional<ConsultingType> valueOf(int value) {
    return Arrays.stream(values()).filter(legNo -> legNo.value == value).findFirst();
  }
}
