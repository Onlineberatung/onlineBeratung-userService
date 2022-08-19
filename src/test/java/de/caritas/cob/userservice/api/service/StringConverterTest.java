package de.caritas.cob.userservice.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringConverterTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks private StringConverter underTest;

  @Test
  void generateMasterKeyShouldFollowFrontendImplementation() {
    var stringToDigest = "MeineUserId";
    var expectedString = "26509bc23cc27fa0c95c9ed6966a29725b4bf26dbf1d538be38268d411f5677d";

    assertEquals(expectedString, underTest.hashOf(stringToDigest));
  }

  @Test
  void aesDecryptShouldFollowFrontendImplementationShort() {
    var secret = "fnwebFEBK3BFE";
    var encryptedText =
        "U2FsdGVkX18TwcLautoyvh0UfVXqu1nh1KF2VtWbP6XsWxjkjS22oHXIIpJnQgqtsLLuJ7dwlhei8ICjRK3TJw==";
    var decryptedText = underTest.aesDecrypt(encryptedText, secret);

    assertEquals("The quick brown fox jumps over the lazy dog.", decryptedText);
  }

  @Test
  void aesDecryptShouldFollowFrontendImplementationLong() {
    var secret = "the quick brown fox jumps over the lazy dog";
    var encryptedText =
        "U2FsdGVkX1/p2D62+iqhS6Irmgg+zIcKSV2dXqmT25dwPNOv8UpSmA3UepiFl131FwSktDHds"
            + "6YqqKEK8+MRXc2E4NPblSxLg8uphJSl9UOCLzj7qTNoaLw15w9oBaX/kr64Wv6vx83GIqcdCQrNF5s6kIeY69op"
            + "iHSTQvs4ODdMvhqKMXGE0jzP+mpYR0Zp34rn3lLKxr1E8F/i7IBHfWiCo4UMNiux2v++DS5CIcI976iyuF6F8W0"
            + "X5Iew08o4cKdQqJtApIfevYSi4eshjYEDfNjUmcwm4EcP7Q16lNyqoKFWkvp8nivV74xLhNMQ";
    var decryptedText = underTest.aesDecrypt(encryptedText, secret);

    assertEquals(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy"
            + " eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At "
            + "vero eos et accusam et justo duo dolores et ea rebum.",
        decryptedText);
  }

  @Test
  void rsaEncryptShouldFollowFrontendImplementation() {
    var n =
        "l43bSozKXGPm5Fjm6bv-gO6LbPruG4fPABMfoD-IkFTgorlTTK7u1qD9RPKjlJZt41t8Z6rCfXQGwd4aJ1sIt"
            + "7A93anv1Ai5LO90ciu7jNjTbieKtAOojcGgFwQSOn1WK_8xfakaXp9SVo3vvqB8Nk-k92EANRR4JqNmepSC5Sci"
            + "Hr2h94c7ghaa8cazLJN1XQfgeOPa0xOqzCI_tMVhFwt3TGdZcA3bZ2UFxhdwy8W7b0942nG2PC6eXQDbbVyhJwR"
            + "OAgM61q_DwNtOz6lOzzSC2RTiFbdGP0sHtJqAYWTAmeC8M69rufCwpzt4AV3V2H7_2h-XTRIjuVZ-pZ1xjw";
    var encryptedBytes = underTest.rsaEncrypt("MeinRoomKey", n);

    var intArray = underTest.int8Array(encryptedBytes);
    var jsonStringified = underTest.jsonStringify(intArray);
    var updatedE2eKey = "0123456789ab" + underTest.base64AsciiEncode(jsonStringified);

    assertNotNull(updatedE2eKey);
  }

  @Test
  void int8ArrayShouldConvertBytesIntoUnsignedIntegers() {
    var byteArray = new byte[16];
    easyRandom.nextBytes(byteArray);

    var intArray = underTest.int8Array(byteArray);

    assertEquals(byteArray.length, intArray.length);
    for (var i : intArray) {
      assertTrue(i >= 0);
      assertTrue(i < 256);
    }
  }

  @Test
  void int8ArrayShouldConvertNegativeByteToItsUnsignedInteger() {
    var negativeInt = -easyRandom.nextInt(256);
    byte[] byteArray = {(byte) negativeInt};

    var intArray = underTest.int8Array(byteArray);

    assertEquals(256 + negativeInt, intArray[0]);
  }

  @Test
  void int8ArrayShouldNotConvertPositiveByte() {
    var positiveInt = easyRandom.nextInt(255);
    byte[] byteArray = {(byte) positiveInt};

    var intArray = underTest.int8Array(byteArray);

    assertEquals(positiveInt, intArray[0]);
  }

  @Test
  void jsonStringifyShouldStringifyIntegerArray() {
    int[] intArray = {185, 195, 67};

    var jsonStringified = underTest.jsonStringify(intArray);

    assertEquals("{\"0\":185,\"1\":195,\"2\":67}", jsonStringified);
  }

  @Test
  void encodeBase64AsciiShouldFollowFrontendBtoaImplementation() {
    var s = "fkefuurj6dbf";
    var t = "GUefu&dEEurj6dbf";

    assertEquals("ZmtlZnV1cmo2ZGJm", underTest.base64AsciiEncode(s));
    assertEquals("R1VlZnUmZEVFdXJqNmRiZg==", underTest.base64AsciiEncode(t));
  }
}
