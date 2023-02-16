package de.caritas.cob.userservice.api.service.helper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;

class EmailUrlDecoderTest {

  @Test
  public void decodeEmailQuery_Should_notChangeProperEmail() {
    assertThat(EmailUrlDecoder.decodeEmailQuery("testmail@gmail.com"))
        .isEqualTo("testmail@gmail.com");
    assertThat(EmailUrlDecoder.decodeEmailQuery("testmail%40virtual-identity.com"))
        .isEqualTo("testmail@virtual-identity.com");
    assertThat(EmailUrlDecoder.decodeEmailQuery("test.mail%40virtual-identity.com"))
        .isEqualTo("test.mail@virtual-identity.com");
  }

  @Test
  public void decodeEmailQuery_Should_decodeProperlyMailsWithPlus() {
    assertThat(EmailUrlDecoder.decodeEmailQuery("testmail%2Bext%40gmail.com"))
        .isEqualTo("testmail+ext@gmail.com");
    assertThat(EmailUrlDecoder.decodeEmailQuery("testmail%2Bext%40virtual-identity.com"))
        .isEqualTo("testmail+ext@virtual-identity.com");
    assertThat(
            EmailUrlDecoder.decodeEmailQuery(
                "test.user.ext%2Btenant14.02.v2%40virtual-identity.com"))
        .isEqualTo("test.user.ext+tenant14.02.v2@virtual-identity.com");
  }
}
