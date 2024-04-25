package de.caritas.cob.userservice.api.service.helper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmailUrlDecoderTest {

  @ParameterizedTest
  @CsvSource({
    "testmail%2Bext%40gmail.com,testmail+ext@gmail.com",
    "testmail%2Bext%40virtual-identity.com,testmail+ext@virtual-identity.com",
    "test.user.ext%2Btenant14.02.v2%40virtual-identity.com,test.user.ext+tenant14.02.v2@virtual-identity.com",
    "testmail@gmail.com,testmail@gmail.com",
    "testmail%40virtual-identity.com,testmail@virtual-identity.com",
    "test.mail%40virtual-identity.com,test.mail@virtual-identity.com",
  })
  void decodeEmailQuery_Should_decodeProperlyMailsWithPlus(String input, String expectedOutput) {
    assertThat(EmailUrlDecoder.decodeEmailQuery(input)).isEqualTo(expectedOutput);
  }
}
