package de.caritas.cob.userservice.api.model.jsonserializer;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import de.caritas.cob.userservice.api.helper.UserHelper;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DecodeUsernameJsonSerializerTest {

  @InjectMocks
  private DecodeUsernameJsonSerializer serializer;
  @Mock
  private JsonGenerator jsonGenerator;
  @Mock
  private UserHelper userHelper;

  @Test
  public void serialize_Schould_DecodeEncodedUsername() throws IOException {
    serializer.serialize(USERNAME_ENCODED, jsonGenerator, null);

    verify(jsonGenerator, times(1)).writeObject(USERNAME_DECODED);
  }

  @Test
  public void serialize_SchouldNot_DecodeDecodedUsername() throws IOException {
    serializer.serialize(USERNAME_DECODED, jsonGenerator, null);

    verify(jsonGenerator, times(1)).writeObject(USERNAME_DECODED);
  }
}
