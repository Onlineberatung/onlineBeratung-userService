package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.helper.MD5Utils.toMd5;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Md5UtilsTest {

  @Test
  void toMd5_Should_ConvertKnownInputToMD5() {
    assertThat("CC03E747A6AFBBCBF8BE7668ACFEBEE5".toUpperCase()).isEqualTo(toMd5("test123"));
    assertThat("5D41402ABC4B2A76B9719D911017C592").isEqualTo(toMd5("hello"));
  }

  @Test
  void toMd5_Should_ConvertNull_To_Null() {
    assertNull(toMd5(null));
  }
}
