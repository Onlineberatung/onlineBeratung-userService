package de.caritas.cob.userservice.api.helper;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HelperTest {

  private static String TEXT = "Lorem Ipsum";
  private static String TEXT_WITH_NEWLINE = "Lorem Ipsum\nLorem Ipsum";
  private static String TEXT_WITH_NEWLINE_AND_HTML_AND_JS =
      "<b>Lorem Ipsum</b>\nLorem Ipsum<script>alert('1');</script>";
  private static String TEXT_WITH_HTML = "<strong>Lorem Ipsum</strong>";
  private static String TEXT_WITH_JS = "Lorem Ipsum<script>alert('1');</script>";

  @InjectMocks private Helper helper;

  @Test
  public void getUnixTimestampFromDate_Should_ReturnNullIfNoParameterProvided() {
    assertNull(Helper.getUnixTimestampFromDate(null));
  }

  @Test
  public void getUnixTimestampFromDate_Should_ReturnLongIfDateProvided() {
    assertThat(Helper.getUnixTimestampFromDate(new Date()), is(instanceOf(Long.class)));
  }

  @Test
  public void removeHTMLFromText_Should_RemoveHtmlFromText() {
    assertEquals(TEXT, Helper.removeHTMLFromText(TEXT_WITH_HTML));
  }

  @Test
  public void removeHTMLFromText_Should_RemoveJavascriptFromText() {
    assertEquals(TEXT, Helper.removeHTMLFromText(TEXT_WITH_JS));
  }

  @Test
  public void removeHTMLFromText_ShouldNot_RemoveNewslinesFromText() {
    assertEquals(TEXT_WITH_NEWLINE, Helper.removeHTMLFromText(TEXT_WITH_NEWLINE));
  }

  @Test
  public void
      removeHTMLFromText_Should_RemoveHtmlAndJavascriptFromText_And_ShouldNot_RemoveNewslines() {
    assertEquals(TEXT_WITH_NEWLINE, Helper.removeHTMLFromText(TEXT_WITH_NEWLINE_AND_HTML_AND_JS));
  }
}
