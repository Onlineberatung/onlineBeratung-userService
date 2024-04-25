package de.caritas.cob.userservice.api.adapters.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SearchQueryDecoderTest {

  @ParameterizedTest
  @CsvSource({
    "firstname.lastname+consultant@virtual-identity.com, firstname.lastname+consultant@virtual-identity.com",
    "*, *",
    "firstname.lastname@virtual-identity.com, firstname.lastname@virtual-identity.com",
    "A Test, A Test",
    ",''",
    "\"\",\"\"",
    "stringWithoutPlusSign,stringWithoutPlusSign",
    "firstname.lastname%2Bconsultant%40virtual-identity.com, firstname.lastname+consultant@virtual-identity.com"
  })
  void decode_Should_Decode_String_Not_ChangingPlusIntoSpace(String input, String expectedOutput) {
    assertThat(SearchQueryDecoder.decode(input).trim()).isEqualTo(expectedOutput);
  }
}
