package de.caritas.cob.userservice.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StringConverterTest {

  @InjectMocks
  private StringConverter underTest;

  @Test
  void generateMasterKeyShouldFollowFrontendImplementation() {
    var stringToDigest = "MeineUserId";
    var expectedString = "26509bc23cc27fa0c95c9ed6966a29725b4bf26dbf1d538be38268d411f5677d";

    assertEquals(expectedString, underTest.hashOf(stringToDigest));
  }

  @Test
  void encrypt2AndDecrypt2ShouldBeInverseFunctions() {
    var chatUserId = RandomStringUtils.randomAlphanumeric(17);
    var masterKey = underTest.hashOf(chatUserId);
    var clearText = RandomStringUtils.randomAlphanumeric(32);
    var encryptedText = underTest.encrypt(clearText, masterKey);
    var decryptedText = underTest.decrypt(encryptedText, masterKey);

    assertEquals(clearText, decryptedText);
    assertNotEquals(clearText, encryptedText);
  }
}
