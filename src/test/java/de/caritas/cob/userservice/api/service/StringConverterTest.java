package de.caritas.cob.userservice.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StringConverterTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks
  private StringConverter underTest;

  @Test
  void generateMasterKeyShouldFollowFrontendImplementation() {
    var stringToDigest = "MeineUserId";
    var expectedString = "26509bc23cc27fa0c95c9ed6966a29725b4bf26dbf1d538be38268d411f5677d";

    assertEquals(expectedString, underTest.hashOf(stringToDigest));
  }

  @Test
  void aesDecryptShouldFollowFrontendImplementationShort() {
    var secret = "fnwebFEBK3BFE";
    var encryptedText = "U2FsdGVkX18TwcLautoyvh0UfVXqu1nh1KF2VtWbP6XsWxjkjS22oHXIIpJnQgqtsLLuJ7dwlhei8ICjRK3TJw==";
    var decryptedText = underTest.aesDecrypt(encryptedText, secret);

    assertEquals("The quick brown fox jumps over the lazy dog.", decryptedText);
  }

  @Test
  void aesDecryptShouldFollowFrontendImplementationLong() {
    var secret = "the quick brown fox jumps over the lazy dog";
    var encryptedText = "U2FsdGVkX1/p2D62+iqhS6Irmgg+zIcKSV2dXqmT25dwPNOv8UpSmA3UepiFl131FwSktDHds"
        + "6YqqKEK8+MRXc2E4NPblSxLg8uphJSl9UOCLzj7qTNoaLw15w9oBaX/kr64Wv6vx83GIqcdCQrNF5s6kIeY69op"
        + "iHSTQvs4ODdMvhqKMXGE0jzP+mpYR0Zp34rn3lLKxr1E8F/i7IBHfWiCo4UMNiux2v++DS5CIcI976iyuF6F8W0"
        + "X5Iew08o4cKdQqJtApIfevYSi4eshjYEDfNjUmcwm4EcP7Q16lNyqoKFWkvp8nivV74xLhNMQ";
    var decryptedText = underTest.aesDecrypt(encryptedText, secret);

    assertEquals("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy"
        + " eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At "
        + "vero eos et accusam et justo duo dolores et ea rebum.", decryptedText);
  }

  @Test
  void rsaEncryptShouldEncrypt() {
    var n = "w5j-hUYZRT-ZSBJsk3J1gEtZG5fuP66dWMxs2I4PxgIC7TH8JU_zEDSjgjR6mCsIARVhyzZnBsNVoJYIg2TDF"
        + "18TAcYhaDsFEhxntg9RktrLGIs_nod0cafLCVQYWfp27SrpBeHdO9ewuezJzSzvNPZnx-8iWIDqp_nQt2xSPdh2"
        + "8AUm8f3KJ0P0AGFL6HiQ24GcLlsi-xqit3_M-MMr0kYJenaxJX1IdXCd1Io_pWBcgykSxhGo0fDWpfhkS1jmU4_"
        + "_9RNfoR1uroa10g3YVWYXvpZ5T9Qw96ynhwqdLMsGwbo1Y2AyG8NckOR3fE4ARC3OSUv0LFqmdq2xf5quZw";
    var encryptedBytes = underTest.rsaBcEncrypt("MeinRoomKey", n);

    var intArray = underTest.int8Array(encryptedBytes);
    var jsonStringified = underTest.jsonStringify(intArray);
    var l = encryptedBytes.length;
    var b = Base64.getEncoder().encodeToString(encryptedBytes);
    assertNotNull(encryptedBytes);
  }

  @Test
  void rsaEncryptByNimbusShouldEncrypt() {
    var publicKeyJson = "{\"alg\":\"RSA-OAEP-256\",\"e\":\"AQAB\",\"ext\":true,"
        + "\"key_ops\":[\"encrypt\"],\"kty\":\"RSA\","
        + "\"n\":\"w5j-hUYZRT-ZSBJsk3J1gEtZG5fuP66dWMxs2I4PxgIC7TH8JU_zEDSjgjR6mCsIARVhyzZnBsNVoJY"
        + "Ig2TDF18TAcYhaDsFEhxntg9RktrLGIs_nod0cafLCVQYWfp27SrpBeHdO9ewuezJzSzvNPZnx-8iWIDqp_nQt2"
        + "xSPdh28AUm8f3KJ0P0AGFL6HiQ24GcLlsi-xqit3_M-MMr0kYJenaxJX1IdXCd1Io_pWBcgykSxhGo0fDWpfhkS"
        + "1jmU4__9RNfoR1uroa10g3YVWYXvpZ5T9Qw96ynhwqdLMsGwbo1Y2AyG8NckOR3fE4ARC3OSUv0LFqmdq2xf5qu"
        + "Zw\"}";
    var encryptedString = underTest.rsaNimbusEncrypt("MeinRoomKey", publicKeyJson);

    var l = encryptedString.length;
    var s = new String(encryptedString, StandardCharsets.UTF_8);
    assertNotNull(encryptedString);
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
    var negativeInt = -easyRandom.nextInt(255);
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

    assertEquals("{'0':185,'1':195,'2':67}", jsonStringified);
  }

  @Test
  void encodeBase64AsciiShouldFollowFrontendBtoaImplementation() {
    var s = "fkefuurj6dbf";
    var t = "GUefu&dEEurj6dbf";

    assertEquals("ZmtlZnV1cmo2ZGJm", underTest.encodeBase64Ascii(s));
    assertEquals("R1VlZnUmZEVFdXJqNmRiZg==", underTest.encodeBase64Ascii(t));
  }
}
